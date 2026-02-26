package co.ltlabs.ltmechanic.viewmodels.shared

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import co.ltlabs.ltmechanic.constant.AppConfig
import co.ltlabs.ltmechanic.database.DatabaseAuthDetails
import co.ltlabs.ltmechanic.database.getDatabase
import co.ltlabs.ltmechanic.domain.Factory
import co.ltlabs.ltmechanic.network.ApiGlobal
import co.ltlabs.ltmechanic.network.SwitchFactoryResponse
import co.ltlabs.ltmechanic.network.main.Auth2Api
import co.ltlabs.ltmechanic.network.main.ReferenceApi
import co.ltlabs.ltmechanic.network.main.dto.asFactoryDomainModel
import co.ltlabs.ltmechanic.repository.LtMechDatabaseRepository
import co.ltlabs.ltmechanic.service.DeviceTokenService
import co.ltlabs.ltmechanic.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ChangeFactoryViewModel";

class ChangeFactoryViewModel @Inject constructor(
    application: Application,
    val referenceApi: ReferenceApi,
    private val global: ApiGlobal
) : AndroidViewModel(application) {

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val database = getDatabase(application)
    private val ltMechDatabaseRepository = LtMechDatabaseRepository(database)

    val mfgLinesFromDatabase = ltMechDatabaseRepository.mfgLines

    private val _status = MutableLiveData<ApiStatus>()
    val status: LiveData<ApiStatus>
        get() = _status

    private val _factories = MutableLiveData<List<Factory>>()
    val factories: LiveData<List<Factory>>
        get() = _factories

    private val _changeFactoryStatus = MutableLiveData<ChangeFactoryStatus>()
    val changeFactoryStatus: LiveData<ChangeFactoryStatus>
        get() = _changeFactoryStatus

    fun factoriesComplete() {
        _factories.value = null
    }

    fun getAssignedFactories() {

        viewModelScope.launch {

            val getAssignedFactoriesDeferred =
                referenceApi.getAssignedFactoriesAsync("Bearer ${AuthUtil.token}")

            try {

                _status.value = ApiStatus.LOADING

                val result = getAssignedFactoriesDeferred.await()

                if (result.success) {

                    _factories.value = result.data.asFactoryDomainModel()

                } else {

                    _factories.value = null
                }

                _status.value = ApiStatus.DONE

            } catch (e: Exception) {

                Log.e(TAG, "getAssignedFactories: ", e)

                _status.value = ApiStatus.ERROR

            }

        }

    }

    fun switchFactory(context: Context, factoryId: Long) {

        viewModelScope.launch {

            try {

                _status.value = ApiStatus.LOADING
                val result = global.switchFactoryAsync(factoryId.toInt(), AuthUtil.token).await()
                if (result.userId != null) {
                    storeLocale(context, factoryId, result)
                    _changeFactoryStatus.value = ChangeFactoryStatus.SUCCESS
                    _status.value = ApiStatus.DONE
                }

            } catch (e: Exception) {
                _status.value = ApiStatus.ERROR
                _changeFactoryStatus.value = ChangeFactoryStatus.FAILED
            }
        }
    }

    private fun storeLocale(context: Context, factoryId: Long, data: SwitchFactoryResponse) {
        viewModelScope.launch {
            AuthUtil.apply {
                role = data.role ?: role
                token = data.accessToken ?: token
                this.factoryId = factoryId
            }

            SharePrefUtil.set(AppConfig.SP_USER_ROLE, AuthUtil.role)
            SharePrefUtil.set(AppConfig.SP_USER_TOKEN, AuthUtil.token)
            SharePrefUtil.set(AppConfig.SP_USER_REFRESH_TOKEN, data.refreshToken ?: "")
            val intent = Intent(context, DeviceTokenService::class.java)
            context.startService(intent)

            insertToAuthDetailsDatabase(
                arrayOf(
                    DatabaseAuthDetails(
                        username = AuthUtil.username,
                        role = data.role ?: AuthUtil.role,
                        token = data.accessToken ?: AuthUtil.token,
                        loggedIn = true,
                        tokenP = AuthUtil.password
                    )
                )
            )

            ltMechDatabaseRepository.deleteMfgLines()
            LineUtil.clearSelectedLine()
        }
    }

    private fun insertToAuthDetailsDatabase(authDetails: Array<DatabaseAuthDetails>) {
        viewModelScope.launch {
            ltMechDatabaseRepository.insertAuthDetails(authDetails)
        }
    }

    fun changeFactoryStatusComplete() {
        _changeFactoryStatus.value = null
    }

}