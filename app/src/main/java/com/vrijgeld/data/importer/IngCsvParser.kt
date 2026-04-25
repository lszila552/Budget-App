package com.vrijgeld.data.importer

import com.vrijgeld.data.model.ImportSource
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Locale

// ING Netherlands CSV — supports Dutch and English headers, any of tab/comma/semicolon delimiters,
// YYYYMMDD / DD-MM-YYYY / YYYY-MM-DD date formats, and comma or dot as decimal separator.
class IngCsvParser : BankCsvParser {

    private val dateFmts = listOf(
        SimpleDateFormat("yyyyMMdd",   Locale.US),
        SimpleDateFormat("dd-MM-yyyy", Locale.US),
        SimpleDateFormat("yyyy-MM-dd", Locale.US)
    )

    // Maps every known header spelling (lowercase, trimmed) → logical field key.
    private val headerAliases = mapOf(
        "date"                to "date",
        "datum"               to "date",
        "name / description"  to "name",
        "naam / omschrijving" to "name",
        "account"             to "account",
        "rekening"            to "account",
        "counterparty"        to "counterparty",
        "tegenrekening"       to "counterparty",
        "code"                to "code",
        "debit/credit"        to "direction",
        "af bij"              to "direction",
        "amount (eur)"        to "amount",
        "bedrag (eur)"        to "amount",
        "transaction type"    to "type",
        "mutatiesoort"        to "type",
        "notifications"       to "memo",
        "mededelingen"        to "memo"
    )

    override fun parse(lines: List<String>): List<ParsedTransaction> {
        val headerIdx = lines.indexOfFirst { line ->
            val l = stripBom(line).lowercase()
            (l.contains("date") || l.contains("datum")) &&
            (l.contains("amount") || l.contains("bedrag"))
        }
        if (headerIdx < 0) return emptyList()

        val headerLine = stripBom(lines[headerIdx])
        val delimiter  = detectDelimiter(headerLine)
        val rawHeaders = parseDelimitedRow(headerLine, delimiter)

        // Build field-name → column-index map from the actual header row.
        val colFor = mutableMapOf<String, Int>()
        rawHeaders.forEachIndexed { idx, h ->
            val field = headerAliases[h.trim().lowercase()]
            if (field != null) colFor[field] = idx
        }

        // Date, amount and direction are the minimum required columns.
        val colDate         = colFor["date"]        ?: return emptyList()
        val colAmount       = colFor["amount"]      ?: return emptyList()
        val colDirection    = colFor["direction"]   ?: return emptyList()
        val colName         = colFor["name"]
        val colAccount      = colFor["account"]
        val colCounterparty = colFor["counterparty"]
        val colCode         = colFor["code"]
        val colType         = colFor["type"]
        val colMemo         = colFor["memo"]

        val results = mutableListOf<ParsedTransaction>()
        for (i in (headerIdx + 1) until lines.size) {
            val line = lines[i].trim()
            if (line.isEmpty()) continue
            val cols = parseDelimitedRow(line, delimiter)

            val dateStr      = cols.getOrElse(colDate)         { "" }
            val amountStr    = cols.getOrElse(colAmount)       { "" }
            val direction    = cols.getOrElse(colDirection)    { "" }.trim()
            val name         = colName?.let         { cols.getOrElse(it) { "" } } ?: ""
            val account      = colAccount?.let      { cols.getOrElse(it) { "" } } ?: ""
            val counterparty = colCounterparty?.let { cols.getOrElse(it) { "" } } ?: ""
            val code         = colCode?.let         { cols.getOrElse(it) { "" } } ?: ""
            val type         = colType?.let         { cols.getOrElse(it) { "" } } ?: ""
            val memo         = colMemo?.let         { cols.getOrElse(it) { "" } } ?: ""

            val dateMs       = parseDate(dateStr)
            val amountDouble = parseEuropeanAmount(amountStr) ?: continue
            val isDebit = direction.equals("Af",    ignoreCase = true) ||
                          direction.equals("Debit", ignoreCase = true) ||
                          direction.equals("D",     ignoreCase = true)
            val cents = (amountDouble * 100).toLong().let { if (isDebit) -it else it }

            val description = buildString {
                if (type.isNotBlank()) append(type)
                if (memo.isNotBlank()) { if (isNotEmpty()) append(": "); append(memo) }
            }.ifEmpty { code.ifEmpty { name } }

            results += ParsedTransaction(
                amountCents      = cents,
                dateMillis       = dateMs,
                description      = description,
                merchantName     = name.ifEmpty { null },
                counterpartyIban = counterparty.ifEmpty { null },
                importHash       = sha256("$dateStr|$amountStr|$direction|$description"),
                importSource     = ImportSource.CSV,
                ownIban          = account.ifEmpty { null }
            )
        }
        return results
    }

    private fun parseDate(s: String): Long {
        val t = s.trim()
        for (fmt in dateFmts) {
            val ms = runCatching { fmt.parse(t)?.time }.getOrNull()
            if (ms != null && ms > 0L) return ms
        }
        return 0L
    }
}

// ── Shared CSV utilities ──────────────────────────────────────────────────────

internal fun stripBom(line: String): String =
    if (line.isNotEmpty() && line[0] == '\uFEFF') line.substring(1) else line

// Count delimiters outside quoted strings and return the most frequent one.
internal fun detectDelimiter(line: String): Char {
    var inQ = false
    var tabs = 0; var commas = 0; var semis = 0
    for (ch in stripBom(line)) {
        when {
            ch == '"'          -> inQ = !inQ
            !inQ && ch == '\t' -> tabs++
            !inQ && ch == ','  -> commas++
            !inQ && ch == ';'  -> semis++
        }
    }
    return when {
        tabs > commas && tabs > semis -> '\t'
        commas > semis                -> ','
        else                          -> ';'
    }
}

// RFC 4180-style single-row parser for any delimiter, with BOM stripping and quote handling.
internal fun parseDelimitedRow(line: String, delimiter: Char): List<String> {
    val result   = mutableListOf<String>()
    val current  = StringBuilder()
    var inQuotes = false
    for (ch in stripBom(line)) {
        when {
            ch == '"'                    -> inQuotes = !inQuotes
            ch == delimiter && !inQuotes -> { result += current.toString().trim(); current.clear() }
            else                         -> current.append(ch)
        }
    }
    result += current.toString().trim()
    return result
}

// Kept for AbnAmroCsvParser + RabobankCsvParser.
internal fun parseSemicolonRow(line: String): List<String> = parseDelimitedRow(line, ';')

// ─────────────────────────────────────────────────────────────────────────────

// Handles European number formats:
//   "1.234,56"  → 1234.56  (dot = thousands separator, comma = decimal)
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

internal fun sha256(input: String): String =
    MessageDigest.getInstance("SHA-256")
        .digest(input.toByteArray())
        .joinToString("") { "%02x".format(it) }
