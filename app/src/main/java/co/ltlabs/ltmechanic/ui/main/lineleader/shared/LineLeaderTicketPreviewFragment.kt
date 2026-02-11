package co.ltlabs.ltmechanic.ui.main.lineleader.shared

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.MediaController
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.constant.TicketType
import co.ltlabs.ltmechanic.databinding.*
import co.ltlabs.ltmechanic.di.AppModule
import co.ltlabs.ltmechanic.ui.main.ReopenTicketAdapter
import co.ltlabs.ltmechanic.ui.main.lineleader.AttachmentsAdapter
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.lineleader.LineLeaderTicketPreviewViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.TicketViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import dagger.android.support.DaggerFragment
import org.json.JSONObject
import javax.inject.Inject

private const val TAG = "LLTicketPreview";

class LineLeaderTicketPreviewFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val viewModel: LineLeaderTicketPreviewViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(LineLeaderTicketPreviewViewModel::class.java)
    }
    private val ticketViewModel: TicketViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(TicketViewModel::class.java)
    }

    @Inject
    lateinit var requestManager: RequestManager

    private val args: LineLeaderTicketPreviewFragmentArgs by navArgs()

    private var popupWindow: PopupWindow? = null

    var macSubTypeId = 0
    var brandId = 0

    @Inject
    lateinit var reopenTicketAdapter: ReopenTicketAdapter
    private lateinit var binding: FragmentLineLeaderTicketPreviewBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentLineLeaderTicketPreviewBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        // Start translation
        with(languageJsonObject) {
            with(binding) {
                ticketNoLabel.text = getTranslation(ticketNoLabel.text.toString())
                machineLabel.text = getTranslation(machineLabel.text.toString())
                problemLabel.text = getTranslation(problemLabel.text.toString())
                solutionLabel.text = getTranslation(solutionLabel.text.toString())
                remarksLabel.text = getTranslation(remarksLabel.text.toString())
                currentPlaceLabel.text = getTranslation(currentPlaceLabel.text.toString())
                reportedPlaceLabel.text = getTranslation(reportedPlaceLabel.text.toString())

                reportedTimeLabel.text = getTranslation(reportedTimeLabel.text.toString())
                grabbedTimeLabel.text = getTranslation(grabbedTimeLabel.text.toString())
                repairedTimeLabel.text = getTranslation(repairedTimeLabel.text.toString())
                closedTimeLabel.text = getTranslation(closedTimeLabel.text.toString())
                totalDurationLabel.text = getTranslation(totalDurationLabel.text.toString())

                attachmentsLabel.text = getTranslation(attachmentsLabel.text.toString())
                btnCloseTicket.text = getTranslation(btnCloseTicket.text.toString())
                btnViewChecklist.text = getTranslation(btnViewChecklist.text.toString())
                btnReopenTicket.text = getTranslation(btnReopenTicket.text.toString())
                btnViewChecklist2.text = getTranslation(btnViewChecklist2.text.toString())
            }
        }
        // End translation

        binding.btnViewChecklist.visibility = if (args.status == "REPAIRED") {
            View.VISIBLE
        } else {
            View.GONE
        }

        if (TicketUtil.reopenTicketEnabled && args.status == "CLOSED") {

            binding.btnViewChecklist2.visibility = View.INVISIBLE

            binding.btnViewChecklist.visibility = View.VISIBLE

            binding.btnReopenTicket.visibility = View.VISIBLE

        } else {

            if (args.status == "CLOSED") {
                binding.btnViewChecklist2.visibility = View.VISIBLE

                binding.btnViewChecklist.visibility = View.INVISIBLE

                binding.btnReopenTicket.visibility = View.INVISIBLE
            }

        }

        binding.btnCloseTicket.visibility = if (args.status == "REPAIRED") {
            View.VISIBLE
        } else {
            View.GONE
        }

        binding.statusTextView.text = languageJsonObject.getTranslation(args.status)
        binding.statusTextView2.text = languageJsonObject.getTranslation(args.status)
        val textColor = when (TicketType.fromCodeToType(args.status)) {
            is TicketType.Reported -> {
                ContextCompat.getColor(requireContext(), R.color.colorReported)
            }
            is TicketType.InRepair -> {
                ContextCompat.getColor(requireContext(), R.color.colorInRepair)
            }
            is TicketType.Reopen -> {
                binding.statusTextView.text = languageJsonObject.getTranslation(args.status)
                binding.statusTextView2.text = languageJsonObject.getTranslation(args.status)
                binding.statusTextView.setBackgroundResource(R.drawable.bg_reopen_ticket)
                binding.statusTextView2.setBackgroundResource(R.drawable.bg_reopen_ticket)
                ContextCompat.getColor(requireContext(), R.color.colorWhite)
            }
            else -> {
                ContextCompat.getColor(requireContext(), R.color.colorClosedRepaired)
            }
        }
        binding.statusTextView.setTextColor(textColor)
        binding.statusTextView2.setTextColor(textColor)

        binding.btnCloseTicket.setOnClickListener {
            val dm = DisplayMetrics()
            activity?.windowManager?.defaultDisplay?.getMetrics(dm)

            val width = (dm.widthPixels * .9).toInt()
            val height = (dm.heightPixels * .28).toInt()


            dismissPopup()
            popupWindow = showTicketClosePopupWindow()
            popupWindow?.isOutsideTouchable = true
            popupWindow?.isFocusable = true
            popupWindow?.update(0, 0, width, height)
            popupWindow?.showAtLocation(it.rootView, Gravity.CENTER, 0, -25)

            DimUtil.dimBehind(popupWindow)

            popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        }

        binding.btnReopenTicket.setOnClickListener {
            val dm = DisplayMetrics()
            activity?.windowManager?.defaultDisplay?.getMetrics(dm)

            val width = (dm.widthPixels * .9).toInt()
            val height = (dm.heightPixels * .28).toInt()

            dismissPopup()
            popupWindow = showTicketReopenPopupWindow()
            popupWindow?.isOutsideTouchable = true
            popupWindow?.isFocusable = true
            popupWindow?.update(0, 0, width, height)
            popupWindow?.showAtLocation(it.rootView, Gravity.CENTER, 0, -25)

            popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        }

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

        ticketViewModel.ticketStatus.observe(viewLifecycleOwner, Observer { ticketStatus ->
            if (ticketStatus != null) {

                when (ticketStatus) {

                    TicketStatus.CLOSED -> {
                        dismissPopup()
                        navigateToRepaired()
                    }

                }

                ticketViewModel.ticketStatusComplete()
            }
        })

        ticketViewModel.ticketReopenStatus.observe(
            viewLifecycleOwner,
            Observer { ticketReopenStatus ->
                if (ticketReopenStatus != null) {

                    when (ticketReopenStatus) {

                        TicketReopenStatus.SUCCESS -> {
                            dismissPopup()
                            navigateToRepaired()
                        }

                        TicketReopenStatus.HAS_OPEN_TICKETS -> {
                            binding.coordinatorLayout.showSnackbar(
                                languageJsonObject.getTranslation(
                                    "Cannot Reopen Ticket. Machine has open repair ticket"
                                )
                            )
                        }

                        TicketReopenStatus.TIME_EXCEEDED -> {
                            binding.coordinatorLayout.showSnackbar(
                                languageJsonObject.getTranslation(
                                    "Allowed time to reopen ticket has been exceeded."
                                )
                            )
                        }

                        TicketReopenStatus.NOT_ALLOWED -> {
                            binding.coordinatorLayout.showSnackbar(
                                languageJsonObject.getTranslation(
                                    "You are not allowed to reopen this ticket."
                                )
                            )
                        }

                    }

                    ticketViewModel.ticketReopenStatusComplete()
                }
            })

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

        binding.remarks.text = args.remarks.ifBlank {
            "-"
        }
        binding.solution.text = args.solution.ifBlank {
            "-"
        }

        binding.btnViewChecklist.setOnClickListener {
            navigateToChecklist(args.ticketId, args.ticketNo)
        }

        binding.btnViewChecklist2.setOnClickListener {
            navigateToChecklist(args.ticketId, args.ticketNo)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (TicketType.fromCodeToType(args.status) is TicketType.Reopen) {
            handleReopenTicket()
        }
        setupAttachmentList()
        maintDate()
    }

    private fun setupAttachmentList() {
        val baseUrl =
            "${AppModule.scheme}://${AppModule.host}$FILE_API_ADDED_URL/api/files/by-name?path="
        val video = args.videoAttachmentUrl
        val video2 = args.videoAttachmentUrl2
        val image = args.imageAttachmentUrl1
        val image2 = args.imageAttachmentUrl2
        val image3 = args.imageAttachmentUrl3
        val list = arrayListOf<String>()
        if (!video.isNullOrBlank()) {
            list.add("$baseUrl$video")
        }
        if (!video2.isNullOrBlank()) {
            list.add("$baseUrl$video2")
        }
        if (!image.isNullOrBlank()) {
            list.add("$baseUrl$image")
        }
        if (!image2.isNullOrBlank()) {
            list.add("$baseUrl$image2")
        }
        if (!image3.isNullOrBlank()) {
            list.add("$baseUrl$image3")
        }

        binding.emptyAttachment.isVisible = list.isEmpty()
        binding.rvAttachment.isVisible = list.isNotEmpty()
        val adapter = AttachmentsAdapter(requestManager, list)
        binding.rvAttachment.adapter = adapter

        adapter.setItemClick {
            val data = list[it]

            if (data.contains(".mp4")) {
                showAssetPopupWindow(binding.root, showVideoPreviewPopupWindow(data))
                return@setItemClick
            }

            if (data.contains(".png") || data.contains(".jpg")) {
                showAssetPopupWindow(binding.root, showImagePreviewPopupWindow(data))
            }
        }

    }

    private fun maintDate() {
        binding.apply {
            tvMainStatus.isVisible =
                TicketType.fromCodeToType(args.status) is TicketType.Reopen ||
                        TicketType.fromCodeToType(args.status) is TicketType.Reported
            tvMainStatusLabel.isVisible = tvMainStatus.isVisible
            tvNextMainDate.isVisible = tvMainStatus.isVisible
            tvNextMainDateLabel.isVisible = tvMainStatus.isVisible

            val next = args.nextMainDate?.nextMainDate
            val reported = args.nextMainDate?.reportedDate
            tvMainStatus.isSelected = DateUtil.isMaintDateOverdue(reported, next)
            tvMainStatus.text = if (DateUtil.isMaintDateOverdue(reported, next))
                languageJsonObject.getTranslation("OVERDUE")
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
        viewModel.ticketLogs.observe(viewLifecycleOwner, Observer {
            binding.rvReopened.isVisible = it.isNotEmpty()
            reopenTicketAdapter.submitList(it)
        })
    }

    private fun showTicketClosePopupWindow(): PopupWindow {

        val inflater =
            activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupCloseConfirmationMessageBinding.inflate(inflater)

        with(languageJsonObject) {
            with(binding) {
                btnCancelLineSetup2.text = getTranslation(btnCancelLineSetup2.text.toString())
                btnConfirmLineSetup2.text = getTranslation(btnConfirmLineSetup2.text.toString())
            }
        }

        with(languageJsonObject) {
            with(binding) {
                textView3.text = getTranslation(textView3.text.toString())
            }
        }

        binding.btnCancelLineSetup2.setOnClickListener {
            dismissPopup()

            ticketViewModel.reopenTicket(args.ticketNo)
        }

        binding.btnConfirmLineSetup2.setOnClickListener {

//            ticketViewModel.updateTicketStatus(args.ticketNo, StatusIdUtil.RT_CLOSED.toString(), remarks = args.remarks)
            ticketViewModel.getStatusIdAndUpdateTicketStatus(
                TicketsStatus.CLOSED,
                TicketModule.REPAIR,
                args.ticketNo,
                remarks = args.remarks
            )
        }

        return PopupWindow(
            binding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun showTicketReopenPopupWindow(): PopupWindow {

        val inflater =
            activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupReopenConfirmationMessageBinding.inflate(inflater)

        binding.btnCancelLineSetup2.setOnClickListener {
            dismissPopup()

        }

        with(languageJsonObject) {
            with(binding) {
                textView3.text = getTranslation(textView3.text.toString())
            }
        }

        with(languageJsonObject) {
            with(binding) {
                btnCancelLineSetup2.text = getTranslation(btnCancelLineSetup2.text.toString())
                btnConfirmLineSetup2.text = getTranslation(btnConfirmLineSetup2.text.toString())
            }
        }


        binding.btnConfirmLineSetup2.setOnClickListener {
            TicketUtil.selectedRepairedTab = "closed"

            ticketViewModel.reopenTicket(args.ticketNo)
//            navigateToRepaired()
            dismissPopup()
        }

        return PopupWindow(
            binding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
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

    private fun navigateToChecklist(ticketId: Long, ticketNo: String) {
        val action = LineLeaderTicketPreviewFragmentDirections
            .actionLineLeaderTicketPreviewFragmentToLineLeaderTicketChecklistFragment(
                ticketId,
                ticketNo
            )
        navigate(action)
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

    private fun showVideoPreviewPopupWindow(videoUrl: String): PopupWindow {

        val inflater =
            activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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
            binding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun showImagePreviewPopupWindow(imageUrl: String): PopupWindow {

        val inflater =
            activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupImagePreviewBinding.inflate(inflater)

        val url = "https://cdn1.iconfinder.com/data/icons/logotypes/32/android-512.png"

        Glide.with(this)
            .load(imageUrl)
//            .transform(RotateTransformation(activity, 90f))
            .into(binding.imagePreview)

        binding.closePopup.setOnClickListener {
            dismissPopup()
        }

        return PopupWindow(
            binding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun navigateToRepaired() {
        val action = LineLeaderTicketPreviewFragmentDirections
            .actionLineLeaderTicketPreviewFragmentToLineLeaderRepairedTicketsFragment()
        navigate(action)
    }

}
