package co.ltlabs.ltmechanic.ui.auth

class AuthResource<T> constructor(val status: AuthStatus?, val data: T? = null, val message: String? = null) {

    companion object {

        fun <T> authenticated(data: T?): AuthResource<T> =
            AuthResource(AuthStatus.AUTHENTICATED, data)

        fun <T> error(msg: String?, data: T?): AuthResource<T> =
            AuthResource(AuthStatus.ERROR, data, msg)

        fun <T> loading(data: T?): AuthResource<T> =
            AuthResource(AuthStatus.LOADING, data)

        fun <T> logout(): AuthResource<T> =
            AuthResource(AuthStatus.NOT_AUTHENTICATED)

    }

    enum class AuthStatus { AUTHENTICATED, ERROR, LOADING, NOT_AUTHENTICATED }
}