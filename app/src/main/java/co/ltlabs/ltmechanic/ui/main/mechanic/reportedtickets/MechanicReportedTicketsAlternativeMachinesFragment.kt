package co.ltlabs.ltmechanic.ui.main.mechanic.reportedtickets

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager

import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.databinding.FragmentMechanicReportedTicketsAlternativeMachinesBinding
import co.ltlabs.ltmechanic.ui.adapter.MechanicMachineAvailableListAdapter
import co.ltlabs.ltmechanic.ui.adapter.MechanicMachineAvailableOtherBrandsListAdapter
import co.ltlabs.ltmechanic.util.ApiStatus
import co.ltlabs.ltmechanic.util.ConnectionUtil
import co.ltlabs.ltmechanic.util.getTranslation
import co.ltlabs.ltmechanic.util.showProgressBar
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.MechanicReportedTicketsAlternativeMachinesViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.LineViewModel
import dagger.android.support.DaggerFragment
import org.json.JSONObject
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class MechanicReportedTicketsAlternativeMachinesFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val viewModel: MechanicReportedTicketsAlternativeMachinesViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MechanicReportedTicketsAlternativeMachinesViewModel::class.java)
    }

    private val lineViewModel: LineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(LineViewModel::class.java)
    }

    private var selectedTab = "brand"

    private val args: MechanicReportedTicketsAlternativeMachinesFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentMechanicReportedTicketsAlternativeMachinesBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        // Start translation
        with(languageJsonObject) {
            with(binding) {
                toolBarSelectedLineTextView.text = getTranslation(toolBarSelectedLineTextView.text.toString())
                toolBarTitleTextView.text = getTranslation(toolBarTitleTextView.text.toString())
                otherBrandsTab.text = getTranslation(otherBrandsTab.text.toString())
                noAvailableMachine.text = getTranslation(noAvailableMachine.text.toString())
            }
        }
        // End translation

        ConnectionUtil.internetConnected.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it) {

                    lineViewModel.getMachinesAvailableByArea(args.areaId, args.macSubTypeId, args.brandId)
                    lineViewModel.getMachinesOtherBrandAvailableByArea(args.areaId, args.macSubTypeId, args.brand)

                    ConnectionUtil.setInternetConnected(false)
                }
                ConnectionUtil.setInternetConnectedComplete()
            }
        })

        lineViewModel.getMachinesAvailableByArea(args.areaId, args.macSubTypeId, args.brandId)
        lineViewModel.getMachinesOtherBrandAvailableByArea(args.areaId, args.macSubTypeId, args.brand)

        binding.toolBarSelectedLineTextView.text = args.area
        binding.brandTab.text = args.brand

        val adapter = MechanicMachineAvailableListAdapter(languageJsonObject)
        binding.recyclerViewBrand.layoutManager = LinearLayoutManager(activity)
        binding.recyclerViewBrand.adapter = adapter


        val adapterOtherBrands = MechanicMachineAvailableOtherBrandsListAdapter(languageJsonObject)
        binding.recyclerViewOtherBrands.layoutManager = LinearLayoutManager(activity)
        binding.recyclerViewOtherBrands.adapter = adapterOtherBrands

        lineViewModel.machinesAvailable.observe(viewLifecycleOwner, Observer {
            if (it != null) {

                if (selectedTab == "brand") {
                    binding.noAvailableMachine.visibility = if (it.isEmpty()) {
                        View.VISIBLE
                    } else {
                        View.INVISIBLE
                    }
                }

                adapter.data = it

                lineViewModel.machinesAvailableComplete()
            }
        })

        lineViewModel.machinesOtherBrandAvailable.observe(viewLifecycleOwner, Observer {
            if (it != null) {

                if (selectedTab == "other_brand") {
                    binding.noAvailableMachine.visibility = if (it.isEmpty()) {
                        View.VISIBLE
                    } else {
                        View.INVISIBLE
                    }
                }

                adapterOtherBrands.data = it

                lineViewModel.machinesOtherBrandAvailableComplete()
            }
        })

        binding.brandTab.setOnClickListener {
            selectedTab = "brand"
            binding.recyclerViewBrand.visibility = View.VISIBLE
            binding.recyclerViewOtherBrands.visibility = View.INVISIBLE

            binding.otherBrandsTab.setBackgroundColor(Color.parseColor("#1D5072"))
            binding.brandTab.setBackgroundColor(Color.parseColor("#0F75BC"))
        }

        binding.otherBrandsTab.setOnClickListener {
            selectedTab = "other_brand"
            binding.recyclerViewOtherBrands.visibility = View.VISIBLE
            binding.recyclerViewBrand.visibility = View.INVISIBLE
            binding.otherBrandsTab.setBackgroundColor(Color.parseColor("#0F75BC"))
            binding.brandTab.setBackgroundColor(Color.parseColor("#1D5072"))
        }

        lineViewModel.status.observe(viewLifecycleOwner, Observer {
            when (it) {
                ApiStatus.LOADING -> {
                    binding.progressBar.showProgressBar(true)
                }

                else -> {
                    binding.progressBar.showProgressBar(false)
                }
            }
        })

        return binding.root
    }

}
