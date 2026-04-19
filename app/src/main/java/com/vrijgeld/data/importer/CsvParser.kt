package com.vrijgeld.data.importer

import com.vrijgeld.data.model.ImportSource
import java.text.SimpleDateFormat
import java.util.Locale

interface BankCsvParser {
    fun parse(lines: List<String>): List<ParsedTransaction>
}

// Strategy dispatcher: inspects the header row and delegates to the correct bank parser.
class CsvParser {

    fun parse(lines: List<String>): List<ParsedTransaction> {
        val header = lines.firstOrNull { it.contains(";") } ?: return emptyList()
        return when {
            // ING Netherlands: has both "Naam / Omschrijving" and "Af Bij"
            header.contains("Naam / Omschrijving") && header.contains("Af Bij") ->
                IngCsvParser().parse(lines)

            // ABN AMRO: has "Muntsoort" and "Transactiedatum"
            header.contains("Muntsoort") && header.contains("Transactiedatum") ->
                AbnAmroCsvParser().parse(lines)

            // Rabobank: has "IBAN/BBAN" and "Naam tegenpartij"
            header.contains("IBAN/BBAN") && header.contains("Naam tegenpartij") ->
                RabobankCsvParser().parse(lines)

            // Fall back to ING parser as best-effort
            else -> IngCsvParser().parse(lines)
        }
    }
}

// ABN AMRO CSV format:
// "Rekeningnummer";"Muntsoort";"Transactiedatum";"Beginstand";"Eindstand";"Rentedatum";"Bedrag";"Omschrijving"
// Date: YYYYMMDD  Amount: comma decimal, already signed (negative = debit)
class AbnAmroCsvParser : BankCsvParser {

    private val dateFmt = SimpleDateFormat("yyyyMMdd", Locale.US)

    override fun parse(lines: List<String>): List<ParsedTransaction> {
        val headerIdx = lines.indexOfFirst { it.contains("Transactiedatum") }
        if (headerIdx < 0) return emptyList()

        val headers    = parseSemicolonRow(lines[headerIdx])
        val colRek     = headers.indexOf("Rekeningnummer")
        val colDate    = headers.indexOf("Transactiedatum")
        val colBedrag  = headers.indexOf("Bedrag")
        val colOmschr  = headers.indexOf("Omschrijving")

        if (colDate < 0 || colBedrag < 0) return emptyList()

        val results = mutableListOf<ParsedTransaction>()
        for (i in (headerIdx + 1) until lines.size) {
            val line = lines[i].trim()
            if (line.isEmpty()) continue
            val cols = parseSemicolonRow(line)

            val rek    = cols.getOrElse(colRek)    { "" }
            val date   = cols.getOrElse(colDate)   { "" }
            val bedrag = cols.getOrElse(colBedrag) { "" }
            val omschr = cols.getOrElse(colOmschr) { "" }

            val dateMs = runCatching { dateFmt.parse(date)?.time ?: 0L }.getOrDefault(0L)
            val amountDouble = parseEuropeanAmount(bedrag) ?: continue
            val cents = (amountDouble * 100).toLong()

            val description = omschr.ifEmpty { "ABN AMRO transaction" }
            results += ParsedTransaction(
                amountCents      = cents,
                dateMillis       = dateMs,
                description      = description,
                merchantName     = null,
                counterpartyIban = null,
                importHash       = sha256("$date|$bedrag|$description"),
                importSource     = ImportSource.CSV,
                ownIban          = rek.ifEmpty { null }
            )
        }
        return results
    }
}

// Rabobank CSV format (comma-separated *or* semicolon, UTF-8):
// "IBAN/BBAN";"Munt";"BIC";"Volgnr";"Datum";"Rentedatum";"Bedrag";"Saldo na trn";
// "Tegenrekening IBAN/BBAN";"Naam tegenpartij";...;"Omschrijving-1";"Omschrijving-2";"Omschrijving-3";...
// Date: YYYY-MM-DD  Amount: comma decimal, already signed
class RabobankCsvParser : BankCsvParser {

    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override fun parse(lines: List<String>): List<ParsedTransaction> {
        val headerIdx = lines.indexOfFirst { it.contains("IBAN/BBAN") && it.contains("Naam tegenpartij") }
        if (headerIdx < 0) return emptyList()

        val headers    = parseSemicolonRow(lines[headerIdx])
        val colIban    = headers.indexOf("IBAN/BBAN")
        val colDatum   = headers.indexOf("Datum")
        val colBedrag  = headers.indexOf("Bedrag")
        val colTegen   = headers.indexOf("Tegenrekening IBAN/BBAN")
        val colNaam    = headers.indexOf("Naam tegenpartij")
        val colOmschr1 = headers.indexOf("Omschrijving-1")
        val colOmschr2 = headers.indexOf("Omschrijving-2")
        val colOmschr3 = headers.indexOf("Omschrijving-3")

        if (colDatum < 0 || colBedrag < 0) return emptyList()

        val results = mutableListOf<ParsedTransaction>()
        for (i in (headerIdx + 1) until lines.size) {
            val line = lines[i].trim()
            if (line.isEmpty()) continue
            val cols = parseSemicolonRow(line)

            val iban   = cols.getOrElse(colIban)    { "" }
            val datum  = cols.getOrElse(colDatum)   { "" }
            val bedrag = cols.getOrElse(colBedrag)  { "" }
            val tegen  = cols.getOrElse(colTegen)   { "" }
            val naam   = cols.getOrElse(colNaam)    { "" }
            val o1     = if (colOmschr1 >= 0) cols.getOrElse(colOmschr1) { "" } else ""
            val o2     = if (colOmschr2 >= 0) cols.getOrElse(colOmschr2) { "" } else ""
            val o3     = if (colOmschr3 >= 0) cols.getOrElse(colOmschr3) { "" } else ""

            val dateMs = runCatching { dateFmt.parse(datum)?.time ?: 0L }.getOrDefault(0L)
            val amountDouble = parseEuropeanAmount(bedrag) ?: continue
            val cents = (amountDouble * 100).toLong()

            val description = listOf(o1, o2, o3).filter { it.isNotBlank() }.joinToString(" ").ifEmpty { naam }
            results += ParsedTransaction(
                amountCents      = cents,
                dateMillis       = dateMs,
                description      = description,
                merchantName     = naam.ifEmpty { null },
                counterpartyIban = tegen.ifEmpty { null },
                importHash       = sha256("$datum|$bedrag|$description"),
                importSource     = ImportSource.CSV,
                ownIban          = iban.ifEmpty { null }
            )
        }
        return results
    }
}
