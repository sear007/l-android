package co.ltlabs.ltmechanic.viewmodels.shared

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.ltlabs.ltmechanic.database.getDatabase
import co.ltlabs.ltmechanic.domain.Notification
import co.ltlabs.ltmechanic.repository.LtMechDatabaseRepository
import co.ltlabs.ltmechanic.util.AuthUtil
import javax.inject.Inject

class NotificationViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val database = getDatabase(application)
    private val ltMechDatabaseRepository = LtMechDatabaseRepository(database)

    val notificationsFromDatabase = ltMechDatabaseRepository.notifications(AuthUtil.username)
    val mfgLinesFromDatabase = ltMechDatabaseRepository.mfgLines
}