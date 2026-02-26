package co.ltlabs.ltmechanic.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import co.ltlabs.ltmechanic.domain.*
import java.util.*

@Entity
data class DatabaseMfgArea(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val mfgFloorId: Int? = 0,
    val mfgArea: String? = "",
    val mfgName: String? = "",
    var mfgDescription: String? = "",
    val mfgAreaId: Int? = 0,
    val mfgUserName:String?="",
    var isSelected: Boolean = true
)

@Entity
data class DatabaseMfgLine(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val mfgLineId: Long,
    val mfgLine: String,
    val mfgLineName: String,
    var seq: Int,
    var checked: Boolean = true,
    val username: String
)

@Entity
data class DatabaseSnackBarAction(
    @PrimaryKey
    val id: Int,
    val action: String,
    val show: Boolean
)

@Entity
data class DatabaseFirebaseNotification(
    @PrimaryKey
    val id: Int,
    val token: String
)

@Entity
data class DatabaseTranslation(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val translationKey: String,
    val translationValue: String
)

@Entity
data class DatabaseNFC(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val rfid: String,
    val newRfid: Boolean
)

@Entity
data class DatabaseNFCDevice(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val enabled: Boolean
)

@Entity
data class DatabaseAuthDetails(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val role: String,
    val token: String,
    val loggedIn: Boolean,
    val tokenP: String
)

@Entity
data class DatabaseLanguage(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val selectedLanguage: String
)

@Entity
data class DatabaseNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val createdDate: Date? = null,
    val message: String,
    val type: String,
    val ticketId: String,
    val generatedDate: String,
    val location: String,
    val machineNo: String,
    val rfid: String,
    val subType: String,
    val millis: String
)

fun List<DatabaseNotification>.asNotificationDomainModel(): List<Notification> {
    return map {
        Notification(
            id = it.id,
            username = it.username,
            dateTime = it.createdDate ?: Date(),
            message = it.message,
            type = it.type,
            ticketId = it.ticketId,
            generatedDate = it.generatedDate,
            location = it.location,
            machineNo = it.machineNo,
            rfid = it.rfid,
            subType = it.subType,
            millis = it.millis
        )
    }
}

fun List<DatabaseLanguage>.asLanguageDomainModel(): List<Language> {
    return map {
        Language(
            "",
            it.selectedLanguage,
            "",
            ""
        )
    }
}

fun List<DatabaseAuthDetails>.asLoginDetailsDomainModel(): List<LoginDetails> {
    return map {
        LoginDetails(
            it.username,
            it.role,
            it.token,
            it.loggedIn,
            it.tokenP
        )
    }
}

fun DatabaseNFCDevice.asNfcDeviceDomainModel(): NFCDevice {
    return NFCDevice(
        this.enabled
    )
}

fun DatabaseNFC.asNfcDomainModel(): NFCValue {
    return NFCValue(
        this.rfid,
        this.newRfid
    )
}

fun List<DatabaseTranslation>.asTranslationListDomainModel(): List<Translation> {
    return map {
        Translation(
            it.translationKey,
            it.translationValue
        )
    }
}

fun DatabaseTranslation.asTranslationDomainModel(): Translation {
    return Translation(
        this.translationKey,
        this.translationValue
    )
}

fun List<DatabaseMfgLine>.asDomainModel(): List<MfgLine> {
    return map {
        MfgLine(
            it.mfgLineId,
            it.mfgLine,
            it.mfgLineName,
            it.seq,
            it.checked,
            username = it.username
        )
    }
}

fun List<DatabaseMfgArea>.asDomainMfgAreaModel(): List<Areas> {
    return map {
        Areas(
            it.mfgFloorId,
            it.mfgArea,
            it.mfgName,
            it.mfgDescription,
            it.mfgAreaId,
            username = it.mfgUserName,
            it.isSelected
        )
    }
}

fun List<DatabaseSnackBarAction>.asSnackBarActionDomainModel(): List<SnackBarAction> {
    return map {
        SnackBarAction(
            it.id,
            it.action,
            it.show
        )
    }
}

fun List<DatabaseFirebaseNotification>.asFirebaseNotificationDomainModel(): List<FireBaseNotification> {
    return map {
        FireBaseNotification(
            it.id,
            it.token
        )
    }
}