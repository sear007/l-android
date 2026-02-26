package co.ltlabs.ltmechanic.util

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import timber.log.Timber
import java.io.*
import java.lang.Exception
import java.lang.NumberFormatException
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "FileUtil";

class FileUtil {

    companion object {
        private var contentUri: Uri? = null

//        @RequiresApi(Build.VERSION_CODES.O)
        fun getPath(context: Context, uri: Uri): String? {

            val isKitkat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
            var selection: String? = null
            var selectionArgs: MutableList<String>? = null

            if (isKitkat && DocumentsContract.isDocumentUri(context, uri)) {
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":")
                    val type = split[0]

                    val fullPath = getPathFromExtSD(split.toTypedArray())
                    if (fullPath.isNotBlank()) {
                        return fullPath
                    } else {
                        return null
                    }
                } else if (isDownloadsDocument(uri)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        var id = ""
                        var cursor: Cursor? = null
                        try {
                            cursor = context.contentResolver?.query(uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)
                            if (cursor != null && cursor.moveToFirst()) {
                                val fileName = cursor.getString(0)
                                val path = "${Environment.getExternalStorageDirectory().toString()}/Download/$fileName"
                                if (path.isNotBlank()) {
                                    return path
                                }
                            }
                        } finally {
                            cursor?.close()
                        }

                        id = DocumentsContract.getDocumentId(uri)
                        if (id.isNotBlank()) {
                            if (id.startsWith("raw:")) {
                                return id.replaceFirst("raw:", "")
                            }
                            val contentUriPrefixesToTry = arrayOf("content://downloads/public_downloads", "content://downloads/my_downloads")
                            for (contentUriPrefix in contentUriPrefixesToTry) {
                                return try {
                                    val contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), id.toLong())

                                    getDataColumn(context, contentUri, null, null)
                                } catch (e: NumberFormatException) {
                                    e.printStackTrace()

                                    uri.path?.replaceFirst("^/document/raw:", "")?.replaceFirst("^raw:", "")

                                }
                            }
                        }
                    } else {
                        val id = DocumentsContract.getDocumentId(uri)
                        val isOreo = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        if (id.startsWith("raw:")) {
                            return id.replaceFirst("raw:", "")
                        }
                        try {
                            contentUri = ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"),
                                id.toLong()
                            )
                        } catch (e: NumberFormatException){
                            e.printStackTrace()
                        }

                        if (contentUri != null) {
                            return getDataColumn(context, contentUri!!, null, null)
                        }
                    }
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":")
                    val type = split[0]

                    var contentUri: Uri? = null

                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }

                    selection = "_id=?"
                    Log.d(TAG, "getPath: split: $split")
//                    selectionArgs?.add(split[1])
//                    val selectionArgs = arrayOf(split[1])

                    return contentUri?.let { getDataColumn(context, it, selection, arrayOf(split[1])) }
                } else if (isGoogleDriveUri(uri)) {
                    return getDriveFilePath(uri, context)
                }
            } else if ("content" == uri.scheme?.toLowerCase()) {
                if (isGooglePhotosUri(uri)) {
                    return uri.lastPathSegment
                }

                if (isGoogleDriveUri(uri)) {
                    return getDriveFilePath(uri, context)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    return getMediaFilePathForN(uri, context)
                } else {
                    return getDataColumn(context, uri, null, null)
                }
            } else if("file" == uri.scheme?.toLowerCase()) {
                return uri.path
            }

            return null

        }

        private fun getPathFromExtSD(pathData: Array<String>): String {
            val type = pathData[0]
            val relativePath = "/${pathData[1]}"
            var fullPath = ""

            if ("primary" == type) {
                fullPath = "${Environment.getExternalStorageDirectory()}$relativePath"
                if (fileExists(fullPath)) {
                    return fullPath
                }
            }

            fullPath = "${System.getenv("SECONDARY_STORAGE")}$relativePath"
            if (fileExists(fullPath)) {
                return fullPath
            }

            fullPath = "${System.getenv("EXTERNAL_STORAGE")}$relativePath"
            if (fileExists(fullPath)) {
                return fullPath
            }

            return fullPath
        }

        private fun getDriveFilePath(uri: Uri, context: Context): String {
            val returnUri = uri
            val returnCursor = context.contentResolver.query(returnUri, null, null, null, null)

            val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = returnCursor?.getColumnIndex(OpenableColumns.SIZE)
            returnCursor?.moveToFirst()
            val name = nameIndex?.let { returnCursor.getString(it) }
            val size = sizeIndex?.let { returnCursor.getLong(it) }
            val file = File(context.cacheDir, name)

            try {

                val inputStream = context.contentResolver.openInputStream(uri)
                val outputStream = FileOutputStream(file)
                var read = 0
                val maxBufferSize = 1 * 1024 * 1024
                val bytesAvailable = inputStream?.available()

                val bufferSize = bytesAvailable?.coerceAtMost(maxBufferSize)

                val buffers = bufferSize?.let { ByteArray(it) }

                while ({
                        if (inputStream != null) {
                            read = inputStream.read(buffers)
                        };read
                        }() != -1) {

                    outputStream.write(buffers, 0, read)
                }
                inputStream?.close()
                outputStream?.close()

            } catch (e: Exception) {
                e.printStackTrace()
            }

            returnCursor?.close()

            return file.path

        }

        private fun getMediaFilePathForN(uri: Uri, context: Context): String {
            val returnUri = uri
            val returnCursor = context.contentResolver?.query(returnUri, null, null, null, null)

            val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = returnCursor?.getColumnIndex(OpenableColumns.SIZE)
            returnCursor?.moveToFirst()
            val name = nameIndex?.let { returnCursor.getString(it) }
            val size = sizeIndex?.let { returnCursor.getLong(it) }
            val file = File(context.cacheDir, name)

            try {

                val inputStream = context.contentResolver.openInputStream(uri)
                val outputStream = FileOutputStream(file)
                var read = 0
                val maxBufferSize = 1 * 1024 * 1024
                val bytesAvailable = inputStream?.available()

                val bufferSize = bytesAvailable?.coerceAtMost(maxBufferSize)

                val buffers = bufferSize?.let { ByteArray(it) }

                while ({
                        if (inputStream != null) {
                            read = inputStream.read(buffers)
                        };read
                    }() != -1) {

                    outputStream.write(buffers, 0, read)
                }
                inputStream?.close()
                outputStream?.close()

            } catch (e: Exception) {
                e.printStackTrace()
            }

            returnCursor?.close()

            return file.path
        }

        private fun getDataColumn(context: Context, uri: Uri, selection: String?, selectionArgs: Array<String>?): String? {

            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(column)

            try {
                cursor = context.contentResolver?.query(uri, projection, selection, selectionArgs, null)

                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(index)
                }
            } finally {
                cursor?.close()
            }
            return null
        }

        private fun fileExists(filePath: String): Boolean {
            return File(filePath).exists()
        }

        private fun isExternalStorageDocument(uri: Uri): Boolean =
            "com.android.externalstorage.documents" == uri.authority

        private fun isDownloadsDocument(uri: Uri): Boolean =
            "com.android.providers.downloads.documents" == uri.authority

        private fun isMediaDocument(uri: Uri): Boolean =
            "com.android.providers.media.documents" == uri.authority

        private fun isGooglePhotosUri(uri: Uri): Boolean =
            "com.google.android.apps.photos.content" == uri.authority

        private fun isGoogleDriveUri(uri: Uri): Boolean =
            "com.google.android.apps.docs.storage" == uri.authority

        val simpleDateFormat = SimpleDateFormat("yyyymmddhhmmss", Locale.getDefault())

        @Throws(IOException::class)
        fun getCompressed(context: Context, path: String): File {

            Log.d(TAG, "getCompressed: path: $path")

            val fileExtension = path.split(".")[path.split(".").size - 1]
            Log.d(TAG, "getCompressed: fileExtension: $fileExtension")

            var cacheDir = context.externalCacheDir
            if (cacheDir == null) {
                cacheDir = context.cacheDir
            }

            val rootDir = "${cacheDir?.absolutePath}/ImageCompressor"
            val root = File(rootDir)

            if (!root.exists()) {
                root.mkdirs()
            }

            val bitmap = decodeImageFromFiles(path, 300, 300)

            val compressed = File(root, "${simpleDateFormat.format(Date())}.$fileExtension")
            Log.d(TAG, "getCompressed: compressed file name: ${compressed.name}")

            val byteArrayOutputStream = ByteArrayOutputStream()

            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)

            val fileOutputStream = FileOutputStream(compressed)
            fileOutputStream.write(byteArrayOutputStream.toByteArray())
            fileOutputStream.flush()

            fileOutputStream.close()

            return compressed
        }

        private fun decodeImageFromFiles(path: String, width: Int, height: Int): Bitmap {
            val scaleOptions = BitmapFactory.Options()
            scaleOptions.inJustDecodeBounds = true
            BitmapFactory.decodeFile(path, scaleOptions)
            var scale = 1
            while (scaleOptions.outWidth / scale / 2 >= width
                && scaleOptions.outHeight / scale / 2 >= height) {

                scale *= 2
            }

            val outOptions = BitmapFactory.Options()
            outOptions.inSampleSize = scale
            return ExifUtil.rotateBitmap(path, BitmapFactory.decodeFile(path, outOptions))
        }

        fun writeResponseBodyToDisk(body: ResponseBody, context: Context): Boolean {

            try {

                val jsonfile = File("${context.getExternalFilesDir(null)}${File.separator}language.json")

                var inputStream: InputStream? = null
                var outputStream: OutputStream? = null

                try {

                    val fileReader = ByteArray(4096)

                    val fileSize = body.contentLength()
                    var fileSizeDownloaded = 0

                    inputStream = body.byteStream()
                    outputStream = FileOutputStream(jsonfile)

                    while (true) {
                        val read = inputStream.read(fileReader)

                        if (read == -1) {
                            break
                        }

                        outputStream.write(fileReader, 0, read)

                        fileSizeDownloaded += read

                        Log.d(TAG, "writeResponseBodyToDisk: file download $fileSizeDownloaded of $fileSize")
                    }

                    outputStream.flush()

                    return true

                } catch (e: IOException) {
                    return false
                } finally {
                    inputStream?.close()
                    outputStream?.close()
                }

            } catch (e: IOException) {
                Log.e(TAG, "writeResponseBodyToDisk: ", e)
                return false
            }
        }

        suspend fun writeToDisk(content: String, context: Context) : Boolean{
            return withContext(Dispatchers.IO){
                try {
                    val fileName = "${context.getExternalFilesDir(null)}${File.separator}language.json"
                    val file = File(fileName)
                    if (file.exists()) file.delete()
                    val boo = file.createNewFile()
                    if(!boo){
                        Timber.e("Unable create file language.json")
                        false
                    }else{
                        file.bufferedWriter().use { out ->
                            out.write(content)
                        }
                        true
                    }
                } catch (e: IOException) {
                    Timber.e(e.localizedMessage)
                    false
                }
            }
        }
    }


}

