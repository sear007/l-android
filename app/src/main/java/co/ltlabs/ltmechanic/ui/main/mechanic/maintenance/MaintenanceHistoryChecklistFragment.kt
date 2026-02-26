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
import co.ltlabs.ltmechanic.databinding.FragmentMaintenanceHistoryChecklistBinding
import co.ltlabs.ltmechanic.ui.adapter.MechanicMaintenanceChecklistListAdapter
import co.ltlabs.ltmechanic.ui.adapter.MechanicMaintenanceHistoryChecklistListAdapter
import co.ltlabs.ltmechanic.util.ApiStatus
import co.ltlabs.ltmechanic.util.ConnectionUtil
import co.ltlabs.ltmechanic.util.getTranslation
import co.ltlabs.ltmechanic.util.showProgressBar
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.MaintenanceHistoryChecklistViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.ChecklistViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.TicketViewModel
import dagger.android.support.DaggerFragment
import org.json.JSONObject
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class MaintenanceHistoryChecklistFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val viewModel: MaintenanceHistoryChecklistViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MaintenanceHistoryChecklistViewModel::class.java)
    }

    private val checklistViewModel: ChecklistViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(ChecklistViewModel::class.java)
    }

    private val ticketViewModel: TicketViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(TicketViewModel::class.java)
    }

    private val args: MaintenanceHistoryChecklistFragmentArgs by navArgs()

    private var totalSteps = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentMaintenanceHistoryChecklistBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        binding.toolBarTitleTextView.text = args.ticketNo

        binding.ticketNo2.text = languageJsonObject.getTranslation(binding.ticketNo2.text.toString())

        ConnectionUtil.internetConnected.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it) {

//                    ticketViewModel.getTicketDetailsById(args.ticketId)
                    ticketViewModel.getTicketDetailsByTicketNo(args.ticketNo)

                    ConnectionUtil.setInternetConnected(false)
                }
                ConnectionUtil.setInternetConnectedComplete()
            }
        })

//        ticketViewModel.getTicketDetailsById(args.ticketId)
        ticketViewModel.getTicketDetailsByTicketNo(args.ticketNo)

        ticketViewModel.maintenanceCheckList.observe(viewLifecycleOwner, Observer { checkList ->
            if (checkList != null) {

                totalSteps = checkList.size

//                binding.stepsTextView.text = getString(
//                    R.string.checklist_steps,
//                    checkList.filter { it.checked }.size.toString(),
//                    totalSteps.toString()
//                )
                binding.stepsTextView.text = "${checkList.filter { it.checked }.size}/${checkList.size} ${languageJsonObject.getTranslation("STEPS")}"

                checklistViewModel.setMaintenanceChecklists(checkList)

                binding.recyclerView.apply {
                    val linearLayoutManager = LinearLayoutManager(activity)
                    val adapter = MechanicMaintenanceHistoryChecklistListAdapter()
                    layoutManager = linearLayoutManager
                    this.adapter = adapter
                    adapter.data = checkList
                }

                ticketViewModel.maintenanceChecklistComplete()
            }
        })

        checklistViewModel.selectedTaskCount.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                binding.stepsTextView.text = getString(
                    R.string.checklist_steps,
                    it.toString(),
                    totalSteps.toString()
                )
            }
        })

        checklistViewModel.status.observe(viewLifecycleOwner, Observer {
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
