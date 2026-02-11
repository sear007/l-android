package co.ltlabs.ltmechanic.ui.main

class Resource<T> constructor(val status: Status?, val data: T? = null, val message: String? = null) {

    companion object {

        fun <T> success(data: T?): Resource<T> =
            Resource(
                Status.SUCCESS,
                data
            )

        fun <T> error(msg: String?, data: T?): Resource<T> =
            Resource(
                Status.ERROR,
                data,
                msg
            )

        fun <T> loading(data: T?): Resource<T> =
            Resource(
                Status.LOADING,
                data
            )


    }

    enum class Status { SUCCESS, ERROR, LOADING }
}