package co.ltlabs.ltmechanic.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import co.ltlabs.ltmechanic.repository.paging.Meta
import co.ltlabs.ltmechanic.util.Converters
import kotlinx.coroutines.flow.Flow

@Dao
interface CORequestDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meta: Meta)

    @Query("SELECT * FROM Meta WHERE status = :status")
    fun getMeta(status: String): Flow<Meta?>?

    @Query("DELETE from Meta")
    suspend fun deleteAll()

}

@Dao
interface MaintMetaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meta: co.ltlabs.ltmechanic.domain.maint.Meta)

    @Query("SELECT * FROM meta_maint WHERE type = :type")
    fun getMeta(type: String): Flow<co.ltlabs.ltmechanic.domain.maint.Meta?>?

    @Query("DELETE from meta_maint")
    suspend fun deleteAll()

}

@Dao
interface MfgAreaDao{

    @Query("select * from DatabaseMfgArea")
    fun getMfgAreas(): LiveData<List<DatabaseMfgArea>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg mfgLines: DatabaseMfgArea)

    @Query("delete from DatabaseMfgArea")
    fun deleteAll()

    @Transaction
    fun deleteAndCreate(username: String, vararg mfgLines: DatabaseMfgArea) {
        deleteAll()
        insertAll(*mfgLines)
    }
}

@Dao
interface MfgLineDao {
    @Query("select * from DatabaseMfgLine")
    fun getMfgLines(): LiveData<List<DatabaseMfgLine>>

    @Query("select * from DatabaseMfgLine")
    suspend fun getLinesAsync(): List<DatabaseMfgLine>

    @Query("select * from DatabaseMfgLine")
    fun getMfgLinesNotLiveData(): List<DatabaseMfgLine>

    @Query("select * from DatabaseMfgLine where username = :username")
    fun getMfgLinesByUsername(username: String): LiveData<List<DatabaseMfgLine>>

    @Query("select * from DatabaseMfgLine where mfgLineId = :id")
    suspend fun getMfgLinesById(id: Long): List<DatabaseMfgLine>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg mfgLines: DatabaseMfgLine)

    @Query("delete from DatabaseMfgLine")
    fun deleteAll()

    @Query("delete from DatabaseMfgLine where username = :username")
    fun deleteAllByUsername(username: String)

    @Transaction
    fun deleteAndCreate(username: String, vararg mfgLines: DatabaseMfgLine) {
        deleteAll()
        insertAll(*mfgLines)
    }

    @Delete
    fun deleteMfgLines(vararg mfgLines: DatabaseMfgLine)
}

@Dao
interface NotificationDao {
    @Query("select * from DatabaseNotification where username = :username order by createdDate DESC")
    fun getNotificationsByUsername(username: String): LiveData<List<DatabaseNotification>>

    @Query("select * from DatabaseNotification where username = :username order by createdDate DESC")
    fun getNotificationsByUsernameNotLiveData(username: String): List<DatabaseNotification>

    @Query("delete from DatabaseNotification where type = :type and ticketId = :ticketId and username = :username")
    fun deleteAllByTypeAndTicketId(type: String, ticketId: String, username: String)

    @Query("delete from DatabaseNotification where type = :type and millis = :millis")
    fun deleteAllByTypeAndMillis(type: String, millis: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg notifications: DatabaseNotification)

    @Delete
    fun delete(notification: DatabaseNotification)
}

@Dao
interface SnackBarActionDao {
    @Query("select * from DatabaseSnackBarAction")
    fun getSnackBarActions(): LiveData<List<DatabaseSnackBarAction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg snackBarAction: DatabaseSnackBarAction)

    @Query("delete from DatabaseSnackBarAction")
    fun deleteAll()
}

@Dao
interface FirebaseNotificationDao {
    @Query("select * from DatabaseFirebaseNotification")
    fun getFirebaseNotifications(): LiveData<List<DatabaseFirebaseNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg firebaseNotifications: DatabaseFirebaseNotification)

    @Query("delete from DatabaseFirebaseNotification")
    fun deleteAll()
}

@Dao
interface TranslationDao {
    @Query("select * from DatabaseTranslation")
    fun getTranslations(): LiveData<List<DatabaseTranslation>>

    @Query("select * from DatabaseTranslation where translationKey = :key")
    fun getTranslationByKey(key: String): LiveData<DatabaseTranslation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg translations: DatabaseTranslation)

    @Query("delete from DatabaseTranslation")
    fun deleteAll()
}

@Dao
interface NFCDao {
    @Query("select * from DatabaseNFC limit 1")
    fun getNfc(): LiveData<DatabaseNFC>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg nfc: DatabaseNFC)

    @Query("delete from DatabaseNFC")
    fun deleteAll()
}

@Dao
interface NFCDeviceDao {
    @Query("select * from DatabaseNFCDevice limit 1")
    fun getNfcDevice(): LiveData<DatabaseNFCDevice>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg nfcDevice: DatabaseNFCDevice)

    @Query("delete from DatabaseNFCDevice")
    fun deleteAll()
}

@Dao
interface AuthDetailsDao {
    @Query("select * from DatabaseAuthDetails limit 1")
    fun getAuthDetails(): LiveData<List<DatabaseAuthDetails>>

    @Query("select * from DatabaseAuthDetails limit 1")
    fun getAuthDetailsNotLiveData(): List<DatabaseAuthDetails>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg authDetails: DatabaseAuthDetails)

    @Query("delete from DatabaseAuthDetails")
    fun deleteAll()

}

@Dao
interface LanguageDao {
    @Query("select * from DatabaseLanguage limit 1")
    fun getLanguage(): LiveData<List<DatabaseLanguage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg languages: DatabaseLanguage)

    @Query("delete from DatabaseLanguage")
    fun deleteAll()
}

@Database(
    entities = [
        DatabaseMfgLine::class,
        DatabaseMfgArea::class,
        DatabaseSnackBarAction::class,
        DatabaseFirebaseNotification::class,
        DatabaseTranslation::class,
        DatabaseNFC::class,
        DatabaseNFCDevice::class,
        DatabaseAuthDetails::class,
        DatabaseLanguage::class,
        DatabaseNotification::class,
        Meta::class,
        co.ltlabs.ltmechanic.domain.maint.Meta::class
    ],
    version = 26
)
@TypeConverters(Converters::class)
abstract class LtMechDatabase : RoomDatabase() {
    abstract val mfgLineDao: MfgLineDao
    abstract val mfgAreaDao:MfgAreaDao
    abstract val snackBarActionDao: SnackBarActionDao
    abstract val firebaseNotificationDao: FirebaseNotificationDao
    abstract val translationDao: TranslationDao
    abstract val nfcDao: NFCDao
    abstract val nfcDeviceDao: NFCDeviceDao
    abstract val authDetailsDao: AuthDetailsDao
    abstract val languageDao: LanguageDao
    abstract val notificationDao: NotificationDao
    abstract val coRequestDao: CORequestDao
    abstract val maintDao: MaintMetaDao
}

private lateinit var INSTANCE: LtMechDatabase

fun getDatabase(context: Context): LtMechDatabase {
    if (!::INSTANCE.isInitialized) {
        INSTANCE = Room.databaseBuilder(
            context.applicationContext,
            LtMechDatabase::class.java,
            "ltmech"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    return INSTANCE
}