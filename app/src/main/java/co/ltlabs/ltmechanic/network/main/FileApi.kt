package co.ltlabs.ltmechanic.network.main

import co.ltlabs.ltmechanic.network.FileUploadResponse
import co.ltlabs.ltmechanic.network.LanguagesResponse
import co.ltlabs.ltmechanic.util.FILE_API_ADDED_URL
import kotlinx.coroutines.Deferred
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface FileApi {

    @POST("$FILE_API_ADDED_URL/api/files")
    fun uploadFilesAsync(
        @Body file: RequestBody,
        @Header("Authorization") accessToken: String,
        @Header("is-compress") compress: Int = 1
    ): Deferred<FileUploadResponse>

    @GET("$FILE_API_ADDED_URL/api/local/files/language-en.json")
    fun getLanguageFileAsync():
            Deferred<ResponseBody>

    @GET("$FILE_API_ADDED_URL/api/languages")
    fun getLanguagesAsync():
            Deferred<LanguagesResponse>

    @GET("$FILE_API_ADDED_URL/api/languages/translation")
    fun getTranslationsAsync(
        @Query("language") language: String,
        @Query("factory") factory: String,
        @Query("version") version: String
    ):
            Deferred<ResponseBody>
}