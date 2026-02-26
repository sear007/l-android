package co.ltlabs.ltmechanic.ui.main.mechanic.inrepairtickets

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
import co.ltlabs.ltmechanic.databinding.FragmentMechanicInRepairTicketsChecklistBinding
import co.ltlabs.ltmechanic.ui.adapter.MechanicTicketChecklistListAdapter
import co.ltlabs.ltmechanic.ui.main.mechanic.reportedtickets.MechanicReportedTicketsChecklistFragmentDirections
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.MainViewModel
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.MechanicInRepairTicketsChecklistViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.ChecklistViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.TicketViewModel
import dagger.android.support.DaggerFragment
import org.json.JSONObject
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class MechanicInRepairTicketsChecklistFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val viewModel: MechanicInRepairTicketsChecklistViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MechanicInRepairTicketsChecklistViewModel::class.java)
    }

    private val args: MechanicInRepairTicketsChecklistFragmentArgs by navArgs()

    private val ticketViewModel: TicketViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(TicketViewModel::class.java)
    }

    private val checklistViewModel: ChecklistViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(ChecklistViewModel::class.java)
    }

    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MainViewModel::class.java)
    }

    private var totalSteps = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentMechanicInRepairTicketsChecklistBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        // Start translation
        with(languageJsonObject) {
            with(binding) {
                toolBarTitleTextView.text = getTranslation(toolBarTitleTextView.text.toString())
                ticketNo2.text = getTranslation(ticketNo2.text.toString())
                btnCancel5.text = getTranslation(btnCancel5.text.toString())
                btnSave.text = getTranslation(btnSave.text.toString())
            }
        }
        // End translation

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

        val adapter = MechanicTicketChecklistListAdapter(checklistViewModel, ticketViewModel)

        ticketViewModel.checkList.observe(viewLifecycleOwner, Observer {checkList ->
            if (checkList != null) {

                totalSteps = checkList.size

//                binding.stepsTextView.text = getString(
//                    R.string.checklist_steps,
//                    checkList.filter { it.checked }.size.toString(),
//                    checkList.size.toString()
//                )

                binding.stepsTextView.text = "${checkList.filter { it.checked }.size}/${checkList.size} ${languageJsonObject.getTranslation("STEPS")}"

                checklistViewModel.setChecklists(checkList)

                adapter.data = checkList

                ticketViewModel.checklistComplete()
            }
        })

        binding.btnSave.setOnClickListener {
            checklistViewModel.updateChecklist(checklistViewModel.checklistsTemp)
        }

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

        checklistViewModel.selectedTaskCount.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                binding.stepsTextView.text = getString(
                    R.string.checklist_steps,
                    it.toString(),
                    totalSteps.toString()
                )
            }
        })

        mainViewModel.firebaseNotificationFromDatabase.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it.isNotEmpty()) {

//                    viewModel.sendNotificationToMechanics("Bearer ${it[0].token}", "clare", "New repaired ticket", "A ticket has been repaired.")
                }
            }
        })

        binding.btnCancel5.setOnClickListener {
            navigateToInRepairList()
        }

        checklistViewModel.checklistStatus.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                when (it) {
                    ChecklistStatus.SUCCESS -> {
                        navigateToInRepairList()
                    }
                }

                checklistViewModel.checklistStatusComplete()
            }
        })

        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.adapter = adapter

        return binding.root
    }

    private fun navigateToInRepairList() {
        val action = MechanicInRepairTicketsChecklistFragmentDirections
            .actionMechanicInRepairTicketsChecklistFragment2ToMechanicInRepairTicketsFragment()
        navigate(action)
    }

}
