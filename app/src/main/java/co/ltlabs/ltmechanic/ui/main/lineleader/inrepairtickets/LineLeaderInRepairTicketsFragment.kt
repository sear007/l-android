package co.ltlabs.ltmechanic.ui.main.lineleader.inrepairtickets

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
import co.ltlabs.ltmechanic.databinding.FragmentLineLeaderInRepairTicketsBinding
import co.ltlabs.ltmechanic.databinding.PopupLineLeaderErrorPopupNotOnLineBinding
import co.ltlabs.ltmechanic.domain.NextMainDate
import co.ltlabs.ltmechanic.ui.adapter.LineLeaderInRepairTicketsListAdapter
import co.ltlabs.ltmechanic.ui.main.BaseFragment
import co.ltlabs.ltmechanic.ui.main.DashboardViewModel
import co.ltlabs.ltmechanic.ui.main.main_helper.NFCViewModel
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.util.nfc.NFCAction
import co.ltlabs.ltmechanic.viewmodels.main.lineleader.LineLeaderInRepairTicketsViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.MachineViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.TicketViewModel
import org.json.JSONObject
import java.util.*
import javax.inject.Inject

private const val TAG = "LLInRepairFragment";

class LineLeaderInRepairTicketsFragment : BaseFragment() {

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val viewModel: LineLeaderInRepairTicketsViewModel by viewModels { providerFactory }
    private val ticketViewModel: TicketViewModel by viewModels { providerFactory }
    private val machineViewModel: MachineViewModel by viewModels { providerFactory }
    private val nfcViewModel: NFCViewModel by activityViewModels()
    private val dashboardViewModel: DashboardViewModel by activityViewModels { providerFactory }

    private var popupWindow: PopupWindow? = null

    private var selectedLinesStr = mutableListOf<String>()
    private var selectedLinesIdStr = mutableListOf<String>()

    private var loadedMachine = ""
    private var loadedMachineId = 0L
    private var loadedMachineMfgLine = ""
    private var loadedMachineStation = ""

    private lateinit var coordinatorLayout: CoordinatorLayout

    private var action = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentLineLeaderInRepairTicketsBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.jTranslate = languageJsonObject
        binding.viewModel = viewModel

        coordinatorLayout = binding.coordinatorLayout

        val adapter = LineLeaderInRepairTicketsListAdapter(viewModel, languageJsonObject)

        binding.recyclerView.adapter = adapter

        ticketViewModel.inRepairTickets.observe(viewLifecycleOwner) { reportedTickets ->
            if (reportedTickets != null) {
                adapter.data = reportedTickets
            }
        }

        viewModel.mfgLinesFromDatabase.observe(viewLifecycleOwner, Observer { mfgLines ->

            mfgLines.filter { it.checked ?: false }.forEach {
                selectedLinesStr.add(it.mfgLine)
                selectedLinesIdStr.add(it.mfgLineId.toString())
            }
            val areas = dashboardViewModel.sharedAreasNoLines.map {
                it.id ?: ""
            }
            ticketViewModel.getInRepairTickets(selectedLinesIdStr.joinToString(","), areas.joinToString(","))

        })

        ConnectionUtil.internetConnected.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it) {

                    ticketViewModel.getInRepairTickets(selectedLinesIdStr.joinToString(","))

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
                            (machine.mfgLineId ?: 0).toLong()
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
                        "inrepair"
                    )
                } else {
                    navigateToCreateTicket(
                        loadedMachineId,
                        loadedMachine,
                        loadedMachineStation,
                        loadedMachineMfgLine ?: "",
                        0,
                        "inrepair"
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

        binding.btnCreateTicket.setOnClickListener {
            action = "create_ticket"
            nfcViewModel.setNFCAction(NFCAction.CREATE_TICKET)
        }

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

        viewModel.navigateToTicketPreview.observe(viewLifecycleOwner, Observer {
            if (it != null) {

//                ticketViewModel.getTicketDetailsById(it.id)
                ticketViewModel.getTicketDetailsByTicketNo(it.ticketNo)

                viewModel.navigateToTicketPreviewComplete()
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
        nextMaintDate: Date?
    ) {
        val action = LineLeaderInRepairTicketsFragmentDirections
            .actionLineLeaderInRepairTicketsFragmentToLineLeaderTicketPreviewFragment(
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
                NextMainDate(reported, nextMaintDate)
            )
        navigate(action)
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
            LineLeaderInRepairTicketsFragmentDirections.actionLineLeaderInRepairTicketsFragmentToCreateTicketFragment(
                machineId,
                machine,
                station,
                mfgLine,
                commonProblems,
                origin
            )
        navigate(action)
    }

    private fun navigateToReportedTickets() {
        val action = LineLeaderInRepairTicketsFragmentDirections
            .actionLineLeaderInRepairTicketsFragmentToLineLeaderReportedTicketsFragment()
        navigate(action)
    }

    private fun navigateToRepairedTickets() {
        val action = LineLeaderInRepairTicketsFragmentDirections
            .actionLineLeaderInRepairTicketsFragmentToLineLeaderRepairedTicketsFragment()
        navigate(action)
    }

    private fun navigateToHome() {
        val action = LineLeaderInRepairTicketsFragmentDirections
            .actionLineLeaderInRepairTicketsFragmentToLineLeaderHomeFragment()
        navigate(action)
    }

    private fun navigateToQueryMachine(machineId: Long, machine: String) {
        val action = LineLeaderInRepairTicketsFragmentDirections
            .actionLineLeaderInRepairTicketsFragmentToQueryMachineFragment(machineId, machine)
        navigate(action)
    }

    private fun navigateToNotification() {
        val action = LineLeaderInRepairTicketsFragmentDirections
            .actionLineLeaderInRepairTicketsFragmentToNotificationFragment()
        navigate(action)
    }

    private fun navigateToSendRequest(
        machineId: Long,
        machine: String,
        rfid: String,
        subType: String,
        mfgLineId: Long
    ) {
        val action = LineLeaderInRepairTicketsFragmentDirections
            .actionLineLeaderInRepairTicketsFragmentToSendRequestFragment(
                machineId,
                machine,
                rfid,
                subType,
                mfgLineId
            )

        navigate(action)
    }

    private fun navigateToChangePassword() {
        val action = LineLeaderInRepairTicketsFragmentDirections
            .actionLineLeaderInRepairTicketsFragmentToChangePasswordFragment()
        navigate(action)
    }

    private fun navigateToChangeFactory() {
        val action = LineLeaderInRepairTicketsFragmentDirections
            .actionLineLeaderInRepairTicketsFragmentToChangeFactoryFragment()
        navigate(action)
    }

    private fun navigateToChangeLanguage() {
        val action = LineLeaderInRepairTicketsFragmentDirections
            .actionLineLeaderInRepairTicketsFragmentToChangeLanguageFragment()
        navigate(action)
    }

}
