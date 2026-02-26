package co.ltlabs.ltmechanic.util.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.nfc.tech.MifareUltralight
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.IOException
import java.lang.StringBuilder
import kotlin.experimental.and

private const val TAG = "NFCUtil";

object NFCUtil {

    private val _rfid = MutableLiveData<String>()
    val rfid: LiveData<String>
        get() = _rfid

    fun clearRfid() {
        _rfid.value = null
    }

    fun createNFCMessage(payload: String, intent: Intent?): Boolean {

        val pathPrefix = "ltlabs.co:ltmechanic"
        val nfcRecord = NdefRecord(NdefRecord.TNF_EXTERNAL_TYPE, pathPrefix.toByteArray(), ByteArray(0), payload.toByteArray())
        val nfcMessage = NdefMessage(arrayOf(nfcRecord))
        intent?.let {
            val tag = it.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            return false
        }
        return false
    }

    fun retreiveNFCMessages(intent: Intent?): String {
        intent?.let {
            if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
                val nDefMessages = getNDefMessages(intent)
                nDefMessages[0].records?.let {
                    it.forEach {
                        it?.payload.let {
                            it?.let {
                                return String(it)
                            }
                        }
                    }
                }
            } else {
                return "Touch NFC tag to read data"
            }
        }

        return "Touch NFC tag to read data"
    }

    private fun getNDefMessages(intent: Intent): Array<NdefMessage> {
        val rawMessage = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        rawMessage?.let {
            return rawMessage.map {
                it as NdefMessage
            }.toTypedArray()
        }

        // Unknown tag type
        val empty = byteArrayOf()
        val record = NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty)
        val msg = NdefMessage(arrayOf(record))
        return arrayOf(msg)
    }

    fun disableNFCInForeground(nfcAdapter: NfcAdapter, activity: Activity) {
        nfcAdapter.disableForegroundDispatch(activity)
    }

    fun <T> enableNFCInForeground(nfcAdapter: NfcAdapter, activity: Activity, classType: Class<T>) {
        val pendingIntent = PendingIntent.getActivity(activity, 0,
            Intent(activity, classType).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0)
        val nfcIntentFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        val filters = arrayOf(nfcIntentFilter)

        val techLists = arrayOf(arrayOf(Ndef::class.java.name), arrayOf(NdefFormatable::class.java.name))

        nfcAdapter.enableForegroundDispatch(activity, pendingIntent, filters, techLists)
    }

    private fun writeMessageToTag(nfcMessage: NdefMessage, tag: Tag?): Boolean {

        try {
            val nDefTag = Ndef.get(tag)

            nDefTag?.let {
                it.connect()
                if (it.maxSize < nfcMessage.toByteArray().size) {
                    // Message too large to write to NFC tag
                    return false
                }
                if (it.isWritable) {
                    it.writeNdefMessage(nfcMessage)
                    it.close()
                    // Message is written to tag
                    return true
                } else {
                    // NFC tag is read only
                }
            }

            val nDefFormatableTag = NdefFormatable.get(tag)

            nDefFormatableTag?.let {
                return try {
                    it.connect()
                    it.format(nfcMessage)
                    it.close()
                    // The data is writted to the tag
                    true
                } catch (e: IOException) {
                    e.printStackTrace()
                    // Failed to format tag
                    false
                }
            }
            // NDEF is not supported
            return false
        } catch (e: Exception) {
            // Write operation failed
            e.printStackTrace()
        }

        return false
    }

    private fun dumpTagData(tag: Tag): String {
        val sb = StringBuilder()
        val id = tag.id
        sb.append("ID (hex):").append(toHex(id)).append("\n")
        sb.append("ID (reversed hex): ").append(toReversedHex(id)).append("\n")
        sb.append("ID (dec): ").append(NFCValueParser.toDec(id)).append("\n")
        sb.append("ID (reversed dec): ").append(toReversedDec(id)).append("\n")

        val prefix = "android.nfc.tech"
        sb.append("Technologies: ")

        for (tech in tag.techList) {
            sb.append(tech.substring(prefix.length))
            sb.append(", ")
        }

        sb.delete(sb.length - 2, sb.length)

        for (tech in tag.techList) {
            if (tech == MifareClassic::class.java.name) {
                sb.append('\n')
                var type = "Unknown"

                try {
                    val mifareTag = MifareClassic.get(tag)

                    type = when (mifareTag.type) {
                        MifareClassic.TYPE_CLASSIC -> {
                            "Classic"
                        }
                        MifareClassic.TYPE_PLUS -> {
                            "Plus"
                        }
                        MifareClassic.TYPE_PRO -> {
                            "Pro"
                        }
                        else -> {
                            "Unknown"
                        }
                    }

                    sb.append("Mifare Classic type: ")
                    sb.append(type)
                    sb.append("\n")

                    sb.append("Mifare size: ")
                    sb.append("${mifareTag.size} bytes")
                    sb.append("\n")

                    sb.append("Mifare sectors: ");
                    sb.append(mifareTag.sectorCount);
                    sb.append('\n');

                    sb.append("Mifare blocks: ");
                    sb.append(mifareTag.blockCount);
                } catch (e: Exception) {
                    sb.append("Mifare classic error: ${e.message}")
                }
            }

            if (tech == MifareUltralight::class.java.name) {
                sb.append('\n')
                val mifareUlTag = MifareUltralight.get(tag)
                var type = "Unknown"

                type = when (mifareUlTag.type) {
                    MifareUltralight.TYPE_ULTRALIGHT -> {
                        "Ultralight"
                    }
                    MifareUltralight.TYPE_ULTRALIGHT_C -> {
                        "Ultralight C"
                    }
                    else -> {
                        "Unknown"
                    }
                }

                sb.append("Mifare Ultralight type: ")
                sb.append(type)
            }

        }

        return sb.toString()
    }

    fun resolveIntent(intent: Intent) {
        val action = intent.action

        if (NfcAdapter.ACTION_TAG_DISCOVERED == action
            || NfcAdapter.ACTION_TECH_DISCOVERED == action
            || NfcAdapter.ACTION_NDEF_DISCOVERED == action) {

            val rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            var msgs = arrayOfNulls<NdefMessage>(0)

            if (rawMsgs != null) {

                msgs = arrayOfNulls(rawMsgs.size)

                msgs.forEachIndexed{ index, msg ->
                    msgs[index] = rawMsgs[index] as NdefMessage
                }

            } else {
                val empty = ByteArray(0)
                val id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)
                val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
                val payload = dumpTagData(tag).toByteArray()
                val record = NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload)
                val msg = NdefMessage(arrayOf(record))
                msgs = arrayOf(msg)
            }

            displayMsgs(msgs)
            getNFCDecID(msgs)

        }
    }

    private fun displayMsgs(msgs: Array<NdefMessage?>) {
        msgs.let {
            if (msgs.isNotEmpty()) {

                msgs[0]?.let {m ->
                    val builder = StringBuilder()
                    val records = NdefMessageParser.parse(m)
                    val size = records.size

                    for (i in 0 until size) {
                        val record = records[i]
                        val str = record.str()
                        builder.append(str).append("\n")
                    }

                    Log.d(TAG, "displayMsgs: nfc: ${builder}")
                }


            }
        }
    }

    fun getNFCDecID(msgs: Array<NdefMessage?>): String {
        var id = ""
        msgs.let {
            if (msgs.isNotEmpty()) {
                
                

                msgs[0]?.let {m ->
                    val builder = StringBuilder()
                    val records = NdefMessageParser.parse(m)
                    val size = records.size

                    for (i in 0 until size) {
                        val record = records[i]
                        val str = record.str()
                        builder.append(str).append("\n")
                    }

                    builder.toString().lines().forEach {

                       if (it.contains("(dec)")) {
                           val rfid = it.split(":")[1].trim()
                           _rfid.value = if (rfid.length == 9) "0$rfid" else rfid
                       }
                    }
                }
            }
        }
        return id
    }

    private fun toHex(bytes: ByteArray): String? {
        val sb = StringBuilder()
        for (i in bytes.indices.reversed()) {
            val b: Int = (bytes[i] and 0xff.toByte()).toInt()
            if (b < 0x10) sb.append('0')
            sb.append(Integer.toHexString(b))
            if (i > 0) {
                sb.append(" ")
            }
        }
        return sb.toString()
    }

    private fun toReversedHex(bytes: ByteArray): String? {
        val sb = StringBuilder()
        for (i in bytes.indices) {
            if (i > 0) {
                sb.append(" ")
            }
            val b: Int = (bytes[i] and 0xff.toByte()).toInt()
            if (b < 0x10) sb.append('0')
            sb.append(Integer.toHexString(b))
        }
        return sb.toString()
    }

    private fun toDec(bytes: ByteArray): Long {
        var result: Long = 0
        var factor: Long = 1
        for (i in bytes.indices) {
            val value: Long = (bytes[i] and 0xff.toByte()).toLong()
            result += value * factor
            factor *= 256
        }
        return result
    }

    private fun toReversedDec(bytes: ByteArray): Long {
        var result: Long = 0
        var factor: Long = 1
        for (i in bytes.indices.reversed()) {
            val value: Long = (bytes[i] and 0xffL.toByte()).toLong()
            result += value * factor
            factor *= 256L
        }
        return result
    }

}