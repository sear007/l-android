package co.ltlabs.ltmechanic.ui.main.mechanic.maintenance

import android.os.Bundle
import android.util.Log
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
import co.ltlabs.ltmechanic.databinding.FragmentMaintenanceHistoryBinding
import co.ltlabs.ltmechanic.ui.adapter.MechanicMaintenanceHistoryListAdapter
import co.ltlabs.ltmechanic.util.ConnectionUtil
import co.ltlabs.ltmechanic.util.getTranslation
import co.ltlabs.ltmechanic.util.navigate
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.MaintenanceHistoryViewModel
import dagger.android.support.DaggerFragment
import org.json.JSONObject
import javax.inject.Inject

private const val TAG = "MaintenanceHistory";

/**
 * A simple [Fragment] subclass.
 */
class MaintenanceHistoryFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val viewModel: MaintenanceHistoryViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MaintenanceHistoryViewModel::class.java)
    }

    private val args: MaintenanceHistoryFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentMaintenanceHistoryBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        // Start translation
        with(languageJsonObject) {
            with(binding) {
                toolBarTitleTextView.text = getTranslation(toolBarTitleTextView.text.toString())
                noMaintenanceHistory.text = getTranslation(noMaintenanceHistory.text.toString())
            }
        }
        // End translation

        ConnectionUtil.internetConnected.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it) {

                    viewModel.getMaintenanceHistory(args.machineId)

                    ConnectionUtil.setInternetConnected(false)
                }
                ConnectionUtil.setInternetConnectedComplete()
            }
        })

        viewModel.getMaintenanceHistory(args.machineId)

        viewModel.maintenanceHistoryTickets.observe(viewLifecycleOwner, Observer { maintenanceHistory ->
            if (maintenanceHistory != null) {

                if (maintenanceHistory.isEmpty()) {
                    binding.noMaintenanceHistory.visibility = View.VISIBLE
                }

                binding.recyclerView.apply {
                    val linearLayoutManager = LinearLayoutManager(activity)
                    val adapter = MechanicMaintenanceHistoryListAdapter(viewModel, languageJsonObject)
                    layoutManager = linearLayoutManager
                    this.adapter = adapter
                    adapter.data = maintenanceHistory
                }

                viewModel.maintenanceHistoryComplete()
            }
        })

        viewModel.navigateToCheckList.observe(viewLifecycleOwner, Observer { maintenanceHistory ->
            if (maintenanceHistory != null) {

                navigateToChecklist(
                    maintenanceHistory.id,
                    maintenanceHistory.ticketNo,
                    maintenanceHistory.machineId
                )

                viewModel.navigateToChecklistComplete()
            }
        })

        return binding.root
    }

    private fun navigateToChecklist(
        ticketId: Long,
        ticketNo: String,
        machineId: Long
    ) {
        val action = MaintenanceHistoryFragmentDirections
            .actionMaintenanceHistoryFragmentToMaintenanceHistoryChecklistFragment(
                ticketId,
                ticketNo,
                machineId
            )
        navigate(action)
    }

}
