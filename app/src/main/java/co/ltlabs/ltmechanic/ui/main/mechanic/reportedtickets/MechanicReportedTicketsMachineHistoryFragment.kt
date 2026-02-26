package co.ltlabs.ltmechanic.ui.main.mechanic.reportedtickets

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
import co.ltlabs.ltmechanic.databinding.FragmentMechanicReportedTicketsMachineHistoryBinding
import co.ltlabs.ltmechanic.ui.adapter.MechanicMachineHistoryListAdapter
import co.ltlabs.ltmechanic.util.ApiStatus
import co.ltlabs.ltmechanic.util.ConnectionUtil
import co.ltlabs.ltmechanic.util.getTranslation
import co.ltlabs.ltmechanic.util.showProgressBar
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.MechanicReportedTicketsMachineHistoryViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.TicketViewModel
import dagger.android.support.DaggerFragment
import org.json.JSONObject
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class MechanicReportedTicketsMachineHistoryFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val viewModel: MechanicReportedTicketsMachineHistoryViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MechanicReportedTicketsMachineHistoryViewModel::class.java)
    }

    private val ticketViewModel: TicketViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(TicketViewModel::class.java)
    }

    private val args: MechanicReportedTicketsMachineHistoryFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentMechanicReportedTicketsMachineHistoryBinding.inflate(inflater)

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

                    ticketViewModel.getMachineRepairedHistoryByMachineId(args.machineId)

                    ConnectionUtil.setInternetConnected(false)
                }
                ConnectionUtil.setInternetConnectedComplete()
            }
        })

        ticketViewModel.getMachineRepairedHistoryByMachineId(args.machineId)

        val adapter = MechanicMachineHistoryListAdapter(languageJsonObject)

        ticketViewModel.machineHistory.observe(viewLifecycleOwner, Observer { machineHistory ->

            if (machineHistory.isEmpty()) {
                binding.noRepairHistory.visibility = View.VISIBLE
            }

            adapter.data = machineHistory

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

        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.adapter = adapter



        return binding.root
    }

}
