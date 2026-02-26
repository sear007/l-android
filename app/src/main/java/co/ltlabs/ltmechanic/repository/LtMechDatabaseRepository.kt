package co.ltlabs.ltmechanic.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import co.ltlabs.ltmechanic.database.*
import co.ltlabs.ltmechanic.domain.*
import co.ltlabs.ltmechanic.util.AuthUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

private const val TAG = "LtMechRepository";

class LtMechDatabaseRepository(private val database: LtMechDatabase) {

    val mfgLines: LiveData<List<MfgLine>> =
        Transformations.map(database.mfgLineDao.getMfgLines()) {
            it.asDomainModel()
        }

    val mfgAreas: LiveData<List<Areas>> =
        Transformations.map(database.mfgAreaDao.getMfgAreas()) {
            it.asDomainMfgAreaModel()
        }

//    val mfgLinesNotLiveData: List<MfgLine> =
//        database.mfgLineDao.getMfgLinesNotLiveData().asDomainModel()

    fun mfgLinesByUsername(username: String): LiveData<List<MfgLine>> {
        
        val mfgLineByUsername = Transformations.map(database.mfgLineDao.getMfgLinesByUsername(username)) {
            Log.d(TAG, "mfgLinesByUsername: mfgLineByUsername size: ${it.asDomainModel().size}")
            it.asDomainModel()
        }

        return mfgLineByUsername
    }

    suspend fun insertMfgLines(databaseMfgLines: Array<DatabaseMfgLine>) {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "insertMfgLines: databaseMfgLines size: ${databaseMfgLines.size}")
            database.mfgLineDao.deleteAll()
            Log.d(TAG, "insertMfgLines: AuthUtil.username: ${AuthUtil.username}")
//            database.mfgLineDao.deleteAllByUsername(AuthUtil.username)
//            Thread.sleep(1000)
//            database.mfgLineDao.insertAll(*databaseMfgLines)

            database.mfgLineDao.deleteAndCreate(AuthUtil.username, *databaseMfgLines)
        }
    }

    suspend fun insertMfgArea(databaseMfgAreas: Array<DatabaseMfgArea>) {
        withContext(Dispatchers.IO) {
            database.mfgAreaDao.deleteAll()
            database.mfgAreaDao.deleteAndCreate(AuthUtil.username, *databaseMfgAreas)
        }
    }

    suspend fun deleteMfgLines() {
        withContext(Dispatchers.IO) {
            database.mfgLineDao.deleteAll()
        }

    }

    val snackBarActions: LiveData<List<SnackBarAction>> =
        Transformations.map(database.snackBarActionDao.getSnackBarActions()) {
            it.asSnackBarActionDomainModel()
        }


    suspend fun insertSnackbarActions(dabaseSnackBarActions: Array<DatabaseSnackBarAction>) {
        withContext(Dispatchers.IO) {
            database.snackBarActionDao.deleteAll()
            database.snackBarActionDao.insertAll(*dabaseSnackBarActions)
        }
    }

    val firebaseNotifications: LiveData<List<FireBaseNotification>> =
        Transformations.map(database.firebaseNotificationDao.getFirebaseNotifications()) {
            it.asFirebaseNotificationDomainModel()
        }

    suspend fun insertFirebaseNotifications(databaseFirebaseNotification: Array<DatabaseFirebaseNotification>) {
        withContext(Dispatchers.IO) {
            database.firebaseNotificationDao.deleteAll()
            database.firebaseNotificationDao.insertAll(*databaseFirebaseNotification)
        }
    }

    suspend fun deleteNotification(databaseNotification: DatabaseNotification) {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "deleteNotification: databaseNotification.type: ${databaseNotification.type}")
            if (databaseNotification.type == "send_request") {
                database.notificationDao.deleteAllByTypeAndMillis(databaseNotification.type, databaseNotification.millis)
                database.notificationDao.deleteAllByTypeAndMillis(databaseNotification.type, databaseNotification.millis)
                database.notificationDao.deleteAllByTypeAndMillis(databaseNotification.type, databaseNotification.millis)
            } else {
                database.notificationDao.deleteAllByTypeAndTicketId(databaseNotification.type, databaseNotification.ticketId, AuthUtil.username)
                database.notificationDao.deleteAllByTypeAndTicketId(databaseNotification.type, databaseNotification.ticketId, AuthUtil.username)
                database.notificationDao.deleteAllByTypeAndTicketId(databaseNotification.type, databaseNotification.ticketId, AuthUtil.username)
            }
        }
    }

    suspend fun insertNotifications(databaseNotification: Array<DatabaseNotification>, username: String) {
        withContext(Dispatchers.IO) {

            if (databaseNotification.isNotEmpty()) {
                databaseNotification.forEach {

                    if (it.type != "send_request") {
                        Log.d(TAG, "insertNotifications: it.type: ${it.type}")
                        Log.d(TAG, "insertNotifications: it.ticketId: ${it.ticketId}")
                        database.notificationDao.deleteAllByTypeAndTicketId(
                            it.type,
                            it.ticketId,
                            username
                        )
                        database.notificationDao.deleteAllByTypeAndTicketId(
                            it.type,
                            it.ticketId,
                            username
                        )
                        database.notificationDao.deleteAllByTypeAndTicketId(
                            it.type,
                            it.ticketId,
                            username
                        )
//                    database.notificationDao.deleteAllByTypeAndTicketId(
//                        it.type,
//                        it.ticketId,
//                        AuthUtil.username
//                    )
//                    database.notificationDao.deleteAllByTypeAndTicketId(
//                        it.type,
//                        it.ticketId,
//                        AuthUtil.username
//                    )
                    } else {
                        database.notificationDao.deleteAllByTypeAndMillis(
                            it.type,
                            it.millis
                        )
                        database.notificationDao.deleteAllByTypeAndMillis(
                            it.type,
                            it.millis
                        )
                        database.notificationDao.deleteAllByTypeAndMillis(
                            it.type,
                            it.millis
                        )
                    }
//                val databaseNotificationObj = databaseNotification[0]
//                database.notificationDao.deleteAllByTypeAndTicketId(
//                    databaseNotificationObj.type,
//                    databaseNotificationObj.ticketId
//                )
//                database.notificationDao.deleteAllByTypeAndTicketId(
//                    databaseNotificationObj.type,
//                    databaseNotificationObj.ticketId
//                )
                    }


            }

            database.notificationDao.insertAll(*databaseNotification)
        }
    }

    fun notifications(username: String): LiveData<List<Notification>> =
        Transformations.map(database.notificationDao.getNotificationsByUsername(username)) {
            it.asNotificationDomainModel()
        }

//    fun notificationsNotLiveData(username: String): List<Notification> =
//        database.notificationDao.getNotificationsByUsernameNotLiveData(username).asNotificationDomainModel()

    val authDetails: LiveData<List<LoginDetails>> =
        Transformations.map(database.authDetailsDao.getAuthDetails()) {
            it.asLoginDetailsDomainModel()
        }

    suspend fun insertAuthDetails(databaseAuthDetails: Array<DatabaseAuthDetails>) {
        withContext(Dispatchers.IO) {
            database.authDetailsDao.deleteAll()
            database.authDetailsDao.insertAll(*databaseAuthDetails)
        }
    }

    val translations: LiveData<List<Translation>> =
        Transformations.map(database.translationDao.getTranslations()) {
            it.asTranslationListDomainModel()
        }

    fun translationByKey(key: String): LiveData<Translation> =
        Transformations.map(database.translationDao.getTranslationByKey(key)) {
            it.asTranslationDomainModel()
        }

    suspend fun insertTranslations(databaseTranslation: Array<DatabaseTranslation>) {
        withContext(Dispatchers.IO) {
            val startTime = Date().time
            Log.d(TAG, "database insert: startTime: $startTime")

            database.translationDao.deleteAll()
            database.translationDao.insertAll(*databaseTranslation)

            val endTime = Date().time
            Log.d(TAG, "database insert: endTime: $endTime")
            val elapsedTime = endTime - startTime

            Log.d(TAG, "database insert: elapsed time: ${elapsedTime } ms")
        }
    }

    val nfc: LiveData<NFCValue> =
        Transformations.map(database.nfcDao.getNfc()) {
            it?.asNfcDomainModel() ?: NFCValue("", false)
        }

    suspend fun insertNfc(databaseNFC: DatabaseNFC) {
        withContext(Dispatchers.IO) {
            database.nfcDao.deleteAll()
            database.nfcDao.insertAll(databaseNFC)
        }
    }

    val nfcDevice: LiveData<NFCDevice> =
        Transformations.map(database.nfcDeviceDao.getNfcDevice()) {
            it?.asNfcDeviceDomainModel() ?: NFCDevice(false)
        }

    suspend fun insertNfcDevice(databaseNFCDevice: DatabaseNFCDevice) {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "insertNfcDevice: databaseNFCDevice enabled: ${databaseNFCDevice.enabled}")
            database.nfcDeviceDao.deleteAll()
            database.nfcDeviceDao.insertAll(databaseNFCDevice)
        }
    }

    val language: LiveData<List<Language>> =
        Transformations.map(database.languageDao.getLanguage()) {
            it.asLanguageDomainModel()
        }

    suspend fun insertLanguage(databaseLanguage: Array<DatabaseLanguage>) {
        withContext(Dispatchers.IO) {
            database.languageDao.deleteAll()
            database.languageDao.insertAll(*databaseLanguage)
        }
    }
}