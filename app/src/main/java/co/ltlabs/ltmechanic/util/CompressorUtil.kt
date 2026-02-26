package co.ltlabs.ltmechanic.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.abedelazizshe.lightcompressorlibrary.CompressionListener
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.abedelazizshe.lightcompressorlibrary.VideoQuality
import com.abedelazizshe.lightcompressorlibrary.config.Configuration
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File


object CompressorUtil {

    suspend fun doVideoCompressing(
        context: Context,
        videoPathUri: List<Uri>,
        callback: ((path: String) -> Unit)
    ) {
        withContext(Dispatchers.IO) {
            VideoCompressor.start(
                context = context,
                videoPathUri,
                isStreamable = true,
                saveAt = Environment.DIRECTORY_DOCUMENTS,
                listener = object : CompressionListener {
                    override fun onProgress(index: Int, percent: Float) {

                    }

                    override fun onStart(index: Int) {

                    }

                    override fun onSuccess(index: Int, size: Long, path: String?) {
                        try {
                            path?.let { p ->
                                val returnPath = if (!p.contains(".mp4")) {
                                    val originalFile = File(p)
                                    originalFile.renameTo(File("${p}.mp4"))
                                    "${p}.mp4"
                                } else {
                                    p
                                }
                                callback.invoke(returnPath)
                            }
                        } catch (e: Exception) {
                            Timber.tag("CompressorUtil").e(e)
                        }
                    }

                    override fun onFailure(index: Int, failureMessage: String) {
                        Timber.tag("CompressorUtil").e(failureMessage)
                    }

                    override fun onCancelled(index: Int) {
                        Timber.tag("CompressorUtil").i("compression has been cancelled")
                    }
                },
                configureWith = Configuration(
                    quality = VideoQuality.LOW,
                    isMinBitrateCheckEnabled = true,
                )
            )
        }
    }

    suspend fun doImageCompressing(context: Context, imageFile: File) =
        Compressor.compress(context, imageFile)
}