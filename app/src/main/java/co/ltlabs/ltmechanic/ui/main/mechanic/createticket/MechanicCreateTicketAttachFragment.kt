package co.ltlabs.ltmechanic.ui.main.mechanic.createticket

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import co.ltlabs.ltmechanic.BuildConfig

import co.ltlabs.ltmechanic.R
import co.ltlabs.ltmechanic.databinding.FragmentMechanicCreateTicketAttachBinding
import co.ltlabs.ltmechanic.domain.SolutionType
import co.ltlabs.ltmechanic.util.popup.SpinnerItem
import co.ltlabs.ltmechanic.network.Asset
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.util.dialog.LoadingIndicator
import co.ltlabs.ltmechanic.util.popup.DialogPopup
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.MechanicCreateTicketAttachViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.*
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_mechanic_create_ticket_attach.*
import kotlinx.android.synthetic.main.popup_spinner_with_search.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.abs

private const val TAG = "MAttachFragment"

const val GALLERY_REQUEST_CODE = 100
const val VIDEO_REQUEST_CODE = 200


class MechanicCreateTicketAttachFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    @Inject
    lateinit var loading: LoadingIndicator

    private val viewModel: MechanicCreateTicketAttachViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MechanicCreateTicketAttachViewModel::class.java)
    }

    private val ticketViewModel: TicketViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(TicketViewModel::class.java)
    }

    private val referenceViewModel: ReferenceViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(ReferenceViewModel::class.java)
    }

    private val attachmentViewModel: AttachmentViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(AttachmentViewModel::class.java)
    }

    private val spinnerViewModel: SpinnerViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(SpinnerViewModel::class.java)
    }

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.IO)
    private val mainModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private var cameraFilePath = ""

    private lateinit var coordinatorLayout: CoordinatorLayout
    private var solutionTypeId = "0"

    private val args: MechanicCreateTicketAttachFragmentArgs by navArgs()

    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentMechanicCreateTicketAttachBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        binding.machineNoTextView.text = args.machine
        binding.problemTextView.text = args.problem

        with(languageJsonObject) {
            with(binding) {
                toolBarTitleTextView.text = getTranslation(toolBarTitleTextView.text.toString())
                labelScanMachine.text = getTranslation(labelScanMachine.text.toString())
                labelSelectedProblem.text = getTranslation(labelSelectedProblem.text.toString())
                labelSolution.text = getTranslation(labelSolution.text.toString())
                labelRemarks.text = getTranslation(labelRemarks.text.toString())
                labelAddPicture.text = getTranslation(labelAddPicture.text.toString())
                labelAddVideo.text = getTranslation(labelAddVideo.text.toString())
                clearImage.text = getTranslation(clearImage.text.toString())
                clearVideo.text = getTranslation(clearVideo.text.toString())
                btnCancel.text = getTranslation(btnCancel.text.toString())
                btnSubmit.text = getTranslation(btnSubmit.text.toString())
            }
        }

//        ConnectionUtil.internetConnected.observe(viewLifecycleOwner, Observer {
//            if (it != null) {
//                if (it) {
//
//                    if (args.problemTypeId == 0L) {
//                        referenceViewModel.getSolutionTypes()
//                    } else {
//                        referenceViewModel.getSolutionTypesByProblemId(args.problemTypeId)
//                    }
//
//                    ConnectionUtil.setInternetConnected(false)
//                }
//                ConnectionUtil.setInternetConnectedComplete()
//            }
//        })

        if (args.problemTypeId == 0L) {
            referenceViewModel.getSolutionTypes()
        } else {
            referenceViewModel.getSolutionTypesByProblemId(args.problemTypeId)
        }

        coordinatorLayout = binding.coordinatorLayout
        progressBar = binding.progressBar

        binding.videoImageView.setOnClickListener {
            if (attachmentViewModel.videoUrisTemp.size == 1) {
                binding.coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("You already attached 1 video"))
            } else {
                pickVideoFromGallery()
            }
        }

        binding.attachImage.setOnClickListener {
            if (attachmentViewModel.imageUrisTemp.size == 3) {
                binding.coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("You already attached 3 images"))
            } else {
                pickFromGallery()
            }
        }

        attachmentViewModel.fileResult.observe(viewLifecycleOwner, Observer {
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

                attachmentViewModel.fileResultComplete()
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
            DialogPopup.show(binding.root, spinnerViewModel, resources.getString(R.string.select_item_of_solution), languageJsonObject)
        }

        spinnerViewModel.submitData.observe(viewLifecycleOwner, Observer { solutionType ->
            solutionTypeId = solutionType.id.toString()
            solutionSpinner.text = solutionType.name
        })

        referenceViewModel.solutionTypes.observe(viewLifecycleOwner, Observer { solutionTypes ->
            if (solutionTypes != null) {
                activity?.let {
                    val list = mutableListOf<SolutionType>()
                    list.add(SolutionType(0, "", ""))
                    solutionTypes.forEach { solutionType ->
                        list.add(solutionType)
                    }

                    spinnerViewModel.items = list.filter { it.solutionType != "" }.map { solution ->
                        SpinnerItem(name = solution.solutionType, id = solution.id, desc = solution.desc)
                    }

//                    val dataAdapter =  ArrayAdapter(it, android.R.layout.simple_spinner_item, list)
//                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//                    binding.solutionSpinner.adapter = dataAdapter
//
//                    binding.solutionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//                        override fun onNothingSelected(parent: AdapterView<*>?) {
//                        }
//
//                        override fun onItemSelected(
//                            parent: AdapterView<*>?,
//                            view: View?,
//                            position: Int,
//                            id: Long
//                        ) {
//
//                            val solutionType = parent?.selectedItem as SolutionType
//                            solutionTypeId = solutionType.id.toString()
//
//                        }
//
//                    }
                }

                referenceViewModel.solutionTypecomplete()
            }
        })

        attachmentViewModel.uploadFileStatus.observe(viewLifecycleOwner, Observer {

            when (it) {

                FileUploadStatus.ERROR -> {
                    binding.coordinatorLayout.showSnackbar("File upload: something went wrong. ${attachmentViewModel.uploadErrorMessage}")
                }
            }

        })

        ticketViewModel.ticket.observe(viewLifecycleOwner, Observer {ticket ->
            if (ticket != null) {

                var imageAttachment1Url = ""
                var imageAttachment2Url = ""
                var imageAttachment3Url = ""

                var videoAttachment1Url = ""
                var videoAttachment2Url = ""
                var videoAttachment3Url = ""

                ticket.ticketAsset?.let {assets ->
                    val imageAttachments = assets.filter {
                        it.link.split(".")[1].contains("png") ||
                                it.link.split(".")[1].contains("jpg") }

                    val videoAttachments = assets.filter {
                        it.link.split(".")[1].contains("mp4") }

//                    for ((index, imageAttachment) in imageAttachments.withIndex()) {
//
//
//                        when (index) {
//
//                            0 -> {
//                                imageAttachment1Url = imageAttachment.link.substring(7, imageAttachment.link.length)
//                            }
//
//                            1 -> {
//                                imageAttachment2Url = imageAttachment.link.substring(7, imageAttachment.link.length)
//                            }
//
//                            2 -> {
//                                imageAttachment3Url = imageAttachment.link.substring(7, imageAttachment.link.length)
//                            }
//                        }
//
//                    }
//
//                    for ((index, videoAttachment) in videoAttachments.withIndex()) {
//
//                        when (index) {
//
//                            0 -> {
//                                videoAttachment1Url = videoAttachment.link.substring(7, videoAttachment.link.length)
//                            }
//
//                            1 -> {
//                                videoAttachment2Url = videoAttachment.link
//                            }
//
//                            2 -> {
//                                videoAttachment3Url = videoAttachment.link
//                            }
//                        }
//
//                    }

                    for ((index, imageAttachment) in attachmentViewModel.imageUrisTemp.withIndex()) {


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

                    for ((index, videoAttachment) in attachmentViewModel.videoUrisTemp.withIndex()) {

                        when (index) {

                            0 -> {
                                videoAttachment1Url = videoAttachment.toString()
                            }

//                            1 -> {
//                                videoAttachment2Url = videoAttachment.link
//                            }
//
//                            2 -> {
//                                videoAttachment3Url = videoAttachment.link
//                            }
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
            if (solutionTypeId != "0") {

                if (attachmentViewModel.videoUrisTemp.isEmpty() && attachmentViewModel.imageUrisTemp.isEmpty()) {
                    ticketViewModel.createTicket(
                        args.machineId,
                        if (args.problemTypeId == 0L) null else args.problemTypeId.toString(),
                        binding.remarksEditText.text.toString(),
                        DateTime(DateTimeZone.UTC).toString(),
                        null,
                        if (solutionTypeId == "0") null else solutionTypeId
                    )
                } else {
                    activity?.applicationContext?.let { it1 -> attachmentViewModel.uploadFile(it1) }
                }

            } else {
                binding.solutionSpinner.setBackgroundResource(R.drawable.bg_red_border)
                coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Please select a solution"))
            }

        }

        binding.btnCancel.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.clearImage.setOnClickListener {
            Log.d(TAG, "onCreateView: clearImage")

            attachmentViewModel.imageUrisTemp.forEach {uri ->
                uri.path?.let {
                    File(it).absoluteFile.delete()
                }

            }

            attachmentViewModel.imageUrisTemp.clear()
            attachmentViewModel.updateImageUris(attachmentViewModel.imageUrisTemp)
        }

        binding.clearVideo.setOnClickListener {

            attachmentViewModel.videoUrisTemp.forEach {uri ->
                uri.path?.let {
                    File(it).absoluteFile.delete()
                }
            }

            attachmentViewModel.videoUrisTemp.clear()
            attachmentViewModel.updateVideoUris(attachmentViewModel.videoUrisTemp)
        }

        attachmentViewModel.videoUris.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it.isNotEmpty()) {
                    binding.clearVideo.visibility = View.VISIBLE

                }

                when (it.size) {

                    3 -> {
                        binding.videoIcon1.visibility = View.VISIBLE
                        binding.videoIcon2.visibility = View.VISIBLE
                        binding.videoIcon3.visibility = View.VISIBLE
                    }

                    2 -> {
                        binding.videoIcon1.visibility = View.VISIBLE
                        binding.videoIcon2.visibility = View.VISIBLE
                        binding.videoIcon3.visibility = View.INVISIBLE
                    }

                    1 -> {
                        binding.videoIcon1.visibility = View.VISIBLE
                        binding.videoIcon2.visibility = View.INVISIBLE
                        binding.videoIcon3.visibility = View.INVISIBLE
                    }

                    else -> {
                        binding.clearVideo.visibility = View.INVISIBLE
                        binding.videoIcon1.visibility = View.INVISIBLE
                        binding.videoIcon2.visibility = View.INVISIBLE
                        binding.videoIcon3.visibility = View.INVISIBLE
                    }
                }
            }
        })

        attachmentViewModel.imageUris.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it.isNotEmpty()) {
                    binding.clearImage.visibility = View.VISIBLE

                }

                when (it.size) {

                    3 -> {
                        binding.imageIcon1.visibility = View.VISIBLE
                        binding.imageIcon2.visibility = View.VISIBLE
                        binding.imageIcon3.visibility = View.VISIBLE
                    }

                    2 -> {
                        binding.imageIcon1.visibility = View.VISIBLE
                        binding.imageIcon2.visibility = View.VISIBLE
                        binding.imageIcon3.visibility = View.INVISIBLE
                    }

                    1 -> {
                        binding.imageIcon1.visibility = View.VISIBLE
                        binding.imageIcon2.visibility = View.INVISIBLE
                        binding.imageIcon3.visibility = View.INVISIBLE
                    }

                    else -> {
                        binding.clearImage.visibility = View.INVISIBLE
                        binding.imageIcon1.visibility = View.INVISIBLE
                        binding.imageIcon2.visibility = View.INVISIBLE
                        binding.imageIcon3.visibility = View.INVISIBLE
                    }
                }
            }
        })

        ticketViewModel.status.observe(viewLifecycleOwner, Observer {
            when (it) {
                ApiStatus.LOADING -> {
                    loading.show(requireContext(), true, LoadingIndicator.CREATE_TICKET)
                }
                else -> {
                    loading.dismiss()
                }

            }
        })

        attachmentViewModel.status.observe(viewLifecycleOwner, Observer {
            when (it) {
                ApiStatus.LOADING -> {
                    loading.show(requireContext(), true, LoadingIndicator.UPLOAD_FILE)
                }
                else -> {
                    loading.dismiss()
                }

            }
        })

        // Start translation
        with(languageJsonObject) {
            with(binding) {
                toolBarTitleTextView.text = getTranslation(toolBarTitleTextView.text.toString())
                labelScanMachine.text = getTranslation(labelScanMachine.text.toString())
                labelSelectedProblem.text = getTranslation(labelSelectedProblem.text.toString()) + " *"
                labelSolution.text = getTranslation(labelSolution.text.toString()) + " *"
                labelRemarks.text = getTranslation(labelRemarks.text.toString())
                labelAddPicture.text = getTranslation(labelAddPicture.text.toString())
                labelAddVideo.text = getTranslation(labelAddVideo.text.toString())
                btnCancel.text = getTranslation(btnCancel.text.toString())
                btnSubmit.text = getTranslation(btnSubmit.text.toString())
            }
        }
        // End translation

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loading.onDismissListener { event ->
            if (event == LoadingIndicator.UPLOAD_FILE)
                attachmentViewModel.cancelUploadFileJob()
            if (event == LoadingIndicator.CREATE_TICKET)
                ticketViewModel.cancelTicketJob()
        }
    }

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"

        val mimeTypes = arrayOf("image/jpeg", "image/png")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        activity?.let {activity ->
            createImageFile()?.let {file ->
                cameraIntent.putExtra(
                    MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(
                    activity.applicationContext,
                    "${BuildConfig.APPLICATION_ID}.provider",
                    file
                ))
            }

        }


        val chooser = Intent(Intent.ACTION_CHOOSER)
        chooser.putExtra(Intent.EXTRA_INTENT, intent)
        chooser.putExtra(Intent.EXTRA_TITLE, "Select from:")

        val intentArray = arrayOf(cameraIntent)
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)

        startActivityForResult(chooser,
            co.ltlabs.ltmechanic.ui.main.lineleader.createticket.GALLERY_REQUEST_CODE
        )
    }

    private fun pickVideoFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "video/*"

        val mimeTypes = arrayOf("video/mp4")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)

        val cameraIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        activity?.let {activity ->
            createVideoFile()?.let {file ->
                cameraIntent.putExtra(
                    MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(
                    activity.applicationContext,
                    "${BuildConfig.APPLICATION_ID}.provider",
                    file
                ))
            }

        }


        val chooser = Intent(Intent.ACTION_CHOOSER)
        chooser.putExtra(Intent.EXTRA_INTENT, intent)
        chooser.putExtra(Intent.EXTRA_TITLE, "Select from:")

        val intentArray = arrayOf(cameraIntent)
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)

        startActivityForResult(chooser,
            co.ltlabs.ltmechanic.ui.main.lineleader.createticket.VIDEO_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                co.ltlabs.ltmechanic.ui.main.lineleader.createticket.GALLERY_REQUEST_CODE -> {
                    val imageUri = data?.data
                    val uri = imageUri ?: Uri.parse(cameraFilePath)
                    attachmentViewModel.imageUrisTemp.add(uri)
                    attachmentViewModel.updateImageUris(attachmentViewModel.imageUrisTemp)
                }

                co.ltlabs.ltmechanic.ui.main.lineleader.createticket.VIDEO_REQUEST_CODE ->{
                    val videoUri = data?.data
                    videoUri?.let {
                        val retriever = MediaMetadataRetriever()
                        retriever.setDataSource(activity?.applicationContext, videoUri)
                        val getDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        retriever.release()

                        val duration = getDuration.toLongOrNull()?.div(1000) ?: 0

                        if (duration > 30) {
                            coordinatorLayout.showSnackbar(languageJsonObject.getTranslation("Duration of video limit to 30 seconds"))
                        } else {
                            attachmentViewModel.videoUrisTemp.add(it)
                            attachmentViewModel.updateVideoUris(attachmentViewModel.videoUrisTemp)
                        }
                    }
                }
            }
        }
    }

    private fun createVideoFile(): File? {

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val videoFileName = "MP4_${timeStamp}_"

        activity?.let {
            val storageDir = File("${it.applicationContext.getExternalFilesDir(Environment.DIRECTORY_DCIM)}${File.separator}")
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
            val storageDir = File("${it.applicationContext.getExternalFilesDir(Environment.DIRECTORY_DCIM)}${File.separator}")
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
        val action = MechanicCreateTicketAttachFragmentDirections.actionMechanicCreateTicketAttachFragmentToMechanicCreateTicketPreviewFragment(
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
            origin
        )
        navigate(action)
    }

}
