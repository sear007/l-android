package co.ltlabs.ltmechanic.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ltlabs.ltmechanic.constant.type.AccessType
import co.ltlabs.ltmechanic.domain.AccessItem
import co.ltlabs.ltmechanic.repository.peraccess.PerAccessRepoImpl
import co.ltlabs.ltmechanic.ui.main.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PerAccessViewModel @Inject constructor(
    private val repo: PerAccessRepoImpl
) : ViewModel() {

    private val _perAccessResponse: MutableSharedFlow<Resource<AccessItem>> = MutableSharedFlow()
    val perAccessResponse: SharedFlow<Resource<AccessItem>> = _perAccessResponse

    val productConfig = repo.getProductConfig()

    fun getPerAccess() {
        viewModelScope.launch {
            _perAccessResponse.emit(Resource.loading(null))
            try {
                val result = repo.getProductAccessAsync().await()
                val data = result.data
                if (data != null) {
                    val item = data.access?.find {
                        it?.page == AccessType.CANCEL_TICKET
                    }
                    _perAccessResponse.emit(Resource.success(item))
                }
            } catch (e: Exception) {
                _perAccessResponse.emit(Resource.error(e.localizedMessage.toString(), null))
            }
        }
    }

}