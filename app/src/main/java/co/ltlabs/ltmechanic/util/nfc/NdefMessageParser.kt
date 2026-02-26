package co.ltlabs.ltmechanic.util.nfc

import android.nfc.NdefMessage
import android.nfc.NdefRecord

object NdefMessageParser {

    fun parse(message: NdefMessage): List<ParsedNdefRecord> =
        getRecords(message.records)

    fun getRecords(records: Array<NdefRecord>): List<ParsedNdefRecord> {
        val elements = arrayListOf<ParsedNdefRecord>()

        for (record in records) {
            when {
                UriRecord.isUri(record) -> {
                    elements.add(UriRecord.parse(record))
                }
                TextRecord.isText(record) -> {
                    elements.add(TextRecord.parse(record))
                }
                SmartPoster.isPoster(record) -> {
                    elements.add(SmartPoster.parse(record));
                }
                else -> {
                    elements.add(object : ParsedNdefRecord {
                        override fun str(): String {
                            return String(record.payload)
                        }
                    })
                }
            }
        }

        return elements
    }
}