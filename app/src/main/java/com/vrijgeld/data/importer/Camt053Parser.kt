package com.vrijgeld.data.importer

import android.util.Xml
import com.vrijgeld.data.model.ImportSource
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Locale

data class ParsedTransaction(
    val amountCents: Long,
    val dateMillis: Long,
    val description: String,
    val merchantName: String?,
    val counterpartyIban: String?,
    val importHash: String,
    val importSource: ImportSource = ImportSource.CAMT053
)

class Camt053Parser {

    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun parse(input: InputStream): List<ParsedTransaction> {
        val parser = Xml.newPullParser().apply {
            setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            setInput(input, null)
        }

        val results = mutableListOf<ParsedTransaction>()
        val tagStack = ArrayDeque<String>()
        var inEntry = false
        var inTxDtls = false

        var amountStr = ""
        var isDebit = true
        var bookingDate = ""
        var remittance = ""
        var addtlInfo = ""
        var cpIban = ""
        var cpName = ""

        fun reset() {
            inEntry = false; inTxDtls = false
            amountStr = ""; isDebit = true; bookingDate = ""
            remittance = ""; addtlInfo = ""; cpIban = ""; cpName = ""
        }

        var ev = parser.eventType
        while (ev != XmlPullParser.END_DOCUMENT) {
            when (ev) {
                XmlPullParser.START_TAG -> {
                    val tag = parser.name
                    tagStack.addLast(tag)
                    when {
                        tag == "Ntry" -> inEntry = true
                        tag == "TxDtls" -> inTxDtls = true
                        tag == "Amt" && inEntry && !inTxDtls -> {
                            amountStr = parser.nextText()
                            tagStack.removeLastOrNull()
                        }
                    }
                }
                XmlPullParser.TEXT -> if (inEntry) {
                    val text = parser.text?.trim() ?: ""
                    if (text.isNotEmpty()) {
                        val cur  = tagStack.lastOrNull() ?: ""
                        val prev = if (tagStack.size >= 2) tagStack[tagStack.size - 2] else ""
                        when (cur) {
                            "CdtDbtInd"    -> isDebit = text == "DBIT"
                            "Dt"           -> if (prev == "BookgDt") bookingDate = text
                            "Ustrd"        -> if (prev == "RmtInf") remittance = text
                            "AddtlNtryInf" -> addtlInfo = text
                            "Nm"           -> if (cpName.isEmpty()) cpName = text
                            "IBAN"         -> if (cpIban.isEmpty()) cpIban = text
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    tagStack.removeLastOrNull()
                    when (parser.name) {
                        "TxDtls" -> inTxDtls = false
                        "Ntry"   -> {
                            if (amountStr.isNotEmpty() && bookingDate.isNotEmpty()) {
                                val cents = ((amountStr.replace(",", ".").toDoubleOrNull() ?: 0.0) * 100)
                                    .toLong().let { if (isDebit) -it else it }
                                val date  = runCatching { dateFmt.parse(bookingDate)?.time ?: 0L }.getOrDefault(0L)
                                val desc  = remittance.ifEmpty { addtlInfo }.ifEmpty { cpName }
                                results += ParsedTransaction(
                                    amountCents      = cents,
                                    dateMillis       = date,
                                    description      = desc,
                                    merchantName     = cpName.ifEmpty { null },
                                    counterpartyIban = cpIban.ifEmpty { null },
                                    importHash       = sha256("$bookingDate|$amountStr|$desc")
                                )
                            }
                            reset()
                        }
                    }
                }
            }
            ev = parser.next()
        }
        return results
    }

    private fun sha256(input: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
