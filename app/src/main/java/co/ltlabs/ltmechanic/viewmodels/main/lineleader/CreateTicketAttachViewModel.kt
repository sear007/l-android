package co.ltlabs.ltmechanic.viewmodels.main.lineleader

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import co.ltlabs.ltmechanic.database.getDatabase
import co.ltlabs.ltmechanic.network.main.FileApi
import co.ltlabs.ltmechanic.network.main.ReferenceApi
import co.ltlabs.ltmechanic.network.main.dto.asFileResultDomainModel
import co.ltlabs.ltmechanic.network.main.dto.asSolutionTypeDomainModel
import co.ltlabs.ltmechanic.repository.LtMechDatabaseRepository
import co.ltlabs.ltmechanic.util.ApiStatus
import co.ltlabs.ltmechanic.util.AuthUtil
import co.ltlabs.ltmechanic.util.CompressorUtil
import co.ltlabs.ltmechanic.util.FileUploadStatus
import id.zelory.compressor.Compressor
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import co.ltlabs.ltmechanic.domain.FileResult as FileResultDomain
import co.ltlabs.ltmechanic.domain.SolutionType as SolutionTypeDomain

private const val TAG = "AttachViewModel";

class CreateTicketAttachViewModel @Inject constructor(
    private val fileApi: FileApi,
    private val referenceApi: ReferenceApi,
    application: Application
) : AndroidViewModel(application) {

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)
    private var uploadFileJob: Job? = null

    private val database = getDatabase(application)
    private val mfgLinesDatabaseRepository = LtMechDatabaseRepository(database)

    val mfgLinesFromDatabase = mfgLinesDatabaseRepository.mfgLines

    var uploadErrorMessage = ""

    val imageUrisTemp = mutableListOf<Uri>()

    private val _imageUris = MutableLiveData<List<Uri>>()
    val imageUris: LiveData<List<Uri>>
        get() = _imageUris

    val videoUrisTemp = mutableListOf<Uri>()

    private val _videoUris = MutableLiveData<List<Uri>>()
    val videoUris: LiveData<List<Uri>>
        get() = _videoUris

    private val _status = MutableLiveData<ApiStatus>()
    val status: LiveData<ApiStatus>
        get() = _status

    private val _uploadFileStatus = MutableLiveData<FileUploadStatus>()
    val uploadFileStatus: LiveData<FileUploadStatus>
        get() = _uploadFileStatus

    private val _fileResult = MutableLiveData<List<FileResultDomain>>()
    val fileResult: LiveData<List<FileResultDomain>>
        get() = _fileResult

    private val _solutionTypes = MutableLiveData<List<SolutionTypeDomain>>()
    val solutionTypes: LiveData<List<SolutionTypeDomain>>
        get() = _solutionTypes

    fun updateImageUris(uris: List<Uri>) {
        _imageUris.value = uris
    }

    fun updateVideoUris(uris: List<Uri>) {
        _videoUris.value = uris
    }

    fun uploadFile(context: Context) {
        uploadFileJob = viewModelScope.launch {
            _status.value = ApiStatus.LOADING
            val builder = MultipartBody.Builder()
            builder.setType(MultipartBody.FORM)
            for (imageUri in imageUrisTemp) {

                val file = createTempFile("tmp", ".jpg")
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val outputStream = FileOutputStream(file)
                val buffers = ByteArray(1024)
                var read = 0
                while ({
                        if (inputStream != null) {
                            read = inputStream.read(buffers)
                        };read
                    }() > 0) {

                    outputStream.write(buffers, 0, read)
                }
                inputStream?.close()
                outputStream?.close()

                val compressedImageFile = CompressorUtil.doImageCompressing(context, file)
                val requestImage = RequestBody.create(MediaType.parse("multipart/form-data"), compressedImageFile)
                builder.addFormDataPart(
                    "file",
                    file.name,
                    requestImage
                )
            }

            // Compress Video before upload
            if (videoUrisTemp.isNotEmpty()) {
                CompressorUtil.doVideoCompressing(context, videoUrisTemp) { path ->
                    val file = File(path)
                    builder.addFormDataPart(
                        "file",
                        file.name,
                        RequestBody.create(
                            MediaType.parse("multipart/form-data"),
                            file
                        )
                    )
                    uploadFile(builder)
                }
            } else {
                uploadFile(builder)
            }
        }
    }

    private fun uploadFile(builder: MultipartBody.Builder) {
        viewModelScope.launch {

            try {
                val result = fileApi.uploadFilesAsync(builder.build(), "Bearer ${AuthUtil.token}").await()
                _status.value = ApiStatus.DONE

                if (result.success) {
                    _fileResult.value = result.files.asFileResultDomainModel()
                    _uploadFileStatus.value = FileUploadStatus.SUCCESS

                } else {
                    _status.value = ApiStatus.ERROR
                    _uploadFileStatus.value = FileUploadStatus.FAILED
                }

            } catch (t: Throwable) {
                uploadErrorMessage = t.message.toString()
                _fileResult.value = null
                _uploadFileStatus.value = FileUploadStatus.ERROR
                _status.value = ApiStatus.ERROR
            }
        }

    }

    fun getSolutionTypes() {

        viewModelScope.launch {

            val getSolutionTypesDeferred =
                referenceApi.getSolutionTypesAsync(accessToken = "Bearer ${AuthUtil.token}")

            try {

                val result = getSolutionTypesDeferred.await()

                if (result.success) {

                    _solutionTypes.value = result.solutions.result.asSolutionTypeDomainModel()

                } else {
                    _solutionTypes.value = null
                }

            } catch (t: Throwable) {


                Log.e(TAG, "getSolutionTypes: ", t)

            }

        }

    }

    fun getSolutionTypesByProblemId(problemTypeId: Long) {

        Log.d(TAG, "getSolutionTypesByProblemId: problemTypeId: $problemTypeId")

        viewModelScope.launch {

            val getSolutionTypesDeferred = referenceApi.getSolutionTypesByProblemIdAsync(
                problemTypeId,
                accessToken = "Bearer ${AuthUtil.token}"
            )

            try {

                val result = getSolutionTypesDeferred.await()

                if (result.success) {

                    _solutionTypes.value = result.solutions.asSolutionTypeDomainModel()

                } else {
                    _solutionTypes.value = null
                }

            } catch (t: Throwable) {


                Log.e(TAG, "getSolutionTypes: ", t)

            }

        }

    }

    fun fileResultComplete() {
        _fileResult.value = null
    }

    fun solutionTypecomplete() {
        _solutionTypes.value = null
    }

    override fun onCleared() {
        super.onCleared()

        viewModelJob.cancel()
    }

    fun cancelUploadFileJob() {
        uploadFileJob?.cancel()
        uploadFileJob = null
    }
}