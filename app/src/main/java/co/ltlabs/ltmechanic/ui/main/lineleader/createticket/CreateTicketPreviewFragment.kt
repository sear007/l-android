package co.ltlabs.ltmechanic.ui.main.lineleader.createticket

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.MediaController
import android.widget.PopupWindow
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import co.ltlabs.ltmechanic.constant.type.UserType
import co.ltlabs.ltmechanic.databinding.FragmentCreateTicketPreviewBinding
import co.ltlabs.ltmechanic.databinding.PopupImagePreviewBinding
import co.ltlabs.ltmechanic.databinding.PopupVideoPreviewBinding
import co.ltlabs.ltmechanic.util.AuthUtil
import co.ltlabs.ltmechanic.util.getTranslation
import co.ltlabs.ltmechanic.util.navigate
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.MainViewModel
import co.ltlabs.ltmechanic.viewmodels.main.lineleader.CreateTicketPreviewViewModel
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import dagger.android.support.DaggerFragment
import org.json.JSONObject
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */

private const val TAG = "PreviewFragment";

class CreateTicketPreviewFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var requestManager: RequestManager

    private val viewModel: CreateTicketPreviewViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(CreateTicketPreviewViewModel::class.java)
    }

    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MainViewModel::class.java)
    }

    private lateinit var userType: UserType

    @Inject
    lateinit var languageJsonObject: JSONObject

    private val args: CreateTicketPreviewFragmentArgs by navArgs()

    private var popupWindow: PopupWindow? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentCreateTicketPreviewBinding.inflate(inflater)
        userType = UserType.convertToType(AuthUtil.role)
        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        mainViewModel.firebaseNotificationFromDatabase.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it.isNotEmpty()) {

//                    viewModel.sendNotificationToMechanics("Bearer ${it[0].token}", "mechanic${args.notificationTopic}")
                }
            }
        })

        // Start translation
        with(languageJsonObject) {
            with(binding) {
                Log.d(TAG, "onCreateView: status: ${getTranslation(status.text.toString())}")
                toolBarTitleTextView.text = getTranslation(toolBarTitleTextView.text.toString())
                ticketNoLabel.text = getTranslation(ticketNoLabel.text.toString())
                machineLabel.text = getTranslation(machineLabel.text.toString())
                problemLabel.text = getTranslation(problemLabel.text.toString())
                solutionLabel.text = getTranslation(solutionLabel.text.toString())
                remarksLabel.text = getTranslation(remarksLabel.text.toString())
                attachmentsLabel.text = getTranslation(attachmentsLabel.text.toString())
                statusLabel.text = getTranslation(statusLabel.text.toString())
                currentPlaceLabel.text = getTranslation(currentPlaceLabel.text.toString())
                reportedPlaceLabel.text = getTranslation(reportedPlaceLabel.text.toString())
                btnDone.text = getTranslation(btnDone.text.toString())
            }
        }
        // End translation

        binding.ticketNo.text = args.ticketNo
        binding.machine.text = args.machine
        binding.problem.text = args.problem
        binding.solution.text = if (args.solution.isBlank()) "-" else args.solution
        binding.remarks.text = if (args.remarks.isBlank()) "-" else args.remarks
        binding.status.text = languageJsonObject.getTranslation(args.status)
        binding.currentPlace.text = args.lineStation
        binding.reportedPlace.text = args.lineStation

        if (args.imageAttachment1Url.isBlank() && args.videoAttachment1Url.isBlank()) {
            binding.emptyAttachment.visibility = View.VISIBLE
        }

//        binding.imageAttachment1.tag = "$API_BASE_URL_FILES/api/files/by-name?filename=${args.imageAttachment1Url}"
//        binding.imageAttachment2.tag = "$API_BASE_URL_FILES/api/files/by-name?filename=${args.imageAttachment2Url}"
//        binding.imageAttachment3.tag = "$API_BASE_URL_FILES/api/files/by-name?filename=${args.imageAttachment3Url}"
//
//        binding.videoAttachment1.tag = "$API_BASE_URL_FILES/api/files/by-name?filename=${args.videoAttachment1Url}"

        binding.imageAttachmentTemp.tag = args.imageAttachment1Url
        binding.imageAttachment1.tag = args.imageAttachment1Url
        binding.imageAttachment2.tag = args.imageAttachment2Url
        binding.imageAttachment3.tag = args.imageAttachment3Url

        binding.videoAttachment1.tag = args.videoAttachment1Url

        binding.imageAttachmentTemp.setOnClickListener {
            showPopupWindow(binding.root, showImagePreviewPopupWindow(it.tag.toString()))
        }

        if (args.videoAttachment1Url.isBlank() && args.imageAttachment1Url.isNotBlank()) {
            binding.videoAttachment1.visibility = View.INVISIBLE
            binding.imageAttachment1.visibility = View.GONE
            binding.imageAttachmentTemp.visibility = View.VISIBLE

            requestManager.load(binding.imageAttachmentTemp.tag.toString())
                .apply {
                    RequestOptions().override(75, 75)
                }
                .into(binding.imageAttachmentTemp)


        } else {
            binding.imageAttachment1.visibility = View.VISIBLE
            requestManager.load(binding.imageAttachment1.tag.toString())
                .apply {
                    RequestOptions().override(75, 75)
                }
                .into(binding.imageAttachment1)
        }

        if (args.imageAttachment1Url.isBlank() && args.videoAttachment1Url.isBlank()) {
            binding.emptyAttachment.visibility = View.VISIBLE

            binding.imageAttachmentTemp.visibility = View.INVISIBLE

        }

        if (args.imageAttachment1Url.isBlank()) {
            binding.imageAttachment1.visibility = View.GONE
        }

        binding.imageAttachment2.visibility = if (args.imageAttachment2Url.isBlank()) {
            View.GONE
        } else {

            requestManager.load(binding.imageAttachment2.tag.toString())
                .apply {
                    RequestOptions().override(75, 75)
                }
                .into(binding.imageAttachment2)

            View.VISIBLE
        }

        binding.imageAttachment3.visibility = if (args.imageAttachment3Url.isBlank()) {
            View.GONE
        } else {

            requestManager.load(binding.imageAttachment3.tag.toString())
                .apply {
                    RequestOptions().override(75, 75)
                }
                .into(binding.imageAttachment3)

            View.VISIBLE
        }

        binding.playButtonIcon.visibility = if (args.videoAttachment1Url.isBlank()) {
            View.GONE
        } else {
            View.VISIBLE
        }


        binding.videoAttachment1.visibility = if (args.videoAttachment1Url.isBlank()) {

            View.GONE
        } else {

            requestManager.load(binding.videoAttachment1.tag.toString())
                .apply {
                    RequestOptions().override(75, 75)
                }
                .into(binding.videoAttachment1)

            View.VISIBLE
        }

        binding.btnDone.setOnClickListener {

            when (args.origin) {

                "reported" -> {
                    navigateToReported()
                }

                "inrepair" -> {
                    navigateToInRepair()
                }

                "repaired" -> {
                    navigateToRepaired()
                }

                else -> {
                    navigateToHome()
                }
            }

        }

        binding.imageAttachment1.setOnClickListener {
            showPopupWindow(binding.root, showImagePreviewPopupWindow(it.tag.toString()))
        }

        binding.imageAttachment2.setOnClickListener {
            showPopupWindow(binding.root, showImagePreviewPopupWindow(it.tag.toString()))
        }

        binding.imageAttachment3.setOnClickListener {
            showPopupWindow(binding.root, showImagePreviewPopupWindow(it.tag.toString()))
        }

        binding.videoAttachment1.setOnClickListener {
            showPopupWindow(binding.root, showVideoPreviewPopupWindow(it.tag.toString()))
        }

        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navigateToHome()
                }

            })


        // Start translation
        with(languageJsonObject) {
            with(binding) {
                toolBarTitleTextView.text = getTranslation(toolBarTitleTextView.text.toString())
                ticketNoLabel.text = getTranslation(ticketNoLabel.text.toString())
                machineLabel.text = getTranslation(machineLabel.text.toString())
                problemLabel.text = getTranslation(problemLabel.text.toString())
                solutionLabel.text = getTranslation(solutionLabel.text.toString())
                remarksLabel.text = getTranslation(remarksLabel.text.toString())
                attachmentsLabel.text = getTranslation(attachmentsLabel.text.toString())
                statusLabel.text = getTranslation(statusLabel.text.toString())
                currentPlaceLabel.text = getTranslation(currentPlaceLabel.text.toString())
                reportedPlaceLabel.text = getTranslation(reportedPlaceLabel.text.toString())
                btnDone.text = getTranslation(btnDone.text.toString())
            }
        }
        // End translation

        return binding.root
    }

    private fun showPopupWindow(view: View, popupWindowType: PopupWindow) {
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

    private fun dismissPopup() {
        popupWindow?.let {
            if (it.isShowing) {
                it.dismiss()
            }
            popupWindow = null
        }
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

        requestManager
            .load(imageUrl)
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

    fun navigateToHome() {
        val action =
            when (userType) {
                is UserType.LineLeader -> {
                    CreateTicketPreviewFragmentDirections.actionCreateTicketPreviewFragmentToLineLeaderHomeFragment()
                }
                else -> {
                    CreateTicketPreviewFragmentDirections.actionCreateTicketPreviewFragmentToMechanicHomeFragment(
                        "",
                        false,
                        "",
                        ""
                    )
                }
            }

        navigate(action)
    }

    private fun navigateToReported() {
        val action =
            CreateTicketPreviewFragmentDirections.actionCreateTicketPreviewFragmentToLineLeaderReportedTicketsFragment()
        navigate(action)
    }

    private fun navigateToInRepair() {
        val action =
            CreateTicketPreviewFragmentDirections.actionCreateTicketPreviewFragmentToLineLeaderInRepairTicketsFragment()
        navigate(action)
    }

    private fun navigateToRepaired() {
        val action =
            CreateTicketPreviewFragmentDirections.actionCreateTicketPreviewFragmentToLineLeaderRepairedTicketsFragment()
        navigate(action)
    }

}
