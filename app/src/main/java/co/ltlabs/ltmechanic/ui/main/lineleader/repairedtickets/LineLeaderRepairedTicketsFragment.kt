package co.ltlabs.ltmechanic.ui.main.lineleader.repairedtickets

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import co.ltlabs.ltmechanic.databinding.FragmentLineLeaderRepairedTicketsBinding
import co.ltlabs.ltmechanic.databinding.PopupLineLeaderErrorPopupNotOnLineBinding
import co.ltlabs.ltmechanic.domain.NextMainDate
import co.ltlabs.ltmechanic.ui.adapter.LineLeaderClosedTicketsListAdapter
import co.ltlabs.ltmechanic.ui.adapter.LineLeaderRepairedTicketsListAdapter
import co.ltlabs.ltmechanic.ui.main.BaseFragment
import co.ltlabs.ltmechanic.ui.main.DashboardViewModel
import co.ltlabs.ltmechanic.ui.main.main_helper.NFCViewModel
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.util.nfc.NFCAction
import co.ltlabs.ltmechanic.viewmodels.main.lineleader.LineLeaderRepairedTicketsViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.MachineViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.TicketViewModel
import org.json.JSONObject
import java.util.*
import javax.inject.Inject

private const val TAG = "LLRepairedTickets"

class LineLeaderRepairedTicketsFragment : BaseFragment() {

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val args: LineLeaderRepairedTicketsFragmentArgs by navArgs()

    private val viewModel: LineLeaderRepairedTicketsViewModel by viewModels { providerFactory }
    private val machineViewModel: MachineViewModel by viewModels { providerFactory }
    private val ticketViewModel: TicketViewModel by viewModels { providerFactory }
    private val nfcViewModel: NFCViewModel by activityViewModels()
    private val dashboardViewModel: DashboardViewModel by activityViewModels { providerFactory }

    private var popupWindow: PopupWindow? = null

    private var selectedLinesStr = mutableListOf<String>()
    private var selectedLinesIdStr = mutableListOf<String>()
    private var selectedAreasIdStr = mutableListOf<String>()

    private var loadedMachine = ""
    private var loadedMachineId = 0L
    private var loadedMachineMfgLine = ""
    private var loadedMachineStation = ""

    private lateinit var coordinatorLayout: CoordinatorLayout

    private var action = ""

    private lateinit var binding: FragmentLineLeaderRepairedTicketsBinding

    override fun onPause() {
        super.onPause()

        TicketUtil.selectedRepairedTab = "repaired"
    }

    override fun onResume() {
        super.onResume()

        when (TicketUtil.selectedRepairedTab) {

            "repaired" -> {
                binding.repairedTicketsRecyclerView.visibility = View.VISIBLE
                binding.closedTicketsRecyclerView.visibility = View.INVISIBLE

                binding.repairedTicketsTab.setBackgroundColor(Color.parseColor("#0F75BC"))
                binding.closedTicketsTab.setBackgroundColor(Color.parseColor("#1D5072"))
            }

            "closed" -> {
                binding.closedTicketsRecyclerView.visibility = View.VISIBLE
                binding.repairedTicketsRecyclerView.visibility = View.INVISIBLE

                binding.closedTicketsTab.setBackgroundColor(Color.parseColor("#0F75BC"))
                binding.repairedTicketsTab.setBackgroundColor(Color.parseColor("#1D5072"))
            }

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        if (viewModel.isAlreadyGoToDetail) {
            TicketUtil.selectedRepairedTab = if (viewModel.isClosedTab) "closed" else "repaired"
        } else {
            TicketUtil.selectedRepairedTab = if (args.isClosedTicket) "closed" else "repaired"
        }

        binding = FragmentLineLeaderRepairedTicketsBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.jTranslate = languageJsonObject
        binding.viewModel = viewModel

        coordinatorLayout = binding.coordinatorLayout

        val repairedTicketsListAdapter =
            LineLeaderRepairedTicketsListAdapter(viewModel, languageJsonObject)
        val closedTicketsListAdapter =
            LineLeaderClosedTicketsListAdapter(viewModel, languageJsonObject)

        binding.repairedTicketsTab.setOnClickListener {
            ticketViewModel.getRepairedTickets(
                selectedLinesIdStr.joinToString(","),
                selectedAreasIdStr.joinToString(",")
            )

            TicketUtil.selectedRepairedTab = "repaired"
            viewModel.isClosedTab = false
            binding.repairedTicketsRecyclerView.visibility = View.VISIBLE
            binding.closedTicketsRecyclerView.visibility = View.INVISIBLE

            binding.repairedTicketsTab.setBackgroundColor(Color.parseColor("#0F75BC"))
            binding.closedTicketsTab.setBackgroundColor(Color.parseColor("#1D5072"))
        }

        binding.closedTicketsTab.setOnClickListener {
            ticketViewModel.getClosedTickets(
                selectedLinesIdStr.joinToString(","),
                selectedAreasIdStr.joinToString(",")
            )

            TicketUtil.selectedRepairedTab = "closed"
            viewModel.isClosedTab = true
            binding.closedTicketsRecyclerView.visibility = View.VISIBLE
            binding.repairedTicketsRecyclerView.visibility = View.INVISIBLE

            binding.closedTicketsTab.setBackgroundColor(Color.parseColor("#0F75BC"))
            binding.repairedTicketsTab.setBackgroundColor(Color.parseColor("#1D5072"))
        }

        if (TicketUtil.selectedRepairedTab.isNotBlank()) {
            when (TicketUtil.selectedRepairedTab) {

                "repaired" -> {
                    binding.repairedTicketsRecyclerView.visibility = View.VISIBLE
                    binding.closedTicketsRecyclerView.visibility = View.INVISIBLE

                    binding.repairedTicketsTab.setBackgroundColor(Color.parseColor("#0F75BC"))
                    binding.closedTicketsTab.setBackgroundColor(Color.parseColor("#1D5072"))
                }

                "closed" -> {
                    binding.closedTicketsRecyclerView.visibility = View.VISIBLE
                    binding.repairedTicketsRecyclerView.visibility = View.INVISIBLE

                    binding.closedTicketsTab.setBackgroundColor(Color.parseColor("#0F75BC"))
                    binding.repairedTicketsTab.setBackgroundColor(Color.parseColor("#1D5072"))
                }

            }
        }

        binding.repairedTicketsRecyclerView.layoutManager = LinearLayoutManager(activity)
        binding.repairedTicketsRecyclerView.adapter = repairedTicketsListAdapter

        binding.closedTicketsRecyclerView.layoutManager = LinearLayoutManager(activity)
        binding.closedTicketsRecyclerView.adapter = closedTicketsListAdapter

        ticketViewModel.repairedTickets.observe(viewLifecycleOwner, Observer { repairedTickets ->
            if (repairedTickets != null) {

//                val filtered = reportedTickets.filter { it.ticketNo == "R0507200429" }

                repairedTicketsListAdapter.data = repairedTickets
            }
        })

        ticketViewModel.closedTickets.observe(viewLifecycleOwner, Observer { closedTickets ->
            if (closedTickets != null) {

//                val filtered = reportedTickets.filter { it.ticketNo == "R0507200429" }

                closedTicketsListAdapter.data = closedTickets
            }
        })

        binding.btnCreateTicket.setOnClickListener {
            action = "create_ticket"
            nfcViewModel.setNFCAction(NFCAction.CREATE_TICKET)
        }

        viewModel.mfgLinesFromDatabase.observe(viewLifecycleOwner, Observer { mfgLines ->

            mfgLines.filter { it.checked ?: false }.forEach {
                selectedLinesStr.add(it.mfgLine)
                selectedLinesIdStr.add(it.mfgLineId.toString())
            }

            Log.d(TAG, "onCreateView: selected id: ${selectedLinesIdStr.joinToString(",")}")

            dashboardViewModel.sharedAreasNoLines.map {
                selectedAreasIdStr.add(it.id.toString())
                it.id ?: ""

            }
            ticketViewModel.getRepairedTickets(
                selectedLinesIdStr.joinToString(","),
                selectedAreasIdStr.joinToString(",")
            )
            ticketViewModel.getClosedTickets(
                selectedLinesIdStr.joinToString(","),
                selectedAreasIdStr.joinToString(",")
            )

        })

        ConnectionUtil.internetConnected.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it) {

                    ticketViewModel.getRepairedTickets(selectedLinesIdStr.joinToString(","))

                    ConnectionUtil.setInternetConnected(false)
                }
                ConnectionUtil.setInternetConnectedComplete()
            }
        })

        mainViewModel.nfcFromDatabase.observe(viewLifecycleOwner, Observer { nfc ->
            if (nfc.rfid.isNotBlank() && nfc.new) {

                machineViewModel.getMachineByRfid(nfc.rfid)

                mainViewModel.insertToNfcDatabase("", false)
            }

        })

        ticketViewModel.snackBarActionsFromDatabase.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it.isNotEmpty()) {

                    when (it[0].action) {
                        SNACK_BAR_ACTION_CREATE_TICKET -> {
                            if (it[0].show) {
                                val message =
                                    languageJsonObject.getTranslation("Successfully created ticket")
                                binding.coordinatorLayout.showSnackbar(message)
                            }
                        }


                    }

                }
                ticketViewModel.finishInsertToSnackBarActionDatabase()
            }
        })

        machineViewModel.machineDetailsByMachineNo.observe(viewLifecycleOwner, Observer { machine ->
            Log.d(TAG, "onCreateView: machine: $machine")
            if (machine != null) {

                when (action) {
                    "create_ticket" -> {
                        loadedMachine = machine.machine
                        loadedMachineId = machine.id
                        loadedMachineMfgLine = machine.mfgLine ?: ""
                        loadedMachineStation = machine.station

                        Log.d(TAG, "onCreateView: machine.mfgLine: ${machine.mfgLine}")
                        Log.d(TAG, "onCreateView: selectedLinesStr: $selectedLinesStr")
                        if (MachineUtil.machineFound) {
                            if (selectedLinesStr.any { it == machine.mfgLine }) {

                                if (machine.hasOpenTicket) {
                                    dismissPopup()
                                    binding.coordinatorLayout.showSnackbar(
                                        languageJsonObject.getTranslation(
                                            "This machine has an active ticket"
                                        )
                                    )
                                } else {
                                    ticketViewModel.getMachineProblems(machine.id)
                                }

                            } else {

                                val dm = DisplayMetrics()
                                activity?.windowManager?.defaultDisplay?.getMetrics(dm)

                                val width = (dm.widthPixels * .9).toInt()
                                val height = (dm.heightPixels * .5).toInt()

                                dismissPopup()
                                popupWindow = showErrorPopupWindow()
                                popupWindow?.isOutsideTouchable = true
                                popupWindow?.isFocusable = true
                                popupWindow?.update(0, 0, width, height)
                                popupWindow?.showAtLocation(binding.root, Gravity.CENTER, 0, -25)

                                popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))


                            }
                        } else {
                            dismissPopup()
                        }
                    }
                    "query_machine" -> {
                        dismissPopup()
                        navigateToQueryMachine(machine.id, machine.machine)
                    }
                    "send_request" -> {
                        dismissPopup()

                        navigateToSendRequest(
                            machine.id,
                            machine.machine,
                            machine.rfid ?: "",
                            machine.subtype ?: "",
                            machine.mfgLineId ?: 0
                        )

//                        if (selectedLinesStr.any { it == machine.mfgLine }) {
//
//                        } else {
//                            val dm = DisplayMetrics()
//                            activity?.windowManager?.defaultDisplay?.getMetrics(dm)
//
//                            val width = (dm.widthPixels * .9).toInt()
//                            val height = (dm.heightPixels * .5).toInt()
//
//                            dismissPopup()
//                            popupWindow = showErrorPopupWindow()
//                            popupWindow?.isOutsideTouchable = true
//                            popupWindow?.isFocusable = true
//                            popupWindow?.update(0, 0, width, height)
//                            popupWindow?.showAtLocation(binding.root, Gravity.CENTER, 0, -25)
//
//                            popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
//                        }
                    }
                }

                machineViewModel.machineDetailsByMachineNoComplete()
            }
        })

        ticketViewModel.commonProblems.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                dismissPopup()
                if (it.isNotEmpty()) {
                    navigateToCreateTicket(
                        loadedMachineId,
                        loadedMachine,
                        loadedMachineStation,
                        loadedMachineMfgLine ?: "",
                        it.size.toLong(),
                        "repaired"
                    )
                } else {
                    navigateToCreateTicket(
                        loadedMachineId,
                        loadedMachine,
                        loadedMachineStation,
                        loadedMachineMfgLine ?: "",
                        0,
                        "repaired"
                    )
                }

                ticketViewModel.commonProblemsComplete()
            }
        })

        machineViewModel.status.observe(viewLifecycleOwner, Observer {
            when (it) {
                ApiStatus.LOADING -> {
                    binding.progressBar.showProgressBar(true)
                }
                else -> {
                    binding.progressBar.showProgressBar(false)
                }
            }
        })

        machineViewModel.machineStatus.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                when (it) {
                    MachineStatus.FOUND -> {

                    }

                    MachineStatus.NOT_FOUND -> {

                        dismissPopup()

                        if (MachineUtil.message.isNotBlank()) {
                            binding.coordinatorLayout.showSnackbar(
                                languageJsonObject.getTranslation(
                                    MachineUtil.message.replace(".", "")
                                )
                            )
                        } else {
                            binding.coordinatorLayout.showSnackbar(
                                languageJsonObject.getTranslation(
                                    "Machine number not found"
                                )
                            )
                        }


//                        val dm = DisplayMetrics()
//                        activity?.windowManager?.defaultDisplay?.getMetrics(dm)
//
//                        val width = (dm.widthPixels * .9).toInt()
//                        val height = (dm.heightPixels * .5).toInt()
//
//                        dismissPopup()
//                        popupWindow = showErrorPopupWindow()
//                        popupWindow?.isOutsideTouchable = true
//                        popupWindow?.isFocusable = true
//                        popupWindow?.update(0, 0, width, height)
//                        popupWindow?.showAtLocation(binding.root, Gravity.CENTER, 0, -25)
//
//                        popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
                    }
                }
                machineViewModel.machineStatusComplete()
            }
        })

        viewModel.navigateToTicketPreview.observe(viewLifecycleOwner, Observer {
            if (it != null) {

//                ticketViewModel.getTicketDetailsById(it.id)
                ticketViewModel.getTicketDetailsByTicketNo(it.ticketNo)
                viewModel.isAlreadyGoToDetail = true
                viewModel.navigateToTicketPreviewComplete()
            }
        })

        viewModel.navigateToClosedTicketPreview.observe(viewLifecycleOwner, Observer {
            if (it != null) {

//                ticketViewModel.getTicketDetailsById(it.id)
                ticketViewModel.getTicketDetailsByTicketNo(it.ticketNo)
                viewModel.isAlreadyGoToDetail = true
                viewModel.navigateToClosedTicketPreviewComplete()
            }
        })

        ticketViewModel.snackBarActionsFromDatabase.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it.isNotEmpty()) {
                    when (it[0].action) {
                        SNACK_BAR_ACTION_CLOSE_TICKET -> {
                            if (it[0].show) {
                                val message = "Repair Ticket has been closed"
                                binding.coordinatorLayout.showSnackbar(
                                    languageJsonObject.getTranslation(
                                        message
                                    )
                                )
                            }
                        }

                        SNACK_BAR_ACTION_REOPEN_TICKET -> {
                            if (it[0].show) {
                                val message = "Repair Ticket has been reopened"
                                binding.coordinatorLayout.showSnackbar(
                                    languageJsonObject.getTranslation(
                                        message
                                    )
                                )
                            }
                        }

                    }

                }
                ticketViewModel.finishInsertToSnackBarActionDatabase()
            }
        })

        ticketViewModel.ticket.observe(viewLifecycleOwner, Observer { ticket ->
            if (ticket != null) {

                var imageAttachmentUrl1 = ""
                var imageAttachmentUrl2 = ""
                var imageAttachmentUrl3 = ""
                var videoAttachmentUrl = ""
                var videoAttachmentUrl2 = ""

                ticket.ticketAsset?.let { assets ->

                    val videoAssets = assets.filter { it.link.contains(".mp4") }
                    val imageAssets =
                        assets.filter { it.link.contains(".png") || it.link.contains(".jpg") }
                            .sortedByDescending { it.id }


                    for ((index, videoAsset) in videoAssets.withIndex()) {

                        when (index) {
                            0 -> {
                                videoAttachmentUrl = videoAsset.link
                            }
                            1 -> {
                                videoAttachmentUrl2 = videoAsset.link
                            }
                        }

                    }

                    for ((index, imageAsset) in imageAssets.withIndex()) {

                        when (index) {

                            0 -> {
                                imageAttachmentUrl1 = imageAsset.link
                            }

                            1 -> {
                                imageAttachmentUrl2 = imageAsset.link
                            }

                            2 -> {
                                imageAttachmentUrl3 = imageAsset.link
                            }

                        }

                    }

                }

                navigateToTicketPreview(
                    ticket.id,
                    ticket.ticketNo,
                    ticket.machineNo,
                    ticket.problem,
                    ticket.remarks,
                    ticket.solution,
                    ticket.place,
                    ticket.reportedPlace,
                    imageAttachmentUrl1,
                    imageAttachmentUrl2,
                    imageAttachmentUrl3,
                    videoAttachmentUrl,
                    videoAttachmentUrl2,
                    ticket.status,
                    ticket.machineId,
                    ticket.problemTypeId,
                    ticket.solutionTypeId ?: 0,
                    DateUtil.formatToDateAndTime(ticket.reported),
                    DateUtil.formatToDateAndTime(ticket.grabbedDt),
                    DateUtil.formatToDateAndTime(ticket.repairedDt),
                    DateUtil.formatToDateAndTime(ticket.closedDt),
                    ticket.elapsedDuration,
                    ticket.reported,
                    ticket.nextMaintDate
                )

                ticketViewModel.ticketComplete()
            }
        })


        ticketViewModel.status.observe(viewLifecycleOwner, Observer {
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

    private fun showErrorPopupWindow(): PopupWindow {

        val inflater =
            activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupLineLeaderErrorPopupNotOnLineBinding.inflate(inflater)

        // Start translation
        with(languageJsonObject) {
            with(binding) {
                labelMachineNotAssigned.text =
                    getTranslation(labelMachineNotAssigned.text.toString())
                labelWhatDoYouWant.text = getTranslation(labelWhatDoYouWant.text.toString())
                btnScanAgain.text = getTranslation(btnScanAgain.text.toString())
                btnCancelLineSetup3.text = getTranslation(btnCancelLineSetup3.text.toString())
            }
        }
        // End translation

        binding.btnScanAgain.setOnClickListener {
            dismissPopup()
            mainViewModel.insertToNfcDeviceDatabase(true)
            showFindMachineDialog { machineNo ->
                machineViewModel.getMachineByMachineNo(machineNo)
            }
        }

        binding.btnCancelLineSetup3.setOnClickListener {
            dismissPopup()
        }

        return PopupWindow(
            binding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun dismissPopup() {
        findMachineBsDialog?.dismiss()
        popupWindow?.let {
            if (it.isShowing) {
                it.dismiss()
            }
            popupWindow = null
        }
    }

    private fun navigateToCreateTicket(
        machineId: Long,
        machine: String,
        station: String,
        mfgLine: String,
        commonProblems: Long,
        origin: String
    ) {
        val action =
            LineLeaderRepairedTicketsFragmentDirections.actionLineLeaderRepairedTicketsFragmentToCreateTicketFragment(
                machineId,
                machine,
                station,
                mfgLine,
                commonProblems,
                origin
            )
        navigate(action)
    }

    private fun navigateToTicketPreview(
        ticketId: Long,
        ticketNo: String,
        machineNo: String,
        problem: String,
        remarks: String,
        solution: String,
        place: String,
        reportedPlace: String,
        imageAttachmentUrl1: String,
        imageAttachmentUrl2: String,
        imageAttachmentUrl3: String,
        videoAttachmentUrl: String,
        videoAttachmentUrl2: String?,
        status: String,
        machineId: Long,
        problemTypeId: Long,
        solutionTypeId: Long,
        reportedTime: String,
        grabbedTime: String,
        repairedTime: String,
        closedTime: String,
        elapsedDuration: String,
        reported: Date?,
        nextMainDate: Date?
    ) {
        val action = LineLeaderRepairedTicketsFragmentDirections
            .actionLineLeaderRepairedTicketsFragmentToLineLeaderTicketPreviewFragment(
                ticketId,
                ticketNo,
                machineNo,
                problem,
                remarks,
                solution,
                place,
                reportedPlace,
                imageAttachmentUrl1,
                imageAttachmentUrl2,
                imageAttachmentUrl3,
                videoAttachmentUrl,
                videoAttachmentUrl2,
                status,
                machineId,
                problemTypeId,
                solutionTypeId,
                reportedTime,
                grabbedTime,
                repairedTime,
                closedTime,
                elapsedDuration,
                NextMainDate(reported, nextMainDate)
            )
        navigate(action)
    }

    private fun navigateToInRepairTickets() {
        val action = LineLeaderRepairedTicketsFragmentDirections
            .actionLineLeaderRepairedTicketsFragmentToLineLeaderInRepairTicketsFragment()
        navigate(action)
    }

    private fun navigateToReportedTickets() {
        val action = LineLeaderRepairedTicketsFragmentDirections
            .actionLineLeaderRepairedTicketsFragmentToLineLeaderReportedTicketsFragment()
        navigate(action)
    }

    private fun navigateToHome() {
        val action = LineLeaderRepairedTicketsFragmentDirections
            .actionLineLeaderRepairedTicketsFragmentToLineLeaderHomeFragment()
        navigate(action)
    }

    private fun navigateToQueryMachine(machineId: Long, machine: String) {
        val action = LineLeaderRepairedTicketsFragmentDirections
            .actionLineLeaderRepairedTicketsFragmentToQueryMachineFragment(machineId, machine)
        navigate(action)
    }

    private fun navigateToNotification() {
        val action = LineLeaderRepairedTicketsFragmentDirections
            .actionLineLeaderRepairedTicketsFragmentToNotificationFragment()
        navigate(action)
    }

    private fun navigateToSendRequest(
        machineId: Long,
        machine: String,
        rfid: String,
        subType: String,
        mfgLineId: Long
    ) {
        val action = LineLeaderRepairedTicketsFragmentDirections
            .actionLineLeaderRepairedTicketsFragmentToSendRequestFragment(
                machineId,
                machine,
                rfid,
                subType,
                mfgLineId
            )

        navigate(action)
    }

    private fun navigateToChangePassword() {
        val action = LineLeaderRepairedTicketsFragmentDirections
            .actionLineLeaderRepairedTicketsFragmentToChangePasswordFragment()
        navigate(action)
    }

    private fun navigateToChangeFactory() {
        val action = LineLeaderRepairedTicketsFragmentDirections
            .actionLineLeaderRepairedTicketsFragmentToChangeFactoryFragment()
        navigate(action)
    }

    private fun navigateToChangeLanguage() {
        val action = LineLeaderRepairedTicketsFragmentDirections
            .actionLineLeaderRepairedTicketsFragmentToChangeLanguageFragment()
        navigate(action)
    }

}
