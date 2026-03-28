package org.dmitriigliuz.linguatwist.storage.excel

import io.github.oshai.kotlinlogging.KotlinLogging
import org.dhatim.fastexcel.reader.Row
import org.dmitriigliuz.linguatwist.model.LexicalEntry
import org.dmitriigliuz.linguatwist.model.Vocabulary
import org.dmitriigliuz.linguatwist.storage.VocabularyLoadResult
import org.dmitriigliuz.linguatwist.storage.VocabularyLoader
import org.dmitriigliuz.linguatwist.storage.DeduplicationStats
import org.dmitriigliuz.linguatwist.storage.filterDuplicates
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
        val deduplicationStats = DeduplicationStats()
        val uniqueEntries = readExcelRowsWithHeader(file)
            .parseWithStats(parsingStats)
            .filterDuplicates(deduplicationStats)
            .toSet()

        require(uniqueEntries.isNotEmpty()) { "No unique entries found in Excel file: $path" }

        val vocabulary = Vocabulary(uniqueEntries)

        logger.info {
            "Loaded ${uniqueEntries.size} unique entries, " +
                    "${deduplicationStats.duplicates} duplicates, " +
                    "${deduplicationStats.overlappedPairs} overlapping pairs, " +
                    "${parsingStats.invalidRows} invalid rows"
        }

        VocabularyLoadResult(
            vocabulary = vocabulary,
            duplicates = deduplicationStats.duplicates,
            overlappedPairs = deduplicationStats.overlappedPairs,
            invalidRows = parsingStats.invalidRows
        )
    }.onFailure { logger.error(it) { "Failed to load vocabulary from path: $path. Error: ${it.message}" } }


    private fun Sequence<Row>.parseWithStats(state: ParsingStats): Sequence<LexicalEntry> = sequence {
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
                logger.trace { "Invalid row (one empty cell) at row ${row.rowNum}, source: '$sourceText', translation: '$translation'" }
                state.invalidRows++
                continue
            }
            val score = if (row.cellCount > 2) extractScore(row) else 0
            yield(LexicalEntry(sourceText, translation, entryScore = score))
        }
    }

    private fun extractScore(row: Row): Int {
        val scoreText = row.getCellTrimmedText(2).ifBlank { return 0 }
//      FastExcel returns integer cells as doubles (e.g. "3.0"), so toIntOrNull() would fail — parse as Double first,
//      then truncate
        return (scoreText.toDoubleOrNull()?.let {
            if (it % 1.0 != 0.0) {
                logger.warn { "Fractional score truncated at row ${row.rowNum}: $scoreText" }
            }
            it
        } ?: run {
            logger.warn { "Cannot parse score '$scoreText' at row ${row.rowNum}, defaulting to 0" }
            0.0
        }).toInt()
    }
}