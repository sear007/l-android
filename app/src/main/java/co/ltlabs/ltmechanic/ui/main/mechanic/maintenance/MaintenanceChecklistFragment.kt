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
import co.ltlabs.ltmechanic.databinding.FragmentMaintenanceChecklistBinding
import co.ltlabs.ltmechanic.ui.adapter.MechanicMaintenanceChecklistListAdapter
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.MaintenanceChecklistViewmodel
import co.ltlabs.ltmechanic.viewmodels.shared.ChecklistViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.TicketViewModel
import dagger.android.support.DaggerFragment
import org.json.JSONObject
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class MaintenanceChecklistFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val viewModel: MaintenanceChecklistViewmodel by lazy {
        ViewModelProvider(this, providerFactory).get(MaintenanceChecklistViewmodel::class.java)
    }

    private val checklistViewModel: ChecklistViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(ChecklistViewModel::class.java)
    }

    private val ticketViewModel: TicketViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(TicketViewModel::class.java)
    }

    private var totalSteps = 0

    private val args: MaintenanceChecklistFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentMaintenanceChecklistBinding.inflate(inflater)
        binding.remarksEditText.setText(args.remark)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel
        binding.toolBarTitleTextView.text = args.ticketNo

        with(languageJsonObject) {
            with(binding) {
                ticketNo2.text = getTranslation(ticketNo2.text.toString())
                labelRemarks2.text = getTranslation(labelRemarks2.text.toString())
                btnSave.text = getTranslation(btnSave.text.toString())
                btnComplete.text = getTranslation(btnComplete.text.toString())
            }
        }

        if (args.ticketStatus == "COMPLETED") {
            binding.labelRemarks2.visibility = View.INVISIBLE
            binding.remarksEditText.visibility = View.INVISIBLE
            binding.btnSave.visibility = View.INVISIBLE
            binding.btnComplete.visibility = View.INVISIBLE
            binding.recyclerView.visibility = View.INVISIBLE
            binding.recyclerViewCompleted.visibility = View.VISIBLE
        }

//        ticketViewModel.getTicketDetailsById(args.ticketId)
        ticketViewModel.getTicketDetailsByTicketNo(args.ticketNo)

        ticketViewModel.maintenanceCheckList.observe(viewLifecycleOwner, Observer { checkList ->
            if (checkList != null) {

                totalSteps = checkList.size

//                binding.stepsTextView.text = getString(
//                    R.string.checklist_steps,
//                    checkList.filter { it.checked }.size.toString(),
//                    checkList.size.toString()
//                )

                binding.stepsTextView.text = "${checkList.filter { it.checked }.size}/${checkList.size} ${languageJsonObject.getTranslation("STEPS")}"

                checklistViewModel.setMaintenanceChecklists(checkList)

                binding.recyclerView.apply {
                    val linearLayoutManager = LinearLayoutManager(activity)
                    val adapter = MechanicMaintenanceChecklistListAdapter(viewModel, checklistViewModel)
                    layoutManager = linearLayoutManager
                    this.adapter = adapter
                    adapter.data = checkList
                }

                binding.recyclerViewCompleted.apply {
                    val linearLayoutManager = LinearLayoutManager(activity)
                    val adapter = MechanicMaintenanceChecklistListAdapter(viewModel, checklistViewModel)
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

        binding.btnSave.setOnClickListener {
            checklistViewModel.updateMaintenanceChecklist(
                checklistViewModel.maintenanceChecklistsTemp,
                args.ticketNo,
                binding.remarksEditText.text.toString()
            )
        }

        binding.btnComplete.setOnClickListener {
            checklistViewModel.completeMaintenanceChecklist(
                checklistViewModel.maintenanceChecklistsTemp,
                args.ticketNo,
                binding.remarksEditText.text.toString()
            )
        }

        checklistViewModel.completeChecklistStatus.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                when (it) {
                    ChecklistStatus.SUCCESS -> {
//                        navigateToMaintenance()
//                        ticketViewModel.updateTicketStatus(args.ticketNo, StatusIdUtil.MT_COMPLETED.toString(), type = "M", remarks = binding.remarksEditText.text.toString())
                        ticketViewModel.getStatusIdAndUpdateTicketStatus(
                            TicketsStatus.COMPLETED, TicketModule.MAINTENANCE, args.ticketNo, type = "M", remarks = binding.remarksEditText.text.toString()
                        )
                    }
                }

                checklistViewModel.completeChecklistStatusComplete()
            }
        })

        ticketViewModel.ticketUpdateStatus.observe(viewLifecycleOwner, Observer {
            when (it) {
                TicketUpdateStatus.HAS_OPEN_TICKETS -> {
                    binding.coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Cannot Start Maintenance. Machine has open repair ticket"))
                }
            }
        })

        ticketViewModel.ticketStatus.observe(viewLifecycleOwner, Observer { ticketStatus ->
            if (ticketStatus != null) {

                when (ticketStatus) {

                    TicketStatus.COMPLETED -> {
                        navigateToMaintenance()
                    }

                }

                ticketViewModel.ticketStatusComplete()
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

        checklistViewModel.checklistStatus.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                when (it) {
                    ChecklistStatus.SUCCESS -> {
                        navigateToMaintenance()
                    }
                }

                checklistViewModel.checklistStatusComplete()
            }
        })


        return binding.root
    }

    private fun navigateToMaintenance() {
        val action = MaintenanceChecklistFragmentDirections
            .actionMaintenanceChecklistFragmentToMaintenanceFragment()
        navigate(action)
    }

}
