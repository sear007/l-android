package co.ltlabs.ltmechanic.ui.main.mechanic.reportedtickets

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.MediaController
import android.widget.PopupWindow
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import co.ltlabs.ltmechanic.BuildConfig
import co.ltlabs.ltmechanic.R

import co.ltlabs.ltmechanic.databinding.FragmentMechanicReportedTicketsConfirmBinding
import co.ltlabs.ltmechanic.databinding.PopupImagePreviewBinding
import co.ltlabs.ltmechanic.databinding.PopupVideoPreviewBinding
import co.ltlabs.ltmechanic.domain.Problem
import co.ltlabs.ltmechanic.domain.SolutionType
import co.ltlabs.ltmechanic.network.Asset
import co.ltlabs.ltmechanic.ui.main.lineleader.createticket.GALLERY_REQUEST_CODE
import co.ltlabs.ltmechanic.ui.main.lineleader.createticket.VIDEO_REQUEST_CODE
import co.ltlabs.ltmechanic.ui.main.mechanic.inrepairtickets.PhotoAdapter
import co.ltlabs.ltmechanic.util.*
import co.ltlabs.ltmechanic.util.dialog.LoadingIndicator
import co.ltlabs.ltmechanic.util.popup.DialogPopup
import co.ltlabs.ltmechanic.util.popup.SpinnerItem
import co.ltlabs.ltmechanic.viewmodels.ViewModelProviderFactory
import co.ltlabs.ltmechanic.viewmodels.main.mechanic.MechanicReportedTicketsConfirmViewModel
import co.ltlabs.ltmechanic.viewmodels.shared.*
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_create_ticket_preview.*
import kotlinx.android.synthetic.main.fragment_mechanic_reported_tickets_confirm.*
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class MechanicReportedTicketsConfirmFragment : DaggerFragment() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    @Inject
    lateinit var languageJsonObject: JSONObject

    @Inject
    lateinit var requestManager: RequestManager

    @Inject
    lateinit var loading: LoadingIndicator

    private lateinit var photoAdapter: PhotoAdapter

    private val viewModel: MechanicReportedTicketsConfirmViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(MechanicReportedTicketsConfirmViewModel::class.java)
    }

    private val referenceViewModel: ReferenceViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(ReferenceViewModel::class.java)
    }

    private val ticketViewModel: TicketViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(TicketViewModel::class.java)
    }

    private val attachmentViewModel: AttachmentViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(AttachmentViewModel::class.java)
    }

    private val solutionViewModel: SolutionViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(SolutionViewModel::class.java)
    }

    private val problemViewModel: ProblemViewModel by lazy {
        ViewModelProvider(this, providerFactory).get(ProblemViewModel::class.java)
    }

    private var cameraFilePath = ""

    private val args: MechanicReportedTicketsConfirmFragmentArgs by navArgs()

    private var solutionTypeId = "0"
    private var problemTypeId = "0"

    private lateinit var binding: FragmentMechanicReportedTicketsConfirmBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMechanicReportedTicketsConfirmBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

//        binding.remarksEditText.setText(args.remarks)
        binding.toolBarTitleTextView.text = args.ticketNo

        // Start translation
        with(languageJsonObject) {
            with(binding) {
                labelSelectedProblem.text = getTranslation(labelSelectedProblem.text.toString()) + " *"
                labelSolution.text = getTranslation(labelSolution.text.toString()) + " *"
                labelRemarks.text = getTranslation(labelRemarks.text.toString())
                labelAddPicture.text = getTranslation(labelAddPicture.text.toString())
                labelAddVideo.text = getTranslation(labelAddVideo.text.toString())
                btnCancel.text = getTranslation(btnCancel.text.toString())
                btnSubmit.text = getTranslation(btnSubmit.text.toString())
                clearImage.text = getTranslation(clearImage.text.toString())
                clearVideo.text = getTranslation(clearVideo.text.toString())
            }
        }
        // End translation

        if (args.problemTypeId == 0L) {
            referenceViewModel.getSolutionTypes()
        } else {
            referenceViewModel.getSolutionTypesByProblemId(args.problemTypeId)
        }

        ticketViewModel.getMachineProblems(args.machineId)

        binding.videoImageView.setOnClickListener {
            if (attachmentViewModel.videoUrisTemp.size == 1) {
                showSnackBar(binding.root,  languageJsonObject.getTranslation("You already attached 1 video"))
            } else {
                pickVideoFromGallery()
            }
        }

        binding.attachImage.setOnClickListener {
            if (attachmentViewModel.imageUrisTemp.size == 3) {
                showSnackBar(binding.root,  languageJsonObject.getTranslation("You already attached 3 images"))
            } else {
                pickFromGallery()
            }
        }

        binding.btnCancel.setOnClickListener {
            navigateToReportedTickets()
        }

        attachmentViewModel.fileResult.observe(viewLifecycleOwner, Observer {
            if (it != null) {

                val assets = mutableListOf<Asset>()

                for (file in it) {
                    assets.add(Asset(file.path))
                }

//                ticketViewModel.createTicket(
//                    args.machineId,
//                    args.problemTypeId.toString(),
//                    binding.remarksEditText.text.toString(),
//                    DateTime(DateTimeZone.UTC).toString(),
//                    assets,
//                    if (solutionTypeId == "0") null else solutionTypeId
//                )

//                ticketViewModel.updateTicketStatus(
//                    args.ticketNo,
//                    StatusIdUtil.RT_REPAIRED.toString(),
//                    binding.remarksEditText.text.toString(),
//                    if (solutionTypeId.isNotBlank()) solutionTypeId else null,
//                    if (problemTypeId.isNotBlank()) problemTypeId else null,
//                    assets
//                )

                ticketViewModel.getStatusIdAndUpdateTicketStatus(
                    TicketsStatus.REPAIRED,
                    TicketModule.REPAIR,
                    args.ticketNo,
                    binding.remarksEditText.text.toString(),
                    if (solutionTypeId.isNotBlank()) solutionTypeId else null,
                    if (problemTypeId.isNotBlank()) problemTypeId else null,
                    assets
                )

                attachmentViewModel.fileResultComplete()
            }
        })

        ticketViewModel.ticketUpdateStatus.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                when (it) {
                    TicketUpdateStatus.SUCCESS -> {
                        navigateToChecklist(args.ticketId, args.ticketNo)
                    }

                    TicketUpdateStatus.FAILED -> {

                    }
                }
                ticketViewModel.ticketUpdateComplete()
            }
        })

        binding.btnSubmit.setOnClickListener {
            if (problemTypeId != "0") {
                if (solutionTypeId != "0") {
                    if (attachmentViewModel.videoUrisTemp.isEmpty() && attachmentViewModel.imageUrisTemp.isEmpty()) {
//                ticketViewModel.createTicket(
//                    args.machineId,
//                    args.problemTypeId.toString(),
//                    binding.remarksEditText.text.toString(),
//                    DateTime(DateTimeZone.UTC).toString(),
//                    null,
//                    if (solutionTypeId == "0") null else solutionTypeId
//                )

//                        ticketViewModel.updateTicketStatus(
//                            args.ticketNo,
//                            StatusIdUtil.RT_REPAIRED.toString(),
//                            binding.remarksEditText.text.toString(),
//                            if (solutionTypeId.isNotBlank()) solutionTypeId else null,
//                            if (problemTypeId.isNotBlank()) problemTypeId else null,
//                            null
//                        )

                        ticketViewModel.getStatusIdAndUpdateTicketStatus(
                            TicketsStatus.REPAIRED,
                            TicketModule.REPAIR,
                            args.ticketNo,
                            binding.remarksEditText.text.toString(),
                            if (solutionTypeId.isNotBlank()) solutionTypeId else null,
                            if (problemTypeId.isNotBlank()) problemTypeId else null,
                            null
                        )

                    } else {
                        activity?.applicationContext?.let { it1 -> attachmentViewModel.uploadFile(it1) }
                    }
                } else {
                    binding.solutionSpinner.setBackgroundResource(R.drawable.bg_red_border)
                    showSnackBar(binding.root, languageJsonObject.getTranslation("Please select a solution"))
                }
            } else {
                binding.problemSpinner.setBackgroundResource(R.drawable.bg_red_border)
                showSnackBar(binding.root, languageJsonObject.getTranslation("Please select a problem"))
            }

        }

        attachmentViewModel.uploadFileStatus.observe(viewLifecycleOwner, Observer {

            when (it) {

                FileUploadStatus.ERROR -> {
                    showSnackBar(binding.root, "File upload: something went wrong. ${attachmentViewModel.uploadErrorMessage}")
                }
            }

        })

        binding.clearImage.setOnClickListener {

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

        attachmentViewModel.imageUris.observe(viewLifecycleOwner, Observer {
            photoAdapter.updateList(it)
            binding.clearImage.visibility = if (it.isEmpty()) View.INVISIBLE else View.VISIBLE
        })

        binding.problemSpinner.setOnClickListener {
            DialogPopup.show(binding.root, problemViewModel, resources.getString(R.string.select_item_of_problem), languageJsonObject)
        }

        problemViewModel.submitData.observe(viewLifecycleOwner, Observer { problem ->
            problemTypeId = problem.id.toString()
            referenceViewModel.getSolutionTypesByProblemId(problem.id)
            binding.problemSpinner.text = problem.name
        })

        binding.solutionSpinner.setOnClickListener {
            DialogPopup.show(binding.root, solutionViewModel, resources.getString(R.string.select_item_of_solution), languageJsonObject)
        }

        solutionViewModel.submitData.observe(viewLifecycleOwner, Observer { solutionType ->
            solutionTypeId = solutionType.id.toString()
            binding.solutionSpinner.text = solutionType.name
        })

        ticketViewModel.problems.observe(viewLifecycleOwner, Observer { problems ->

            Log.d(TAG, "onCreateView: problems: $problems")
            
            if (problems != null) {

                activity?.let {
                    val list = mutableListOf<Problem>()
                    list.add(Problem(0, "", false))
                    problems.forEach { problem ->
                        list.add(problem)
                    }

                    problemViewModel.items = list.filter { it.desc1 != "" }.map { solution ->
                        SpinnerItem(name = solution.desc1, checked = solution.checked ?: false, id = solution.problemTypeId)
                    }

                    list.forEachIndexed { _, problem ->
                        if (args.problem == problem.desc1) {
                            binding.problemSpinner.text = problem.desc1
                            problemTypeId = problem.problemTypeId.toString()
                        }
                    }

//                    val dataAdapter =  ArrayAdapter(it, android.R.layout.simple_spinner_item, list)
//                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//                    binding.problemSpinner.adapter = dataAdapter
//
//                    list.forEachIndexed { index, problem ->
//
//                        if (args.problem == problem.desc1) {
//                            binding.problemSpinner.setSelection(index)
//                        }
//
//                    }
//
//                    binding.problemSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
//                            val problem = parent?.selectedItem as Problem
//                            problemTypeId = problem.problemTypeId.toString()
//
//                            referenceViewModel.getSolutionTypesByProblemId(problemTypeId.toLong())
//
//                        }
//
//                    }
                }

            }
        })

        binding.problemSpinner.addTextChangedListener {
            this.run {
                binding.problemSpinner.setBackgroundResource(R.drawable.bg_white)
            }
        }
        binding.solutionSpinner.addTextChangedListener {
            this.run {
                binding.solutionSpinner.setBackgroundResource(R.drawable.bg_white)
            }
        }

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

        referenceViewModel.solutionTypes.observe(viewLifecycleOwner, Observer { solutionTypes ->
            if (solutionTypes != null) {

                activity?.let {
                    val list = mutableListOf<SolutionType>()
                    list.add(SolutionType(0, "", ""))
                    solutionTypes.forEach {solutiontype ->
                        list.add(solutiontype)
                    }

                    solutionViewModel.items = list.filter { it.desc != "" }.map { solution ->
                        SpinnerItem(name = solution.desc, id = solution.id)
                    }

                    list.forEachIndexed { _, solution ->
                        if (args.solutionTypeId == 0L && problemSpinner.text.isNotEmpty()) {
                            binding.solutionSpinner.text = solutionViewModel.items[0].name
                            solutionTypeId = solutionViewModel.items[0].id.toString()
                        } else {
                            if (args.solution == solution.desc) {
                                binding.solutionSpinner.text = solution.desc
                                solutionTypeId = solution.id.toString()
                            }
                        }
                    }

//                    val dataAdapter =  ArrayAdapter(it, android.R.layout.simple_spinner_item, list)
//                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//                    binding.solutionSpinner.adapter = dataAdapter
//
//                    list.forEachIndexed { index, solution ->
//
//                        if (args.solution == solution.desc) {
//                            binding.solutionSpinner.setSelection(index)
//                        }
//
//                    }
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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        setupPhotoList()
        setupListener()

        loading.onDismissListener { event ->
            if (event == LoadingIndicator.UPLOAD_FILE)
                attachmentViewModel.cancelUploadFileJob()
            if (event == LoadingIndicator.CREATE_TICKET)
                ticketViewModel.cancelTicketJob()
        }
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
            if (attachmentViewModel.videoUrisTemp.isNotEmpty()) {
                val uri = attachmentViewModel.videoUrisTemp[0]
                showAssetPopupWindow(binding.root, showVideoPreviewPopupWindow(uri))
            }
        }

        binding.ivPlayButton.setOnClickListener {
            val uri = attachmentViewModel.videoUrisTemp[0]
            showAssetPopupWindow(binding.root, showVideoPreviewPopupWindow(uri))
        }

        binding.videoImageView.setOnClickListener {
            if (attachmentViewModel.videoUrisTemp.size == 1) {
                showSnackBar(
                    binding.root,
                    languageJsonObject.getTranslation(languageJsonObject.getTranslation("You already attached 1 video"))
                )
            } else {
                pickVideoFromGallery()
            }
        }

        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.attachImage.setOnClickListener {
            if (attachmentViewModel.imageUrisTemp.size == 3) {
                showSnackBar(
                    binding.root,
                    languageJsonObject.getTranslation(languageJsonObject.getTranslation("You already attached 3 images"))
                )
            } else {
                pickFromGallery()
            }
        }

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

        startActivityForResult(chooser, GALLERY_REQUEST_CODE)
    }

    private fun pickVideoFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "video/*"

        val mimeTypes = arrayOf("video/mp4")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)

        val cameraIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        activity?.let {activity ->
            createVideoFile()?.let {file ->
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(
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

        startActivityForResult(chooser, VIDEO_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                GALLERY_REQUEST_CODE -> {
                    val imageUri = data?.data
                    val uri = imageUri ?: Uri.parse(cameraFilePath)
                    attachmentViewModel.imageUrisTemp.add(uri)
                    attachmentViewModel.updateImageUris(attachmentViewModel.imageUrisTemp)
                }

                VIDEO_REQUEST_CODE ->{
                    val videoUri = data?.data

                    videoUri?.let {


                        val retriever = MediaMetadataRetriever()
                        retriever.setDataSource(activity?.applicationContext, videoUri)
                        val getDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        retriever.release()

                        val duration = getDuration.toLongOrNull()?.div(1000) ?: 0

                        if (duration > 30) {
                            showSnackBar(binding.root, languageJsonObject.getTranslation("Duration of video limit to 30 seconds"))
                        } else {
                            attachmentViewModel.videoUrisTemp.add(it)
                            attachmentViewModel.updateVideoUris(attachmentViewModel.videoUrisTemp)
                        }

//                        activity?.let { activity ->
//                            viewModelScope.launch {
//                                Log.d(TAG, "onActivityResult: video path: ${FileUtil.getPath(activity, videoUri)}")
//                                FileUtil.getPath(activity, videoUri)?.let {filePath ->
//                                    val file = File(filePath)
//                                    Log.d(TAG, "onActivityResult: original video size: ${file.length()}")
//                                    val destinationPath = filePath.replace(
//                                        filePath.split("/")[filePath.split("/").size - 1], "")
////                                    val destinationPath = activity.applicationContext.getExternalFilesDir(null)?.absolutePath
//                                    Log.d(TAG, "onActivityResult: destinationPath: $destinationPath")
//
//                                    val compressedFilePath = SiliCompressor.with(activity).compressVideo(filePath, destinationPath)
//                                    Log.d(TAG, "onActivityResult: compressedFilePath: $compressedFilePath")
//                                    val compressedVideoFile = File(compressedFilePath)
//                                    Log.d(TAG, "onActivityResult: compressed video file size: ${compressedVideoFile.length()}")
//
//                                    val originalFileSize = file.length().toDouble()
//                                    val compressedFileSize = compressedVideoFile.length().toDouble()
//
//                                    val reducedPercent = ((compressedFileSize / originalFileSize) - 1) * 100
//                                    Log.d(TAG, "onActivityResult: Video file reduced by ${abs(reducedPercent.toInt())}%")
//
//                                    mainModelScope.launch {
//                                        viewModel.videoUrisTemp.add(Uri.fromFile(compressedVideoFile))
//                                        viewModel.updateVideoUris(viewModel.videoUrisTemp)
//                                    }
////                                    val compressedVideoFile = Compressor.compress(activity, file)
////
//////                                    viewModel.imageUrisTemp.add(Uri.fromFile(compressedImageFile))
//////                                    viewModel.updateImageUris(viewModel.imageUrisTemp)
////
////                                    Log.d(TAG, "onActivityResult: file: ${file.length()}")
////                                    Log.d(TAG, "onActivityResult: compressed file: ${compressedVideoFile.length()}")
//                                }
//
//                            }
//                        }

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

    private fun navigateToChecklist(ticketId: Long, ticketNo: String) {
        val action = MechanicReportedTicketsConfirmFragmentDirections
            .actionMechanicReportedTicketsConfirmFragmentToMechanicReportedTicketsChecklistFragment(ticketId, ticketNo)
        navigate(action)
    }

    private fun navigateToReportedTickets() {
        val action = MechanicReportedTicketsConfirmFragmentDirections
            .actionMechanicReportedTicketsConfirmFragmentToMechanicReportedTicketsFragment()
        navigate(action)
    }

}
