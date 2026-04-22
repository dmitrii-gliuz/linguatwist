package org.dmitriigliuz.linguatwist.storage.excel

import io.github.oshai.kotlinlogging.KotlinLogging
import org.dhatim.fastexcel.reader.Row
import org.dmitriigliuz.linguatwist.i18n.Messages
import org.dmitriigliuz.linguatwist.model.LexicalEntry
import org.dmitriigliuz.linguatwist.model.Vocabulary
import org.dmitriigliuz.linguatwist.storage.VocabularyLoadResult
import org.dmitriigliuz.linguatwist.storage.VocabularyLoader
import org.dmitriigliuz.linguatwist.storage.requireValidExcel
import java.io.File

private val logger = KotlinLogging.logger { }

private data class ParsingStats(
    var invalidRows: Int = 0
)

class ExcelVocabularyLoader: VocabularyLoader {
    override fun load(path: String): Result<VocabularyLoadResult> = runCatching {
        val file = File(path).requireValidExcel()
        val parsingStats = ParsingStats()
        val entries = readExcelRowsWithHeader(file).parseWithStats(parsingStats)
        val unique = entries.toSet()
        val duplicates = entries.size - unique.size

        require(unique.isNotEmpty()) { "${Messages.vocabulary_error_noUniqueEntries}: $path" }

        val vocabulary = Vocabulary(unique)

        logger.info {
            "Loaded ${entries.size} unique entries, " +
                    "$duplicates duplicates, " +
                    "${parsingStats.invalidRows} invalid rows"
        }

        VocabularyLoadResult(
            vocabulary = vocabulary,
            path = path,
            duplicates = duplicates,
            invalidRows = parsingStats.invalidRows
        )
    }.onFailure { logger.error(it) { "Failed to load vocabulary from path: $path. Error: ${it.message}" } }


    private fun Iterable<Row>.parseWithStats(state: ParsingStats) = buildList {
        for (row in this@parseWithStats) {
            if (row.cellCount == 0) continue
            if (row.cellCount < 2) {
                logger.warn { "Row must contain at least two columns at row ${row.rowNum}" }
                state.invalidRows++
                continue
            }
            val sourceText = row.getCellTrimmedText(0)
            val translation = row.getCellTrimmedText(1)
            if (sourceText.isEmpty() && translation.isEmpty()) continue
            if (sourceText.isEmpty() || translation.isEmpty()) {
                logger.warn { "Row contains empty cell at row ${row.rowNum}" }
                state.invalidRows++
                continue
            }
            val score = if (row.cellCount > 2) extractScore(row) else 0
            add(LexicalEntry(sourceText, translation, entryScore = score))
        }
    }

    private fun extractScore(row: Row): Int {
        val scoreText = row.getCellTrimmedText(2).ifBlank { return 0 }
//      FastExcel returns integer cells as doubles (e.g. "3.0"), so toIntOrNull() would fail — parse as Double first,
//      then truncate
        return (scoreText.toDoubleOrNull()?.let {
            if (it % 1.0 != 0.0) logger.warn { "Fractional score truncated at row ${row.rowNum}: $scoreText" }
            it
        } ?: run {
            logger.warn { "Cannot parse score '$scoreText' at row ${row.rowNum}, defaulting to 0" }
            0.0
        }).toInt()
    }
}