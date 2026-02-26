package co.ltlabs.ltmechanic.util

import co.ltlabs.ltmechanic.network.ErrorResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody

object ConvertErrorBody {
    val gson = Gson()

    fun toErrorResponse(errorBody: ResponseBody): String {
        val type = object : TypeToken<ErrorResponse>() {}.type
        val error: ErrorResponse = gson.fromJson(errorBody.charStream(), type)
        return error.message
    }
}