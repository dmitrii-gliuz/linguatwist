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
        logger.trace { "Validating Excel file at path: $path" }
        val file = File(path).requireValidExcel()
        logger.trace { "Excel file validated successfully: ${file.absolutePath}" }
        val parsingStats = ParsingStats()
        val deduplicationStats = DeduplicationStats()

        logger.trace { "Reading rows from Excel file" }
        val rows = readRowsFromExcel(file).drop(1)
        logger.trace { "Starting to parse rows" }
        val parsedEntries = rows.parseWithStats(parsingStats)
        logger.trace { "Parsing completed, starting deduplication" }
        val deduplicated = parsedEntries.filterDuplicates(deduplicationStats)
        logger.trace { "Deduplication completed, converting to set" }
        val uniqueEntries = deduplicated.toSet()
        logger.trace { "Converted to set with ${uniqueEntries.size} unique entries" }

        require(uniqueEntries.isNotEmpty()) { "No unique entries found in Excel file: $path" }

        val vocabulary = Vocabulary(uniqueEntries)
        logger.trace { "Vocabulary object created with ${uniqueEntries.size} entries" }

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
            val rowCellCount = row.cellCount
            if (rowCellCount == 0) {
                logger.trace { "Skipping empty row at row ${row.rowNum}" }
                continue
            }
            if (rowCellCount < 2) {
                logger.warn { "Row must contain at least two columns at row ${row.rowNum}" }
                logger.trace { "Invalid row (less than 2 columns) at row ${row.rowNum}, cell count: $rowCellCount" }
                state.invalidRows++
                continue
            }
            val sourceText = row.getCellTrimmedText(0)
            val translation = row.getCellTrimmedText(1)
            if (sourceText.isEmpty() && translation.isEmpty()) {
                logger.trace { "Skipping row with both empty cells at row ${row.rowNum}" }
                continue
            }
            if (sourceText.isEmpty() || translation.isEmpty()) {
                logger.warn { "Row contains empty cell at row ${row.rowNum}" }
                logger.trace { "Invalid row (one empty cell) at row ${row.rowNum}, source: '$sourceText', translation: '$translation'" }
                state.invalidRows++
                continue
            }
            val score = if (rowCellCount > 2) extractScore(row) else 0
            logger.trace { "Parsed entry at row ${row.rowNum}: source='$sourceText', translation='$translation', score=$score" }
            yield(LexicalEntry(sourceText, translation, entryScore = score))
        }
    }

//    private fun parseRow(row: Row): Result<LexicalEntry> = runCatching {
//        require(row.cellCount >= 2) { "Row must contain at least two columns" }
//        val sourceText = extractCellText(row, 0)
//        val translation = extractCellText(row, 1)
//        val score = if (row.cellCount > 2) extractScore(row) else 0
//        LexicalEntry(sourceText, translation, entryScore = score)
//    }

//    private fun extractCellText(row: Row, columnIndex: Int): String  {
//        val value = row.getCellTrimmedText(columnIndex)
//        require(value.isNotEmpty()) { "Cell value is empty at row ${row.rowNum}, column $columnIndex" }
//        require(value.any { it.isLetter() }) {
//            "Cell value must contain at least one letter at row ${row.rowNum}, column $columnIndex"
//        }
//        return value
//    }

    private fun extractScore(row: Row): Int {
        val scoreText = row.getCellTrimmedText(2)
        val score = scoreText.toIntOrNull() ?: 0
        logger.trace { "Extracted score from row ${row.rowNum}: text='$scoreText', score=$score" }
        return score
    }
}