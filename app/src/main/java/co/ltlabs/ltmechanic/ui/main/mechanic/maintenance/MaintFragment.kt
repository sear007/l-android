package co.ltlabs.ltmechanic.ui.main.mechanic.maintenance

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.constant.type.MaintType
import co.ltlabs.ltmechanic.databinding.FragmentMaintBinding
import co.ltlabs.ltmechanic.ui.main.BaseFragment
import co.ltlabs.ltmechanic.ui.main.Resource
import co.ltlabs.ltmechanic.ui.main.main_helper.NFCViewModel
import co.ltlabs.ltmechanic.ui.main.mechanic.maintenance.adapter.MaintPagerAdapter
import co.ltlabs.ltmechanic.util.getTranslation
import co.ltlabs.ltmechanic.util.showSnackBar
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.collectLatest
import org.json.JSONObject
import javax.inject.Inject

class MaintFragment : BaseFragment() {

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val viewModel: MaintViewModel by activityViewModels { providerFactory }
    private val nfcViewModel: NFCViewModel by activityViewModels()

    private lateinit var binding: FragmentMaintBinding
    private lateinit var overdueFragment: SubMaintFragment
    private lateinit var scheduleFragment: SubMaintFragment
    private lateinit var closedFragment: SubMaintFragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMaintBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.jTranslate = languageJsonObject
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
        setHasOptionsMenu(true)
        listenRFID()

        binding.swRefresh.setOnRefreshListener {
            refresh()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_search, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_search) {
            nfcViewModel.isObserveOutsideMainActivity = true
            showFindMachineDialog {
                refresh(it)
            }
            return true
        }
        return false
    }

    private fun refresh(machine: String? = null) {
        binding.swRefresh.isRefreshing = false
        overdueFragment.refresh(machine)
        scheduleFragment.refresh(machine)
        closedFragment.refresh(machine)
    }

    private fun listenRFID() {
        lifecycleScope.launchWhenCreated {
            nfcViewModel.scanRfid.collectLatest {
                findMachineBsDialog?.dismiss()
                nfcViewModel.isObserveOutsideMainActivity = false
                viewModel.getMachineByRfid(it)
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.machine.collectLatest {
                when (it.status) {
                    Resource.Status.ERROR -> {
                        showSnackBar(
                            binding.root,
                            languageJsonObject.getTranslation(it.message.toString())
                        )
                        binding.swRefresh.isRefreshing = false
                    }
                    Resource.Status.LOADING -> binding.swRefresh.isRefreshing = true
                    else -> {
                        binding.swRefresh.isRefreshing = false
                        refresh(it.data?.machine)
                    }
                }
            }
        }
    }

    private fun setupViewPager() {
        if (!this::overdueFragment.isInitialized) {
            overdueFragment = SubMaintFragment.newInstance(MaintType.OVERDUE)
            overdueFragment.onClearFilter {
                refresh()
            }
        }

        if (!this::scheduleFragment.isInitialized) {
            scheduleFragment = SubMaintFragment.newInstance(MaintType.SCHEDULED)
            scheduleFragment.onClearFilter {
                refresh()
            }
        }

        if (!this::closedFragment.isInitialized) {
            closedFragment = SubMaintFragment.newInstance(MaintType.CLOSED)
            closedFragment.onClearFilter {
                refresh()
            }
        }

        val titles = mutableListOf(
            String.format("%s %s", languageJsonObject.getTranslation("OVERDUE"), "(0)"),
            String.format("%s %s", languageJsonObject.getTranslation("ON SCHEDULED"), "(0)"),
            String.format("%s %s", languageJsonObject.getTranslation(MaintType.CLOSED), "(0)")
        )
        val fragments = mutableListOf<Fragment>(
            overdueFragment,
            scheduleFragment,
            closedFragment
        )
        val adapter = MaintPagerAdapter(
            childFragmentManager,
            lifecycle,
            requireContext(),
            fragments,
            titles
        )
        val tabLayout = binding.tab
        val viewPager = binding.viewPager.apply { isUserInputEnabled = false }
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = adapter.itemCount
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.customView = adapter.getTabView(position)
        }.attach()

        lifecycleScope.launchWhenCreated {
            viewModel.getPagingSizeOverdueStatus()?.collectLatest {
                val tab = binding.tab.getTabAt(0)
                val tv = tab?.customView?.findViewById<TextView>(R.id.tv_value)
                tv?.text = String.format(
                    "%s %s",
                    languageJsonObject.getTranslation("OVERDUE"),
                    "(${it?.totalRecord ?: 0})"
                )
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.getPagingSizeScheduleStatus()?.collectLatest {
                val tab = binding.tab.getTabAt(1)
                val tv = tab?.customView?.findViewById<TextView>(R.id.tv_value)
                tv?.text = String.format(
                    "%s %s",
                    languageJsonObject.getTranslation("ON SCHEDULED"),
                    "(${it?.totalRecord ?: 0})"
                )
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.getPagingSizeCloseStatus()?.collectLatest {
                val tab = binding.tab.getTabAt(2)
                val tv = tab?.customView?.findViewById<TextView>(R.id.tv_value)
                tv?.text = String.format(
                    "%s %s",
                    languageJsonObject.getTranslation(MaintType.CLOSED),
                    "(${it?.totalRecord ?: 0})"
                )
            }
        }
    }
}