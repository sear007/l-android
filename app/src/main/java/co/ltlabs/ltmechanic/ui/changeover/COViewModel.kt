package co.ltlabs.ltmechanic.ui.changeover

import android.app.Application
import androidx.lifecycle.*
import androidx.paging.PagingData
import co.ltlabs.ltmechanic.constant.type.COStatusType
import co.ltlabs.ltmechanic.database.getDatabase
import co.ltlabs.ltmechanic.domain.changeover.COItem
import co.ltlabs.ltmechanic.repository.LtMechDatabaseRepository
import co.ltlabs.ltmechanic.repository.co.CORepositoryImpl
import co.ltlabs.ltmechanic.util.SingleLiveEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class COViewModel @Inject constructor(
    private val repo: CORepositoryImpl, application: Application
) : AndroidViewModel(application) {

    private val database = getDatabase(application)
    private val mfgLinesDatabaseRepository = LtMechDatabaseRepository(database)
    val mfgLinesFromDatabase = mfgLinesDatabaseRepository.mfgLines
    private val _reloadCOList: MutableLiveData<Boolean> = SingleLiveEvent()
    val reloadCOList: LiveData<Boolean> = _reloadCOList

    private val _reloadCODetail: MutableLiveData<Boolean> = SingleLiveEvent()
    val reloadCODetail: LiveData<Boolean> = _reloadCODetail

    init {
        viewModelScope.launch {
            repo.database.coRequestDao.deleteAll()
        }
    }

    fun getCOList(status: String, lineSelected: String): Flow<PagingData<COItem>> {
        return repo.getCOList(status, lineSelected)
    }

    fun setReloadCOList() {
        _reloadCOList.postValue(true)
    }

    fun setReloadCODetail() {
        _reloadCODetail.postValue(true)
    }

    fun getPagingSizeOpenStatus() = repo.database.coRequestDao.getMeta(COStatusType.NEW)
    fun getPagingSizeReadyStatus() = repo.database.coRequestDao.getMeta(COStatusType.READY)
    fun getPagingSizeCloseStatus() = repo.database.coRequestDao.getMeta(COStatusType.CLOSED)

}