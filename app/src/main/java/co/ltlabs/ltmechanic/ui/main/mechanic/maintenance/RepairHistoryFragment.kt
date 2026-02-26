package co.ltlabs.ltmechanic.ui.main.mechanic.maintenance

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
import co.ltlabs.ltmechanic.databinding.FragmentRepairHistoryBinding
import co.ltlabs.ltmechanic.domain.asMachineHistoryDomainModel
import co.ltlabs.ltmechanic.ui.adapter.MechanicRepairHistoryListAdapter
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.MaintenanceRepairHistoryViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.TicketViewModel
import dagger.android.support.DaggerFragment
import org.json.JSONObject
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class RepairHistoryFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val viewModel: MaintenanceRepairHistoryViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MaintenanceRepairHistoryViewModel::class.java)
    }

    private val ticketViewModel: TicketViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(TicketViewModel::class.java)
    }

    private val args: RepairHistoryFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentRepairHistoryBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        // Start translation
        with(languageJsonObject) {
            with(binding) {
                toolBarTitleTextView.text = getTranslation(toolBarTitleTextView.text.toString())
                noRepairHistory.text = getTranslation(noRepairHistory.text.toString())
            }
        }
        // End translation

        ConnectionUtil.internetConnected.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it) {

                    if (TicketUtil.isQueryMachine) {
                        ticketViewModel.getTicketRepairHistoryByMachineId(args.machineId)
                    } else {
                        ticketViewModel.getMachineRepairedHistoryByMachineId(args.machineId)
                    }

                    ConnectionUtil.setInternetConnected(false)
                }
                ConnectionUtil.setInternetConnectedComplete()
            }
        })

        if (TicketUtil.isQueryMachine) {
            ticketViewModel.getTicketRepairHistoryByMachineId(args.machineId)
        } else {
            ticketViewModel.getMachineRepairedHistoryByMachineId(args.machineId)
        }

        ticketViewModel.machineHistory.observe(viewLifecycleOwner, Observer { machineHistory ->

            if (machineHistory.isEmpty()) {
                binding.noRepairHistory.visibility = View.VISIBLE
            }

            binding.recyclerView.apply {
                val linearLayoutManager = LinearLayoutManager(activity)
                val adapter = MechanicRepairHistoryListAdapter(languageJsonObject)
                layoutManager = linearLayoutManager
                this.adapter = adapter
                adapter.data = machineHistory
            }

        })

        ticketViewModel.ticketRepairHistory.observe(viewLifecycleOwner, Observer { ticketRepairHistory ->

            if (ticketRepairHistory.isEmpty()) {
                binding.noRepairHistory.visibility = View.VISIBLE
            }

            binding.recyclerView.apply {
                val linearLayoutManager = LinearLayoutManager(activity)
                val adapter = MechanicRepairHistoryListAdapter(languageJsonObject)
                layoutManager = linearLayoutManager
                this.adapter = adapter
                adapter.data = ticketRepairHistory.asMachineHistoryDomainModel()
            }

        })

        ticketViewModel.status.observe(viewLifecycleOwner, Observer {

            when(it) {

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
