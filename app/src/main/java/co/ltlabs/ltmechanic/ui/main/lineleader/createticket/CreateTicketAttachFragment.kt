package co.ltlabs.ltmechanic.ui.main.lineleader.createticket

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.METADATA_KEY_DURATION
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.DisplayMetrics
import android.view.*
import android.widget.MediaController
import android.widget.PopupWindow
import android.widget.ProgressBar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import co.ltlabs.ltmechanic.BuildConfig
import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.databinding.FragmentCreateTicketAttachBinding
import co.ltlabs.ltmechanic.databinding.PopupImagePreviewBinding
import co.ltlabs.ltmechanic.databinding.PopupVideoPreviewBinding
import co.ltlabs.ltmechanic.domain.SolutionType
import co.ltlabs.ltmechanic.network.Asset
import co.ltlabs.ltmechanic.ui.main.mechanic.inrepairtickets.PhotoAdapter
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.util.dialog.LoadingIndicator
import co.ltlabs.ltmechanic.util.popup.DialogPopup
import co.ltlabs.ltmechanic.util.popup.SpinnerItem
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.lineleader.CreateTicketAttachViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.SpinnerViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.TicketViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_mechanic_create_ticket_attach.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


private const val TAG = "AttachFragment";

const val GALLERY_REQUEST_CODE = 100
const val VIDEO_REQUEST_CODE = 200

class CreateTicketAttachFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var requestManager: RequestManager

    @Inject
    lateinit var loading: LoadingIndicator

    private val viewModel: CreateTicketAttachViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(CreateTicketAttachViewModel::class.java)
    }

    private val ticketViewModel: TicketViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(TicketViewModel::class.java)
    }

    private val spinnerViewModel: SpinnerViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(SpinnerViewModel::class.java)
    }

    private lateinit var photoAdapter: PhotoAdapter

    private val args: CreateTicketAttachFragmentArgs by navArgs()

    private var cameraFilePath = ""

    private lateinit var coordinatorLayout: CoordinatorLayout
    private var solutionTypeId = "0"

    private lateinit var binding: FragmentCreateTicketAttachBinding

    private var notificationTopic = ""

    private lateinit var progressBar: ProgressBar

    @Inject
    lateinit var languageJsonObject: JSONObject

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentCreateTicketAttachBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        binding.machineNoTextView.text = args.machine
        binding.problemTextView.text = args.problem

        if (args.problemTypeId == 0L) {
            viewModel.getSolutionTypes()
        } else {
            viewModel.getSolutionTypesByProblemId(args.problemTypeId)
        }

        coordinatorLayout = binding.coordinatorLayout
        progressBar = binding.progressBar

        // Start translation
        with(languageJsonObject) {
            with(binding) {
                toolBarTitleTextView.text = getTranslation(toolBarTitleTextView.text.toString())
                labelScanMachine.text = getTranslation(labelScanMachine.text.toString())
                labelSelectedProblem.text =
                    getTranslation(labelSelectedProblem.text.toString()) + " *"
                labelSolution.text = getTranslation(labelSolution.text.toString())
                labelRemarks.text = getTranslation(labelRemarks.text.toString())
                labelAddPicture.text = getTranslation(labelAddPicture.text.toString())
                labelAddVideo.text = getTranslation(labelAddVideo.text.toString())
                btnCancel.text = getTranslation(btnCancel.text.toString())
                btnSubmit.text = getTranslation(btnSubmit.text.toString())
                clearVideo.text = getTranslation(clearVideo.text.toString())
                clearImage.text = getTranslation(clearImage.text.toString())
            }
        }
        // End translation

        binding.videoImageView.setOnClickListener {
            if (viewModel.videoUrisTemp.size == 1) {
                val message = "You already attached 1 video"
                binding.coordinatorLayout.showSnackbar(languageJsonObject.getTranslation(message))
            } else {
                pickVideoFromGallery()
            }
        }

        binding.attachImage.setOnClickListener {
            if (viewModel.imageUrisTemp.size == 3) {
                val message = "You already attached 3 images"
                binding.coordinatorLayout.showSnackbar(languageJsonObject.getTranslation(message))
            } else {
                pickFromGallery()
            }
        }

        viewModel.mfgLinesFromDatabase.observe(viewLifecycleOwner, Observer { mfgLines ->

            mfgLines.forEach {
                notificationTopic += "-${it.mfgLine}"
            }

        })

        viewModel.fileResult.observe(viewLifecycleOwner, Observer {
            if (it != null) {

                val assets = mutableListOf<Asset>()

                for (file in it) {
                    assets.add(Asset(file.path))
                }

                ticketViewModel.createTicket(
                    args.machineId,
                    if (args.problemTypeId == 0L) null else args.problemTypeId.toString(),
                    binding.remarksEditText.text.toString(),
                    DateTime(DateTimeZone.UTC).toString(),
                    assets,
                    if (solutionTypeId == "0") null else solutionTypeId
                )

                viewModel.fileResultComplete()
            }
        })

        ticketViewModel.createTicketStatus.observe(viewLifecycleOwner, Observer {
            if (it != null) {

                when (it) {
                    CreateTicketStatus.NO_ATTACHED_CHECKLIST -> {
                        binding.coordinatorLayout.showSnackbar(
                            languageJsonObject.getTranslation(
                                "Machine has no attached repair checklist"
                            )
                        )
                    }

                    CreateTicketStatus.REACHED_REMARKS_LIMIT -> {
                        binding.coordinatorLayout.showSnackbar(
                            languageJsonObject.getTranslation(
                                "Cannot Submit. Maximum characters exceeded for Remarks."
                            )
                        )
                    }

                    CreateTicketStatus.HAS_OPEN_TICKETS -> {
                        binding.coordinatorLayout.showSnackbar(
                            languageJsonObject.getTranslation(
                                "This machine has an active ticket"
                            )
                        )
                    }
                }

            }
        })

        binding.solutionSpinner.setOnClickListener {
            DialogPopup.show(
                binding.root,
                spinnerViewModel,
                resources.getString(R.string.select_item_of_solution),
                languageJsonObject
            )
        }

        spinnerViewModel.submitData.observe(viewLifecycleOwner, Observer { solutionType ->
            solutionTypeId = solutionType.id.toString()
            solutionSpinner.text = solutionType.name
        })

        viewModel.solutionTypes.observe(viewLifecycleOwner, Observer { solutionTypes ->
            loading.dismiss()
            if (solutionTypes != null) {

                activity?.let {
                    val list = mutableListOf<SolutionType>()
                    list.add(SolutionType(0, "", ""))
                    solutionTypes.forEach { solutiontype ->
                        list.add(solutiontype)
                    }

                    spinnerViewModel.items = list.filter { it.solutionType != "" }.map { solution ->
                        SpinnerItem(
                            name = solution.solutionType,
                            id = solution.id,
                            desc = solution.desc
                        )
                    }
                }

                viewModel.solutionTypecomplete()
            }
        })

        ticketViewModel.ticket.observe(viewLifecycleOwner, Observer { ticket ->
            if (ticket != null) {

                var imageAttachment1Url = ""
                var imageAttachment2Url = ""
                var imageAttachment3Url = ""

                var videoAttachment1Url = ""

                ticket.ticketAsset?.let { assets ->
                    val imageAttachments = assets.filter {
                        it.link.split(".")[1].contains("png") ||
                                it.link.split(".")[1].contains("jpg")
                    }

                    val videoAttachments = assets.filter {
                        it.link.split(".")[1].contains("mp4")
                    }

                    for ((index, imageAttachment) in viewModel.imageUrisTemp.withIndex()) {

                        when (index) {

                            0 -> {
                                imageAttachment1Url = imageAttachment.toString()
                            }

                            1 -> {
                                imageAttachment2Url = imageAttachment.toString()
                            }

                            2 -> {
                                imageAttachment3Url = imageAttachment.toString()
                            }
                        }

                    }

                    for ((index, videoAttachment) in viewModel.videoUrisTemp.withIndex()) {

                        when (index) {

                            0 -> {
                                videoAttachment1Url = videoAttachment.toString()
                            }
                        }

                    }

                }

                navigateToPreview(
                    ticket.ticketNo,
                    ticket.machineNo,
                    args.problem,
                    ticket.solution,
                    ticket.remarks,
                    ticket.status,
                    "${args.mfgLine} - ${args.station}",
                    imageAttachment1Url,
                    imageAttachment2Url,
                    imageAttachment3Url,
                    videoAttachment1Url,
                    args.station,
                    args.mfgLine,
                    args.origin
                )

                ticketViewModel.ticketComplete()

            }
        })

        binding.btnSubmit.setOnClickListener {
            if (viewModel.videoUrisTemp.isEmpty() && viewModel.imageUrisTemp.isEmpty()) {
                ticketViewModel.createTicket(
                    args.machineId,
                    if (args.problemTypeId == 0L) null else args.problemTypeId.toString(),
                    binding.remarksEditText.text.toString(),
                    DateTime(DateTimeZone.UTC).toString(),
                    null,
                    if (solutionTypeId == "0") null else solutionTypeId
                )
            } else {
                viewModel.uploadFile(requireContext())
            }
        }

        binding.clearImage.setOnClickListener {
            viewModel.imageUrisTemp.forEach { uri ->
                uri.path?.let {
                    File(it).absoluteFile.delete()
                }

            }

            viewModel.imageUrisTemp.clear()
            viewModel.updateImageUris(viewModel.imageUrisTemp)
        }

        binding.clearVideo.setOnClickListener {

            viewModel.videoUrisTemp.forEach { uri ->
                uri.path?.let {
                    File(it).absoluteFile.delete()
                }
            }

            viewModel.videoUrisTemp.clear()
            viewModel.updateVideoUris(viewModel.videoUrisTemp)
        }

        viewModel.videoUris.observe(viewLifecycleOwner, Observer {
            binding.apply {
                clearVideo.visibility = if (it.isEmpty()) View.INVISIBLE else View.VISIBLE
                ivVideo.visibility = clearVideo.visibility
                ivPlayButton.visibility = clearVideo.visibility
            }
            if (it.isNotEmpty()) {
                requestManager.load(it[0])
                    .into(binding.ivVideo)
            }
        })

        viewModel.imageUris.observe(viewLifecycleOwner, Observer {
            photoAdapter.updateList(it)
            binding.clearImage.visibility = if (it.isEmpty()) View.INVISIBLE else View.VISIBLE
        })

        ticketViewModel.status.observe(viewLifecycleOwner, Observer {
            when (it) {

                ApiStatus.LOADING -> {
                    loading.show(requireContext(), false, LoadingIndicator.CREATE_TICKET)
                }
                else -> {
                    loading.dismiss()
                }

            }
        })

        viewModel.status.observe(viewLifecycleOwner, Observer {
            when (it) {

                ApiStatus.LOADING -> {
                    loading.show(requireContext(), false, LoadingIndicator.UPLOAD_FILE)
                }
                else -> {
                    loading.dismiss()
                }

            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        setupPhotoList()
        setupListener()
        loading.show(requireContext(), false, LoadingIndicator.UPLOAD_FILE)

        loading.onDismissListener { event ->
            if (event == LoadingIndicator.UPLOAD_FILE)
                viewModel.cancelUploadFileJob()
            if (event == LoadingIndicator.CREATE_TICKET)
                ticketViewModel.cancelTicketJob()

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    private fun setupPhotoList() {
        photoAdapter = PhotoAdapter(requestManager, arrayListOf())
        binding.rvAttachment.adapter = photoAdapter
        photoAdapter.setItemClick {
            showAssetPopupWindow(binding.root, showImagePreviewPopupWindow(it))
        }
    }

    private fun setupListener() {
        binding.ivVideo.setOnClickListener {
            if (viewModel.videoUrisTemp.isNotEmpty()) {
                val uri = viewModel.videoUrisTemp[0]
                showAssetPopupWindow(binding.root, showVideoPreviewPopupWindow(uri))
            }
        }

        binding.ivPlayButton.setOnClickListener {
            val uri = viewModel.videoUrisTemp[0]
            showAssetPopupWindow(binding.root, showVideoPreviewPopupWindow(uri))
        }

        binding.videoImageView.setOnClickListener {
            if (viewModel.videoUrisTemp.size == 1) {
                showSnackBar(
                    binding.root,
                    languageJsonObject.getTranslation(languageJsonObject.getTranslation("You already attached 1 video"))
                )
            } else {
                pickVideoFromGallery()
            }
        }

        binding.btnCancel.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.attachImage.setOnClickListener {
            if (viewModel.imageUrisTemp.size == 3) {
                showSnackBar(
                    binding.root,
                    languageJsonObject.getTranslation(languageJsonObject.getTranslation("You already attached 3 images"))
                )
            } else {
                pickFromGallery()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    val imageUri = data?.data

                    val uri = imageUri ?: Uri.parse(cameraFilePath)

                    viewModel.imageUrisTemp.add(uri)
                    viewModel.updateImageUris(viewModel.imageUrisTemp)
                }

                VIDEO_REQUEST_CODE -> {
                    val videoUri = data?.data

                    videoUri?.let {


                        val retriever = MediaMetadataRetriever()
                        retriever.setDataSource(activity?.applicationContext, videoUri)
                        val getDuration = retriever.extractMetadata(METADATA_KEY_DURATION)
                        retriever.release()

                        val duration = getDuration.toLongOrNull()?.div(1000) ?: 0

                        if (duration > 30) {
                            coordinatorLayout.showSnackbar(
                                languageJsonObject.getTranslation(
                                    "Duration of video limit to 30 seconds"
                                )
                            )
                        } else {
                            viewModel.videoUrisTemp.add(it)
                            viewModel.updateVideoUris(viewModel.videoUrisTemp)
                        }
                    }
                }
            }
        }
    }

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"

        val mimeTypes = arrayOf("image/jpeg", "image/png")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        activity?.let { activity ->
            createImageFile()?.let { file ->
                cameraIntent.putExtra(
                    MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(
                        activity.applicationContext,
                        "${BuildConfig.APPLICATION_ID}.provider",
                        file
                    )
                )
            }
        }

        val chooser = Intent(Intent.ACTION_CHOOSER)
        chooser.putExtra(Intent.EXTRA_INTENT, intent)
        chooser.putExtra(Intent.EXTRA_TITLE, "Select from:")

        val intentArray = arrayOf(cameraIntent)
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)

        startActivityForResult(chooser, GALLERY_REQUEST_CODE)
    }

    private fun pickVideoFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "video/*"

        val mimeTypes = arrayOf("video/mp4")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)

        val cameraIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        activity?.let { activity ->
            createVideoFile()?.let { file ->
                cameraIntent.putExtra(
                    MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(
                        activity.applicationContext,
                        "${BuildConfig.APPLICATION_ID}.provider",
                        file
                    )
                )
            }
        }

        val chooser = Intent(Intent.ACTION_CHOOSER)
        chooser.putExtra(Intent.EXTRA_INTENT, intent)
        chooser.putExtra(Intent.EXTRA_TITLE, "Select from:")

        val intentArray = arrayOf(cameraIntent)
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)

        startActivityForResult(chooser, VIDEO_REQUEST_CODE)
    }

    private var popupWindow: PopupWindow? = null
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
    }

    private fun showVideoPreviewPopupWindow(videoUrl: Uri): PopupWindow {
        val inflater =
            activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupVideoPreviewBinding.inflate(inflater)

        binding.videoPreview.setVideoURI(videoUrl)

        val mediaController = MediaController(activity)
        binding.videoPreview.setMediaController(mediaController)
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

    private fun showImagePreviewPopupWindow(imageUrl: Uri): PopupWindow {
        val inflater =
            activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupImagePreviewBinding.inflate(inflater)

        Glide.with(this)
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

    private fun dismissPopup() {
        popupWindow?.let {
            if (it.isShowing) {
                it.dismiss()
            }
            popupWindow = null
        }
    }


    private fun encodeImage(bm: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b = baos.toByteArray()
        val encImage = Base64.encodeToString(b, Base64.DEFAULT)

        return encImage
    }

    private fun createVideoFile(): File? {

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val videoFileName = "MP4_${timeStamp}_"

        activity?.let {
            val storageDir =
                File("${it.applicationContext.getExternalFilesDir(Environment.DIRECTORY_DCIM)}${File.separator}")
            val video = File.createTempFile(
                videoFileName,
                ".mp4",
                storageDir
            )
            cameraFilePath = "file://${video.absolutePath}"
            return video

        }

        return null

    }

    private fun createImageFile(): File? {

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_${timeStamp}_"

        activity?.let {
            val storageDir =
                File("${it.applicationContext.getExternalFilesDir(Environment.DIRECTORY_DCIM)}${File.separator}")
            val image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
            )
            cameraFilePath = "file://${image.absolutePath}"
            return image

        }

        return null

    }

    private fun navigateToPreview(
        ticketNo: String,
        machine: String,
        problem: String,
        solution: String,
        remarks: String,
        status: String,
        lineStation: String,
        imageAttachment1Url: String,
        imageAttachment2Url: String,
        imageAttachment3Url: String,
        videoAttachment1Url: String,
        station: String,
        mfgLine: String,
        origin: String
    ) {
        val action =
            CreateTicketAttachFragmentDirections.actionCreateTicketAttachFragmentToCreateTicketPreviewFragment(
                ticketNo,
                machine,
                problem,
                solution,
                remarks,
                status,
                lineStation,
                imageAttachment1Url,
                imageAttachment2Url,
                imageAttachment3Url,
                videoAttachment1Url,
                station,
                mfgLine,
                origin,
                notificationTopic
            )
        navigate(action)
    }
}
