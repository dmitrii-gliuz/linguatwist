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
        logger.debug { "Starting vocabulary load from path: $path" }
        val file = File(path).requireValidExcel()
        val parsingStats = ParsingStats()
        val deduplicationStats = DeduplicationStats()


        val uniqueEntries = readRowsFromExcel(file)
            .drop(1)
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
            parseRow(row)
                .onSuccess { yield(it) }
                .onFailure {
                    logger.warn(it) { "Invalid row: ${it.message}"}
                    state.invalidRows++
            }
        }
    }

    private fun parseRow(row: Row): Result<LexicalEntry> = runCatching {
        require(row.cellCount >= 2) { "Row must contain at least two columns" }
        val sourceText = extractCellText(row, 0)
        val translation = extractCellText(row, 1)
        val score = if (row.cellCount > 2) extractScore(row) else 0
        LexicalEntry(sourceText, translation, entryScore = score)
    }

    private fun extractCellText(row: Row, columnIndex: Int): String  {
        val value = row.getCellTrimmedText(columnIndex)
        require(value.isNotEmpty()) { "Cell value is empty at row ${row.rowNum}, column $columnIndex" }
        require(value.any { it.isLetter() }) {
            "Cell value must contain at least one letter at row ${row.rowNum}, column $columnIndex"
        }
        return value
    }

    private fun extractScore(row: Row) = row.getCellTrimmedText(2).toIntOrNull() ?: 0
}