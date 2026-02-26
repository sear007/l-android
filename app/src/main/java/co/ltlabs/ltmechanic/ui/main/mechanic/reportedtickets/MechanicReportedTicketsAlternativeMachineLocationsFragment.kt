package co.ltlabs.ltmechanic.ui.main.mechanic.reportedtickets

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager

import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.databinding.FragmentMechanicReportedTicketsAlternativeMachineLocationsBinding
import co.ltlabs.ltmechanic.ui.adapter.MechanicMachineLocationListAdapter
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.MechanicReportedTicketsAlternativeMachineLocationsViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.LineViewModel
import dagger.android.support.DaggerFragment
import org.json.JSONObject
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class MechanicReportedTicketsAlternativeMachineLocationsFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val viewModel: MechanicReportedTicketsAlternativeMachineLocationsViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MechanicReportedTicketsAlternativeMachineLocationsViewModel::class.java)
    }

    private val lineViewModel: LineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(LineViewModel::class.java)
    }

    private val args: MechanicReportedTicketsAlternativeMachineLocationsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentMechanicReportedTicketsAlternativeMachineLocationsBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        // Start translation
        with(languageJsonObject) {
            with(binding) {
                toolBarTitleTextView.text = getTranslation(toolBarTitleTextView.text.toString())
                noAvailableMachine.text = getTranslation(noAvailableMachine.text.toString())
            }
        }
        // End translation

        lineViewModel.getStorageAreasBySubType(args.macSubTypeId)

        ConnectionUtil.internetConnected.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it) {

                    lineViewModel.getStorageAreasBySubType(args.macSubTypeId)

                    ConnectionUtil.setInternetConnected(false)
                }
                ConnectionUtil.setInternetConnectedComplete()
            }
        })

        val adapter = MechanicMachineLocationListAdapter(viewModel, languageJsonObject)

        lineViewModel.machineLocations.observe(viewLifecycleOwner, Observer {
            if (it != null) {

                binding.noAvailableMachine.visibility = if (it.isEmpty()) {
                    View.VISIBLE
                } else {
                    View.INVISIBLE
                }

                adapter.data = it

                lineViewModel.machineLocationsComplete()
            }
        })

        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.adapter = adapter

        viewModel.navigateToAvailableMachines.observe(viewLifecycleOwner, Observer {
            if (it != null) {

                navigateToAvailableMachines(
                    args.macSubTypeId,
                    args.brandId,
                    args.brand,
                    it.locationId,
                    it.location
                )

                viewModel.navigateToAvailableMachinesComplete()
            }
        })

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

    private fun navigateToAvailableMachines(macSubTypeId: Long, brandId: Long, brand: String, areaId: Long, area: String) {
        val action = MechanicReportedTicketsAlternativeMachineLocationsFragmentDirections
            .actionMechanicReportedTicketsAlternativeMachineLocationsFragmentToMechanicReportedTicketsAlternativeMachinesFragment(
                macSubTypeId,
                brandId,
                brand,
                areaId,
                area
            )
        navigate(action)
    }

}
