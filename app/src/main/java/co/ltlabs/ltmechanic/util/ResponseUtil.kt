package co.ltlabs.ltmechanic.util

import java.lang.Exception

sealed class ResponseUtil<T>(
    val data: T? = null, val message: String? = null, val exception: Exception? = null) {

    class Success<T>(data: T) : ResponseUtil<T>(data)

    class Error<T>(
        message: String, exception: Exception? = null, data: T? = null
    ) : ResponseUtil<T>(data, message, exception)

    class Loading<T> : ResponseUtil<T>()

}