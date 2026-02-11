package co.ltlabs.ltmechanic.viewmodels.main.lineleader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.ltlabs.ltmechanic.database.getDatabase
import co.ltlabs.ltmechanic.domain.RequestType
import co.ltlabs.ltmechanic.repository.LtMechDatabaseRepository
import javax.inject.Inject

class SendRequestViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val database = getDatabase(application)
    private val mfgLinesDatabaseRepository = LtMechDatabaseRepository(database)

    val mfgLinesFromDatabase = mfgLinesDatabaseRepository.mfgLines

    private val _selectedRequestType = MutableLiveData<RequestType>()
    val selectedRequestType: LiveData<RequestType>
        get() = _selectedRequestType

//    val requestTypes = listOf(
//        RequestType (
//            0,
//            "Change machine"
//        ),
//        RequestType (
//            1,
//            "Request inspection"
//        ),
//        RequestType (
//            2,
//            "Move machine"
//        ),
//        RequestType (
//            3,
//            "Request maintenance"
//        )
//    )

    fun setSelectedRequestType(requestType: RequestType) {
        _selectedRequestType.value = requestType
    }

    fun selectedRequestTypeComplete() {
        _selectedRequestType.value = null
    }
}