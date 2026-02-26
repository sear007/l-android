package co.ltlabs.ltmechanic.ui.changeover

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.constant.type.COStatusType
import co.ltlabs.ltmechanic.databinding.FragmentChangeOverBinding
import co.ltlabs.ltmechanic.ui.main.BaseFragment
import co.ltlabs.ltmechanic.util.getTranslation
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.collectLatest
import org.json.JSONObject
import javax.inject.Inject

class COFragment : BaseFragment() {

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val args: COFragmentArgs by navArgs()

    private lateinit var binding: FragmentChangeOverBinding
    private var openFragment: SubCOFragment? = null
    private var readyFragment: SubCOFragment? = null
    private var closedFragment: SubCOFragment? = null

    private val viewModel: COViewModel by activityViewModels { providerFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChangeOverBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.jTranslate = languageJsonObject
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        setupViewPager()
        binding.swRefresh.setOnRefreshListener {
            refresh()
        }

        viewModel.reloadCOList.observe(viewLifecycleOwner) {
            if (it) refresh()
        }

        if (args.isOpenedByNotify) {
            val direct = COFragmentDirections.actionChangeOverFragmentToCoDetailFragment(
                args.coNo ?: ""
            )
            findNavController().navigate(direct)
        }


    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_search, menu)
    }

    private fun refresh() {
        binding.swRefresh.isRefreshing = false
        openFragment?.refresh()
        readyFragment?.refresh()
        closedFragment?.refresh()
    }

    private fun setupViewPager() {
        if (openFragment == null)
            openFragment = SubCOFragment.newInstance(COStatusType.NEW)

        if (readyFragment == null)
            readyFragment = SubCOFragment.newInstance(COStatusType.READY)

        if (closedFragment == null)
            closedFragment = SubCOFragment.newInstance(COStatusType.CLOSED)

        val titles = mutableListOf(
            String.format("%s %s", languageJsonObject.getTranslation(getString(R.string.open)), "(0)"),
            String.format("%s %s", languageJsonObject.getTranslation(getString(R.string.ready)), "(0)"),
            String.format("%s %s", languageJsonObject.getTranslation(getString(R.string.closed)), "(0)")
        )
        val fragments = mutableListOf<Fragment>(
            openFragment!!,
            readyFragment!!,
            closedFragment!!
        )
        val adapter = COPagerAdapter(
            childFragmentManager,
            lifecycle,
            fragments,
            titles
        )
        val tabLayout = binding.tab
        val viewPager = binding.viewPager.apply { isUserInputEnabled = false }
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = adapter.itemCount
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = adapter.getTitle(position)
        }.attach()

        lifecycleScope.launchWhenCreated {
            viewModel.getPagingSizeOpenStatus()?.collectLatest {
                val tab = binding.tab.getTabAt(0)
                tab?.text = String.format(
                    "%s %s",
                    languageJsonObject.getTranslation(getString(R.string.open)),
                    "(${it?.totalRecord ?: 0})"
                )
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.getPagingSizeReadyStatus()?.collectLatest {
                val tab = binding.tab.getTabAt(1)
                tab?.text = String.format(
                    "%s %s",
                    languageJsonObject.getTranslation(getString(R.string.ready)),
                    "(${it?.totalRecord ?: 0})"
                )
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.getPagingSizeCloseStatus()?.collectLatest {
                val tab = binding.tab.getTabAt(2)
                tab?.text = String.format(
                    "%s %s",
                    languageJsonObject.getTranslation(getString(R.string.closed)),
                    "(${it?.totalRecord ?: 0})"
                )
            }
        }
    }
}