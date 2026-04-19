package com.vrijgeld.data.importer

import com.vrijgeld.data.model.ImportSource
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Locale

// ING Netherlands personal CSV format:
// "Datum";"Naam / Omschrijving";"Rekening";"Tegenrekening";"Code";"Af Bij";"Bedrag (EUR)";"Mutatiesoort";"Mededelingen"
class IngCsvParser : BankCsvParser {

    private val dateFmt = SimpleDateFormat("yyyyMMdd", Locale.US)

    override fun parse(lines: List<String>): List<ParsedTransaction> {
        val headerIdx = lines.indexOfFirst { it.contains("Datum") && it.contains("Bedrag") }
        if (headerIdx < 0) return emptyList()

        val headers   = parseSemicolonRow(lines[headerIdx])
        val colDatum  = headers.indexOf("Datum")
        val colName   = headers.indexOf("Naam / Omschrijving")
        val colRek    = headers.indexOf("Rekening")
        val colTegen  = headers.indexOf("Tegenrekening")
        val colAfBij  = headers.indexOf("Af Bij")
        val colBedrag = headers.indexOf("Bedrag (EUR)")
        val colMutatie= headers.indexOf("Mutatiesoort")
        val colMeded  = headers.indexOf("Mededelingen")

        if (colDatum < 0 || colBedrag < 0 || colAfBij < 0) return emptyList()

        val results = mutableListOf<ParsedTransaction>()
        for (i in (headerIdx + 1) until lines.size) {
            val line = lines[i].trim()
            if (line.isEmpty()) continue
            val cols = parseSemicolonRow(line)

            val datum   = cols.getOrElse(colDatum)   { "" }
            val name    = cols.getOrElse(colName)     { "" }
            val rek     = cols.getOrElse(colRek)      { "" }
            val tegen   = cols.getOrElse(colTegen)    { "" }
            val afBij   = cols.getOrElse(colAfBij)    { "" }.trim()
            val bedrag  = cols.getOrElse(colBedrag)   { "" }
            val mutatie = cols.getOrElse(colMutatie)  { "" }
            val meded   = cols.getOrElse(colMeded)    { "" }

            val dateMs = runCatching { dateFmt.parse(datum)?.time ?: 0L }.getOrDefault(0L)
            val amountDouble = parseEuropeanAmount(bedrag) ?: continue
            val cents = (amountDouble * 100).toLong().let { if (afBij == "Af") -it else it }

            val description = buildString {
                if (mutatie.isNotBlank()) append(mutatie)
                if (meded.isNotBlank()) { if (isNotEmpty()) append(": "); append(meded) }
            }.ifEmpty { name }

            results += ParsedTransaction(
                amountCents      = cents,
                dateMillis       = dateMs,
                description      = description,
                merchantName     = name.ifEmpty { null },
                counterpartyIban = tegen.ifEmpty { null },
                importHash       = sha256("$datum|$bedrag|$description"),
                importSource     = ImportSource.CSV,
                ownIban          = rek.ifEmpty { null }
            )
        }
        return results
    }
}

// Handles European number formats:
//   "1.234,56"  → 1234.56  (dot = thousands, comma = decimal)
//   "1234,56"   → 1234.56  (comma = decimal only)
//   "1234.56"   → 1234.56  (already standard)
internal fun parseEuropeanAmount(s: String): Double? {
    val c = s.trim()
    return when {
        c.contains(",") && c.contains(".") ->
            c.replace(".", "").replace(",", ".").toDoubleOrNull()
        c.contains(",") ->
            c.replace(",", ".").toDoubleOrNull()
        else -> c.toDoubleOrNull()
    }
}

// Parses a single semicolon-separated row, respecting double-quoted fields.
internal fun parseSemicolonRow(line: String): List<String> {
    val result  = mutableListOf<String>()
    val current = StringBuilder()
    var inQuotes = false
    // Strip UTF-8 BOM if present on first character
    val chars = if (line.isNotEmpty() && line[0] == '\uFEFF') line.substring(1) else line
    for (ch in chars) {
        when {
            ch == '"'             -> inQuotes = !inQuotes
            ch == ';' && !inQuotes -> { result += current.toString().trim(); current.clear() }
            else                  -> current.append(ch)
        }
    }
    result += current.toString().trim()
    return result
}

internal fun sha256(input: String): String =
    MessageDigest.getInstance("SHA-256")
        .digest(input.toByteArray())
        .joinToString("") { "%02x".format(it) }
