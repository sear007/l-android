package co.ltlabs.ltmechanic.viewmodels.shared

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.ltlabs.ltmechanic.database.getDatabase
import co.ltlabs.ltmechanic.network.ChangePasswordRequest
import co.ltlabs.ltmechanic.network.Error
import co.ltlabs.ltmechanic.network.Error4
import co.ltlabs.ltmechanic.network.main.Auth2Api
import co.ltlabs.ltmechanic.repository.LtMechDatabaseRepository
import co.ltlabs.ltmechanic.util.ApiStatus
import co.ltlabs.ltmechanic.util.AuthUtil
import co.ltlabs.ltmechanic.util.ChangePasswordStatus
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

private const val TAG = "ChangePasswordViewModel";

class ChangePasswordViewModel @Inject constructor(
    application: Application,
    private val authApi: Auth2Api
) : AndroidViewModel(application) {

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val database = getDatabase(application)
    private val ltMechDatabaseRepository = LtMechDatabaseRepository(database)

    val mfgLinesFromDatabase = ltMechDatabaseRepository.mfgLines

    private val _changePasswordStatus = MutableLiveData<ChangePasswordStatus>()
    val changePasswordStatus: LiveData<ChangePasswordStatus>
        get() = _changePasswordStatus

    private val _status = MutableLiveData<ApiStatus>()
    val status: LiveData<ApiStatus>
        get() = _status

    fun changePassword(newPassword: String, currentPassword: String) {

        Log.d(TAG, "changePassword: userId: ${AuthUtil.userId}")
        Log.d(TAG, "changePassword: password: $newPassword")

        viewModelScope.launch {

            val changePasswordDeferred = authApi.changePasswordAsync(
                ChangePasswordRequest(
                    currentPassword,
                    newPassword
                ),
                AuthUtil.userId,
                "Bearer ${AuthUtil.token}"
            )

            try {

                _status.value = ApiStatus.LOADING

                val result = changePasswordDeferred.await()

                if (result.result.contains("success")) {
                    _changePasswordStatus.value = ChangePasswordStatus.SUCCESS
                } else {
                    _changePasswordStatus.value = ChangePasswordStatus.FAILED
                }

                _status.value = ApiStatus.DONE

            } catch (t: Throwable) {

                if (t is HttpException) {

                    val error = t.response()?.errorBody()?.string()

                    val gson = Gson()
                    val errorObj: String? = error
                    val errorJson = gson.fromJson(errorObj, Error4::class.java)

                    Log.d(TAG, "changePassword: error: $error")
                    Log.d(TAG, "changePassword: errorObj: $errorJson")
//                    Log.d(TAG, "checkInMachine: errorObj: $errorObj")


                    if (errorJson.error?.contains("Your current password is incorrect") == true) {
                        _changePasswordStatus.value = ChangePasswordStatus.INCORRECT_PASSWORD
                    }  else if (errorJson.error?.contains("New password must not be the same with your current password") == true) {
                        _changePasswordStatus.value = ChangePasswordStatus.SAME_PASSWORD
                    }

                } else {
                    _changePasswordStatus.value = ChangePasswordStatus.FAILED
                }

                _status.value = ApiStatus.ERROR
                _changePasswordStatus.value = ChangePasswordStatus.FAILED

                Log.e(TAG, "changePassword: ", t)
            }

        }

    }

    fun changePasswordStatusComplete() {
        _changePasswordStatus.value = null
    }

}