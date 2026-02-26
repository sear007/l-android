package co.ltlabs.ltmechanic.util.nfc

import android.nfc.NdefRecord
import com.google.common.base.Preconditions
import java.io.UnsupportedEncodingException
import java.lang.IllegalArgumentException
import java.nio.charset.Charset
import java.util.*
import kotlin.experimental.and

class TextRecord(var languageCode: String, var text: String) : ParsedNdefRecord {

    init {
        languageCode = Preconditions.checkNotNull(languageCode)
        text = Preconditions.checkNotNull(text)
    }

    override fun str(): String =
        text

    companion object {
        fun parse(record: NdefRecord): TextRecord {
            Preconditions.checkArgument(record.tnf == NdefRecord.TNF_WELL_KNOWN)
            Preconditions.checkArgument(Arrays.equals(record.type, NdefRecord.RTD_TEXT))
            try {
                val payload = record.payload
                /*
                 * payload[0] contains the "Status Byte Encodings" field, per the
                 * NFC Forum "Text Record Type Definition" section 3.2.1.
                 *
                 * bit7 is the Text Encoding Field.
                 *
                 * if (Bit_7 == 0): The text is encoded in UTF-8 if (Bit_7 == 1):
                 * The text is encoded in UTF16
                 *
                 * Bit_6 is reserved for future use and must be set to zero.
                 *
                 * Bits 5 to 0 are the length of the IANA language code.
                 */
                val textEncoding = if ((payload[0] and 128.toByte()).toInt() == 0) "UTF-8" else "UTF-16"
                val languageCodeLength = payload[0] and 63
                val languageCode = String(payload, 1, languageCodeLength.toInt(), Charset.forName("US-ASCII"))
                val text = String(
                    payload,
                    languageCodeLength + 1,
                    payload.size - languageCodeLength - 1,
                    Charset.forName(textEncoding)
                )
                return TextRecord(languageCode, text)
            } catch (e: UnsupportedEncodingException) {
                throw IllegalArgumentException(e)
            }
        }


        fun isText(record: NdefRecord) =
            try {
                parse(record)
                true
            } catch (e: IllegalArgumentException) {
                false
            }
    }
}