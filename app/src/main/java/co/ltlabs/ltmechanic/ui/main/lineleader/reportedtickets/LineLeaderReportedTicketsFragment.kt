package co.ltlabs.ltmechanic.ui.main.lineleader.reportedtickets

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
import android.widget.ProgressBar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.constant.type.TicketType
import co.ltlabs.ltmechanic.databinding.FragmentLineLeaderReportedTicketsBinding
import co.ltlabs.ltmechanic.databinding.PopupLineLeaderErrorPopupNotOnLineBinding
import co.ltlabs.ltmechanic.domain.NextMainDate
import co.ltlabs.ltmechanic.ui.adapter.LineLeaderReportedTicketsListAdapter
import co.ltlabs.ltmechanic.ui.main.BaseFragment
import co.ltlabs.ltmechanic.ui.main.DashboardViewModel
import co.ltlabs.ltmechanic.ui.main.main_helper.NFCViewModel
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.util.nfc.NFCAction
import co.ltlabs.ltmechanic.viewmodels.main.lineleader.LineLeaderReportedTicketsViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.MachineViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.TicketViewModel
import org.json.JSONObject
import java.util.*
import javax.inject.Inject

private const val TAG = "LLReportedTickets";

class LineLeaderReportedTicketsFragment : BaseFragment() {

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val args: LineLeaderReportedTicketsFragmentArgs by navArgs()
    private val viewModel: LineLeaderReportedTicketsViewModel by viewModels { providerFactory }
    private val machineViewModel: MachineViewModel by viewModels { providerFactory }
    private val ticketViewModel: TicketViewModel by viewModels { providerFactory }
    private val nfcViewModel: NFCViewModel by activityViewModels()
    private val dashboardViewModel: DashboardViewModel by activityViewModels { providerFactory }

    private lateinit var ticketType: TicketType
    private var popupWindow: PopupWindow? = null

    private var selectedLinesStr = mutableListOf<String>()
    private var selectedLinesIdStr = mutableListOf<String>()

    private var loadedMachine = ""
    private var loadedMachineId = 0L
    private var loadedMachineMfgLine = ""
    private var loadedMachineStation = ""

    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var progressBar: ProgressBar
    lateinit var binding: FragmentLineLeaderReportedTicketsBinding

    private var action = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentLineLeaderReportedTicketsBinding.inflate(inflater)

        progressBar = binding.progressBar

        binding.lifecycleOwner = viewLifecycleOwner
        binding.jTranslate = languageJsonObject
        binding.viewModel = viewModel

        val adapter = LineLeaderReportedTicketsListAdapter(viewModel, languageJsonObject)

        coordinatorLayout = binding.coordinatorLayout

        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.adapter = adapter

        binding.btnCreateTicket.setOnClickListener {
            action = "create_ticket"
            nfcViewModel.setNFCAction(NFCAction.CREATE_TICKET)
        }

        mainViewModel.nfcFromDatabase.observe(viewLifecycleOwner) { nfc ->
            if (nfc.rfid.isNotBlank() && nfc.new) {

                machineViewModel.getMachineByRfid(nfc.rfid)

                mainViewModel.insertToNfcDatabase("", false)
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
            ticketViewModel.getReportedTickets(selectedLinesIdStr.joinToString(","), areas.joinToString(","))

        })

        ConnectionUtil.internetConnected.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it) {

                    ticketViewModel.getReportedTickets(selectedLinesIdStr.joinToString(","))

                    ConnectionUtil.setInternetConnected(false)
                }
                ConnectionUtil.setInternetConnectedComplete()
            }
        })

        machineViewModel.machineDetailsByMachineNo.observe(viewLifecycleOwner, Observer { machine ->
            Log.d(TAG, "onCreateView: machine: $machine")
            if (machine != null) {

                Log.d(TAG, "onCreateView: action: $action")

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
                        "reported"
                    )
                } else {
                    navigateToCreateTicket(
                        loadedMachineId,
                        loadedMachine,
                        loadedMachineStation,
                        loadedMachineMfgLine ?: "",
                        0,
                        "reported"
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

        ticketViewModel.reportedTickets.observe(viewLifecycleOwner, Observer { reportedTickets ->
            viewModel.getReportedOrReopenedTicket(ticketType, reportedTickets)
        })

        viewModel.reportedTickets.observe(viewLifecycleOwner) {
            adapter.data = it
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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolbarTitle()
    }

    private fun setToolbarTitle() {
        /**
         * check if click from reopen or reported to change title
         */
        ticketType = TicketType.fromCodeToType(args.ticketType)
        if (ticketType is TicketType.Reported) {
            binding.toolBarTitleTextView.text =
                languageJsonObject.getTranslation(getString(R.string.reported_tickets))
        } else if (ticketType is TicketType.Reopen) {
            binding.toolBarTitleTextView.text =
                languageJsonObject.getTranslation(getString(R.string.reopened_tickets))
        }
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
        reportedDate: Date?,
        nextMainDate: Date?
    ) {
        val action = LineLeaderReportedTicketsFragmentDirections
            .actionLineLeaderReportedTicketsFragmentToLineLeaderTicketPreviewFragment(
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
                NextMainDate(reportedDate, nextMainDate)
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
            LineLeaderReportedTicketsFragmentDirections.actionLineLeaderReportedTicketsFragmentToCreateTicketFragment(
                machineId,
                machine,
                station,
                mfgLine,
                commonProblems,
                origin
            )
        navigate(action)
    }

    private fun navigateToQueryMachine(machineId: Long, machine: String) {
        val action = LineLeaderReportedTicketsFragmentDirections
            .actionLineLeaderReportedTicketsFragmentToQueryMachineFragment(machineId, machine)
        navigate(action)
    }

    private fun navigateToSendRequest(
        machineId: Long,
        machine: String,
        rfid: String,
        subType: String,
        mfgLineId: Long
    ) {
        val action = LineLeaderReportedTicketsFragmentDirections
            .actionLineLeaderReportedTicketsFragmentToSendRequestFragment(
                machineId,
                machine,
                rfid,
                subType,
                mfgLineId
            )

        navigate(action)
    }

}
