package co.ltlabs.ltmechanic.ui.main.mechanic.reportedtickets

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.MediaController
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.constant.TicketType
import co.ltlabs.ltmechanic.constant.type.AccessType
import co.ltlabs.ltmechanic.databinding.*
import co.ltlabs.ltmechanic.di.AppModule
import co.ltlabs.ltmechanic.domain.Ticket
import co.ltlabs.ltmechanic.ui.main.BaseFragment
import co.ltlabs.ltmechanic.ui.main.ReopenTicketAdapter
import co.ltlabs.ltmechanic.ui.main.Resource
import co.ltlabs.ltmechanic.ui.main.main_helper.NFCViewModel
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.viewmodels.PerAccessViewModel
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.MechanicReportedTicketsPreviewViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.MachineViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.TicketViewModel
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class MechanicReportedTicketsPreviewFragment : BaseFragment() {

    @Inject
    lateinit var languageJsonObject: JSONObject

    @Inject
    lateinit var requestManager: RequestManager

    @Inject
    lateinit var reopenTicketAdapter: ReopenTicketAdapter

    private var requiredMachineVerification = "Y"

    private val viewModel: MechanicReportedTicketsPreviewViewModel by lazy {
        ViewModelProvider(
            this, providerFactory
        ).get(MechanicReportedTicketsPreviewViewModel::class.java)
    }

    private val perAccessViewModel: PerAccessViewModel by viewModels { providerFactory }

    private val ticketViewModel: TicketViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(TicketViewModel::class.java)
    }

    private val machineViewModel: MachineViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MachineViewModel::class.java)
    }

    private val nfcViewModel: NFCViewModel by activityViewModels()

    private var currentTicket: Ticket? = null

    private lateinit var binding: FragmentMechanicReportedTicketsPreviewBinding

    private val args: MechanicReportedTicketsPreviewFragmentArgs by navArgs()

    private var popupWindow: PopupWindow? = null

    var macSubTypeId = 0L
    var brandId = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        binding = FragmentMechanicReportedTicketsPreviewBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        machineViewModel.getMachineByMachineNo(args.machineNo)

        ticketViewModel.getTicketDetailsByTicketNo(args.ticketNo)

        // Start translation
        with(languageJsonObject) {
            with(binding) {
                ticketNoLabel.text = getTranslation(ticketNoLabel.text.toString())
                machineLabel.text = getTranslation(machineLabel.text.toString())
                problemLabel.text = getTranslation(problemLabel.text.toString())
                solutionLabel.text = getTranslation(solutionLabel.text.toString())
                remarksLabel.text = getTranslation(remarksLabel.text.toString())
                reportedPlaceLabel.text = getTranslation(reportedPlaceLabel.text.toString())
                currentPlaceLabel.text = getTranslation(currentPlaceLabel.text.toString())

                reportedTimeLabel.text = getTranslation(reportedTimeLabel.text.toString())
                grabbedTimeLabel.text = getTranslation(grabbedTimeLabel.text.toString())
                repairedTimeLabel.text = getTranslation(repairedTimeLabel.text.toString())
                closedTimeLabel.text = getTranslation(closedTimeLabel.text.toString())
                totalDurationLabel.text = getTranslation(totalDurationLabel.text.toString())

                attachmentsLabel.text = getTranslation(attachmentsLabel.text.toString())
                btnOption.text = getTranslation(btnOption.text.toString())
                btnNext.text = getTranslation(btnNext.text.toString())
            }
        }
        // End translation

        ticketViewModel.ticket.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                currentTicket = it
                when (TicketType.fromCodeToType(it.status)) {
                    is TicketType.Reported -> {
                        val status = languageJsonObject.getTranslation(it.status)
                        val color = ContextCompat.getColor(requireContext(), R.color.colorReported)
                        binding.statusTextView.text = status
                        binding.statusTextView2.text = status
                        binding.statusTextView.setTextColor(color)
                        binding.statusTextView2.setTextColor(color)
                        binding.statusTextView.setBackgroundResource(0)
                        binding.statusTextView2.setBackgroundResource(0)
                    }
                    is TicketType.Reopen -> {
                        val status = languageJsonObject.getTranslation(args.status)
                        val color = ContextCompat.getColor(requireContext(), R.color.colorWhite)
                        binding.statusTextView.text = status
                        binding.statusTextView2.text = status
                        binding.statusTextView.setBackgroundResource(R.drawable.bg_reopen_ticket)
                        binding.statusTextView2.setBackgroundResource(R.drawable.bg_reopen_ticket)
                        binding.statusTextView.setTextColor(color)
                        binding.statusTextView2.setTextColor(color)
                    }
                    else -> {
                        binding.btnNext.text = languageJsonObject.getTranslation("FINISH")
                        binding.btnNext.tag = "FINISH"
                        binding.statusTextView.text = languageJsonObject.getTranslation("IN REPAIR")
                        binding.statusTextView.setTextColor(Color.parseColor("#F59A23"))
                        binding.statusTextView2.text =
                            languageJsonObject.getTranslation("IN REPAIR")
                        binding.statusTextView2.setTextColor(Color.parseColor("#F59A23"))
                        binding.statusTextView.setBackgroundResource(0)
                        binding.statusTextView2.setBackgroundResource(0)
                    }
                }
            }
        })

        machineViewModel.machineDetailsByMachineNo.observe(viewLifecycleOwner,
            Observer { machineByNo ->

                if (machineByNo != null) {

                    macSubTypeId = (machineByNo.macSubTypeId ?: 0).toLong()
                    brandId = machineByNo.brandId

                    machineViewModel.machineDetailsByMachineNoComplete()
                }

            })

        val baseUrl =
            "${AppModule.scheme}://${AppModule.host}$FILE_API_ADDED_URL/api/files/by-name?path="

        binding.toolBarTitleTextView.text = args.ticketNo
        binding.ticketNo.text = args.ticketNo
        binding.machine.text = args.machineNo
        binding.problem.text = if (args.problem.isNotBlank()) {
            languageJsonObject.getTranslation(args.problem)
        } else {
            "-"
        }
        binding.currentPlace.text = args.place
        binding.reportedPlace.text = args.reportedPlace

        binding.reportedTime.text = if (args.reportedTime.isNotEmpty()) {
            DateTimeUtil.parseWithTimeZone(args.nextMainDate?.reportedDate)
        } else "-"

        binding.grabbedTime.text = args.grabbedTime

        binding.repairedTime.text = args.repairedTime

        binding.closedTime.text = args.closedTime

        binding.totalDuration.text = args.elapsedDuration

        binding.remarks.text = args.remarks.ifBlank { "-" }
        binding.solution.text = args.solution.ifBlank { "-" }

        binding.imageAttachmentTemp.tag = "$baseUrl${args.imageAttachmentUrl1}"
        binding.imageAttachment1.tag = "$baseUrl${args.imageAttachmentUrl1}"
        binding.imageAttachment2.tag = "$baseUrl${args.imageAttachmentUrl2}"
        binding.imageAttachment3.tag = "$baseUrl${args.imageAttachmentUrl3}"
        binding.videoAttachment1.tag = "$baseUrl${args.videoAttachmentUrl}"


        binding.imageAttachmentTemp.setOnClickListener {
            showAssetPopupWindow(binding.root, showImagePreviewPopupWindow(it.tag.toString()))
        }

        binding.imageAttachment1.setOnClickListener {
            showAssetPopupWindow(binding.root, showImagePreviewPopupWindow(it.tag.toString()))
        }

        binding.imageAttachment2.setOnClickListener {
            showAssetPopupWindow(binding.root, showImagePreviewPopupWindow(it.tag.toString()))
        }

        binding.imageAttachment3.setOnClickListener {
            showAssetPopupWindow(binding.root, showImagePreviewPopupWindow(it.tag.toString()))
        }

        binding.videoAttachment1.setOnClickListener {
            showAssetPopupWindow(binding.root, showVideoPreviewPopupWindow(it.tag.toString()))
        }

        if (args.videoAttachmentUrl.isBlank() && args.imageAttachmentUrl1.isNotBlank()) {
            binding.videoAttachment1.visibility = View.INVISIBLE
            binding.imageAttachment1.visibility = View.GONE
            binding.imageAttachmentTemp.visibility = View.VISIBLE
            binding.statusTextView2.visibility = View.INVISIBLE
            binding.statusTextView.visibility = View.VISIBLE

            requestManager.load(binding.imageAttachmentTemp.tag.toString()).apply {
                RequestOptions().override(75, 75)
            }.into(binding.imageAttachmentTemp)


        } else {
            binding.imageAttachment1.visibility = View.VISIBLE
            requestManager.load(binding.imageAttachment1.tag.toString()).apply {
                RequestOptions().override(75, 75)
            }.into(binding.imageAttachment1)
        }

        if (args.imageAttachmentUrl1.isBlank() && args.videoAttachmentUrl.isBlank()) {
            binding.emptyAttachment.visibility = View.VISIBLE

            binding.statusTextView.visibility = View.INVISIBLE
            binding.statusTextView2.visibility = View.VISIBLE
            binding.imageAttachmentTemp.visibility = View.INVISIBLE

        }

        if (args.imageAttachmentUrl1.isBlank()) {
            binding.imageAttachment1.visibility = View.GONE
        }

        binding.imageAttachment2.visibility = if (args.imageAttachmentUrl2.isBlank()) {
            View.GONE
        } else {

            requestManager.load(binding.imageAttachment2.tag.toString()).apply {
                RequestOptions().override(75, 75)
            }.into(binding.imageAttachment2)

            View.VISIBLE
        }

        binding.imageAttachment3.visibility = if (args.imageAttachmentUrl3.isBlank()) {
            View.GONE
        } else {

            requestManager.load(binding.imageAttachment3.tag.toString()).apply {
                RequestOptions().override(75, 75)
            }.into(binding.imageAttachment3)

            View.VISIBLE
        }

        binding.playButtonIcon.visibility = if (args.videoAttachmentUrl.isBlank()) {
            View.GONE
        } else {
            View.VISIBLE
        }

        binding.videoAttachment1.visibility = if (args.videoAttachmentUrl.isBlank()) {

            View.GONE
        } else {

            requestManager.load(binding.videoAttachment1.tag.toString()).apply {
                RequestOptions().override(75, 75)
            }.into(binding.videoAttachment1)

            View.VISIBLE
        }

        ticketViewModel.ticketStatus.observe(viewLifecycleOwner, Observer { ticketStatus ->
            if (ticketStatus != null) {

                when (ticketStatus) {

                    TicketStatus.IN_REPAIR -> {
                        currentTicket?.status = TicketType.IN_REPAIR
                        binding.statusTextView.text = languageJsonObject.getTranslation("IN REPAIR")
                        binding.statusTextView2.text =
                            languageJsonObject.getTranslation("IN REPAIR")
                        val color = ContextCompat.getColor(requireContext(), R.color.colorInRepair)
                        binding.statusTextView.setTextColor(color)
                        binding.statusTextView2.setTextColor(color)
                        binding.btnNext.text = languageJsonObject.getTranslation("FINISH")
                        binding.btnNext.tag = "FINISH"
                        binding.statusTextView.setBackgroundResource(0)
                        binding.statusTextView2.setBackgroundResource(0)

                    }

                    TicketStatus.CANCELLED -> {
                        dismissPopup()
                        navigateToReportedTickets()
                    }

                }

                ticketViewModel.ticketStatusComplete()
            }
        })

        ticketViewModel.status.observe(viewLifecycleOwner, Observer {
            when (it) {
                ApiStatus.LOADING -> {
                    binding.progressBar.showProgressBar(true)
                    binding.btnNext.isEnabled = false
                }
                else -> {
                    binding.progressBar.showProgressBar(false)
                    binding.btnNext.isEnabled = true
                }
            }
        })

        binding.btnOption.setOnClickListener {
            showPopupWindow(binding.root, showOptionsPopupWindow())
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleReopenTicket()
        setupListener()
        handleVerifyMachine()
        listenerPerAccess()
        maintDate()

        lifecycleScope.launchWhenCreated {
            perAccessViewModel.productConfig.collectLatest {
                when (it) {
                    is com.ltlabs.lt_core.network.Resource.Success -> {
                        val data =
                            it.data?.data?.find { item -> item.config == "ReqMachineVerification" }
                        requiredMachineVerification = data?.value ?: requiredMachineVerification
                    }
                    else -> {

                    }
                }
            }
        }
    }

    private var accessType: AccessType? = null
    private fun listenerPerAccess() {
        perAccessViewModel.getPerAccess()
        lifecycleScope.launch {
            perAccessViewModel.perAccessResponse.collectLatest {
                when (it.status) {
                    Resource.Status.LOADING -> loading.show(requireContext())
                    Resource.Status.SUCCESS -> {
                        accessType = it.data?.access
                        loading.dismiss()
                    }
                    Resource.Status.ERROR -> loading.dismiss()
                }
            }
        }
    }

    private fun maintDate() {
        binding.apply {
            val next = args.nextMainDate?.nextMainDate
            val reported = args.nextMainDate?.reportedDate
            tvMainStatus.isSelected = DateUtil.isMaintDateOverdue(reported, next)
            tvMainStatus.text = if (DateUtil.isMaintDateOverdue(
                    reported,
                    next
                )
            ) languageJsonObject.getTranslation("OVERDUE")
            else languageJsonObject.getTranslation("ON SCHEDULE")
            tvNextMainDate.text = DateUtil.convertMaintDate(next)
        }
    }

    private fun handleReopenTicket() {
        viewModel.getTicketLogs(args.ticketNo)
        val dividerItemDecoration = DividerItemDecoration(requireContext())
        binding.rvReopened.apply {
            removeItemDecoration(dividerItemDecoration)
            addItemDecoration(dividerItemDecoration)
            adapter = reopenTicketAdapter
        }
        viewModel.ticketLogs.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.rvReopened.isVisible = it.isNotEmpty()
                reopenTicketAdapter.submitList(it)
            }
        }
    }

    private fun setupListener() {
        binding.btnNext.setOnClickListener {
            val type = TicketType.fromCodeToType(currentTicket?.status)
            if (type is TicketType.Reported || type is TicketType.Reopen) {
                if (requiredMachineVerification.equals("N", true)) {
                    ticketViewModel.getStatusIdAndUpdateTicketStatus(
                        TicketsStatus.IN_REPAIR,
                        TicketModule.REPAIR,
                        args.ticketNo,
                        remarks = args.remarks
                    )
                } else {
                    nfcViewModel.isObserveOutsideMainActivity = true
                    showFindMachineDialog {
                        viewModel.getMachineByMachineNo(it)
                    }
                }

            } else {
                navigateToConfirm(
                    args.ticketId,
                    args.machineId,
                    args.ticketNo,
                    args.machineNo,
                    args.problem,
                    args.problemTypeId,
                    args.solution,
                    args.solutionTypeId,
                    args.remarks
                )
            }
        }
    }

    private fun handleVerifyMachine() {

        viewModel.machine.observe(viewLifecycleOwner) {
            when (it.status) {
                Resource.Status.LOADING -> loading.show(requireContext())
                Resource.Status.ERROR -> {
                    loading.dismiss()
                    showSnackBar(
                        binding.root, languageJsonObject.getTranslation(it.message.toString())
                    )
                }
                Resource.Status.SUCCESS -> {
                    loading.dismiss()
                    val machine = it.data ?: return@observe
                    if (args.machineNo == machine.machine) {
                        ticketViewModel.getStatusIdAndUpdateTicketStatus(
                            TicketsStatus.IN_REPAIR,
                            TicketModule.REPAIR,
                            args.ticketNo,
                            remarks = args.remarks
                        )
                    } else {
                        showUnmatchedMachineDialog()
                    }
                }

            }
        }

        lifecycleScope.launchWhenCreated {
            nfcViewModel.scanRfid.collectLatest {
                findMachineBsDialog?.dismiss()
                viewModel.getMachineByRfid(it)
            }
        }
    }

    private var dialog: AlertDialog.Builder? = null
    private fun showUnmatchedMachineDialog() {
        if (dialog == null) dialog = AlertDialog.Builder(requireContext())
        val view = DialogUnmatchedMachineBinding.inflate(layoutInflater)
        view.tvTitle.text = languageJsonObject.getTranslation("Unmatched Machine Code")
        view.tvMsg.text = "${languageJsonObject.getTranslation("Please check and try again")}."
        dialog?.apply {
            setView(view.root)
            setPositiveButton("Ok") { d, _ ->
                d.dismiss()
                dialog = null
            }
            show()
        }
    }


    private fun showPopupWindow(view: View, popupWindowType: PopupWindow) {
        val dm = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(dm)

        val width = (dm.widthPixels * .9).toInt()
        val height = (dm.heightPixels * .35).toInt()

        dismissPopup()
        popupWindow = popupWindowType
        popupWindow?.isOutsideTouchable = false

//        popupWindow?.setTouchInterceptor(object : View.OnTouchListener {
//            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
//                event?.let {
//                    if (it.x < 0 || it.x > width) return true
//                    if (it.y < 0 || it.y > height) return true
//                }
//
//                return false
//            }
//
//        })

        popupWindow?.isFocusable = true
        popupWindow?.update(0, 0, width, height)
        popupWindow?.showAtLocation(view, Gravity.CENTER, 0, height)

        popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
    }

    private fun dismissPopup() {
        popupWindow?.let {
            if (it.isShowing) {
                it.dismiss()
            }
            popupWindow = null
        }
    }

    private fun showOptionsPopupWindow(): PopupWindow {

        val inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupOptionMenuBinding.inflate(inflater)

        // Start translation
        with(languageJsonObject) {
            with(binding) {
                btnMachineHistory.text = getTranslation(btnMachineHistory.text.toString())
                btnAlternativeMachine.text = getTranslation(btnAlternativeMachine.text.toString())
                btnCancelTicket.text = getTranslation(btnCancelTicket.text.toString())

                // Hide cancel button whenever configuration from GA don't allow to cancel ticket
                btnCancelTicket.isVisible = accessType == AccessType.FullAccess
                textView12.isVisible = btnCancelTicket.isVisible
            }
        }
        // End translation

        binding.btnMachineHistory.setOnClickListener {
            dismissPopup()
            navigateToMachineHistory(args.machineId)
        }

        binding.btnAlternativeMachine.setOnClickListener {
            dismissPopup()
            navigateToMachineLocations(
                macSubTypeId, brandId, args.brand
            )
        }

        binding.btnCancelTicket.setOnClickListener {
            dismissPopup()

            val dm = DisplayMetrics()
            activity?.windowManager?.defaultDisplay?.getMetrics(dm)

            val width = (dm.widthPixels * .9).toInt()
            val height = (dm.heightPixels * .28).toInt()


            dismissPopup()
            popupWindow = showPopupWindow()
            popupWindow?.isOutsideTouchable = true
            popupWindow?.isFocusable = true
            popupWindow?.update(0, 0, width, height)
            popupWindow?.showAtLocation(it.rootView, Gravity.CENTER, 0, -25)

            popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        }

        return PopupWindow(
            binding.root, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun navigateToConfirm(
        ticketId: Long,
        machineId: Long,
        ticketNo: String,
        machineNo: String,
        problem: String,
        problemTypeId: Long,
        solution: String,
        solutionTypeId: Long,
        remarks: String
    ) {
        val action =
            MechanicReportedTicketsPreviewFragmentDirections.actionMechanicReportedTicketsPreviewFragmentToMechanicReportedTicketsConfirmFragment(
                ticketId,
                machineId,
                ticketNo,
                machineNo,
                problem,
                problemTypeId,
                solution,
                solutionTypeId,
                remarks
            )
        navigate(action)
    }

    private fun showPopupWindow(): PopupWindow {

        val inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.popup_cancel_confirmation_message, null)
        val labelCancel = view.findViewById<TextView>(R.id.labelCancel3)
        val binding = PopupCancelConfirmationMessageBinding.inflate(inflater)

//        labelCancel.text = "testsss"
//        Log.d(TAG, "showPopupWindow: labelCancel.text: ${labelCancel.text}")

        // Start translation
        with(languageJsonObject) {
            with(binding) {
                labelCancel3.text = getTranslation(labelCancel3.text.toString().replace("?", ""))
                btnCancelLineSetup2.text = getTranslation(btnCancelLineSetup2.text.toString())
                btnConfirmLineSetup2.text = getTranslation(btnConfirmLineSetup2.text.toString())
            }
        }
        // End translation

        binding.btnCancelLineSetup2.setOnClickListener {
            dismissPopup()
        }

        binding.btnConfirmLineSetup2.setOnClickListener {

            // TODO uncomment
//            ticketViewModel.updateTicketStatus(args.ticketNo, StatusIdUtil.RT_CANCELLED.toString(), remarks = args.remarks)
            ticketViewModel.getStatusIdAndUpdateTicketStatus(
                TicketsStatus.CANCELLED, TicketModule.REPAIR, args.ticketNo, remarks = args.remarks
            )

            // TODO remove
//            ticketViewModel.updateTicketStatus(args.ticketNo, "1404", remarks = args.remarks)
        }

        return PopupWindow(
            binding.root, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun showAssetPopupWindow(view: View, popupWindowType: PopupWindow) {
        val dm = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(dm)

        val width = (dm.widthPixels * .9).toInt()
        val height = (dm.heightPixels * .93).toInt()

        dismissPopup()
        popupWindow = popupWindowType
        popupWindow?.isOutsideTouchable = false

        popupWindow?.setTouchInterceptor(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                event?.let {
                    if (it.x < 0 || it.x > width) return true
                    if (it.y < 0 || it.y > height) return true
                }

                return false
            }

        })

        popupWindow?.isFocusable = true
        popupWindow?.update(0, 0, width, height)
        popupWindow?.showAtLocation(view, Gravity.CENTER, 0, -25)

//        popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
    }

    private fun navigateToMachineHistory(machineId: Long) {
        val action =
            MechanicReportedTicketsPreviewFragmentDirections.actionMechanicReportedTicketsPreviewFragmentToMechanicReportedTicketsMachineHistoryFragment(
                machineId
            )
        navigate(action)
    }

    private fun navigateToMachineLocations(macSubTypeId: Long, brandId: Long, brand: String) {
        val action =
            MechanicReportedTicketsPreviewFragmentDirections.actionMechanicReportedTicketsPreviewFragmentToMechanicReportedTicketsAlternativeMachineLocationsFragment(
                macSubTypeId, brandId, brand
            )
        navigate(action)
    }

    private fun navigateToReportedTickets() {
        val action =
            MechanicReportedTicketsPreviewFragmentDirections.actionMechanicReportedTicketsPreviewFragmentToMechanicReportedTicketsFragment()
        navigate(action)
    }

    private fun showVideoPreviewPopupWindow(videoUrl: String): PopupWindow {

        val inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupVideoPreviewBinding.inflate(inflater)

        val url = "https://demonuts.com/Demonuts/smallvideo.mp4"
        val uri = Uri.parse(videoUrl)
        binding.videoPreview.setVideoURI(uri)

        val mediaController = MediaController(activity)
        binding.videoPreview.setMediaController(mediaController)
//        binding.videoPreview.seekTo(1)
        mediaController.setAnchorView(binding.videoPreview)

        binding.closePopup.setOnClickListener {
            dismissPopup()
        }

        return PopupWindow(
            binding.root, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun showImagePreviewPopupWindow(imageUrl: String): PopupWindow {

        val inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupImagePreviewBinding.inflate(inflater)

        val url = "https://cdn1.iconfinder.com/data/icons/logotypes/32/android-512.png"

        requestManager.load(imageUrl)
//            .transform(RotateTransformation(activity, 90f))
            .into(binding.imagePreview)

        binding.closePopup.setOnClickListener {
            dismissPopup()
        }

        return PopupWindow(
            binding.root, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
