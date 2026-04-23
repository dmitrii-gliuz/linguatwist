package org.dmitriigliuz.linguatwist.storage.excel

import io.github.oshai.kotlinlogging.KotlinLogging
import org.dhatim.fastexcel.reader.Row
import org.dmitriigliuz.linguatwist.i18n.Messages
import org.dmitriigliuz.linguatwist.model.LexemeKey
import org.dmitriigliuz.linguatwist.model.LexicalEntry
import org.dmitriigliuz.linguatwist.model.Vocabulary
import org.dmitriigliuz.linguatwist.storage.VocabularyLoadResult
import org.dmitriigliuz.linguatwist.storage.VocabularyLoader
import org.dmitriigliuz.linguatwist.storage.requireValidExcel
import java.io.File

private val logger = KotlinLogging.logger { }


class ExcelVocabularyLoader: VocabularyLoader {
    override fun load(path: String): Result<VocabularyLoadResult> = runCatching {
        val file = File(path).requireValidExcel()
        val rows = readExcelRowsWithHeader(file)
        val (entriesMap, invalidRowsCount) = rows.parseAndFindInvalid()
        val duplicates = rows.size - invalidRowsCount - entriesMap.size

        require(entriesMap.isNotEmpty()) { "${Messages.vocabulary_error_noUniqueEntries}: $path" }

        val vocabulary = Vocabulary(entriesMap)

        logger.info {
            "Loaded ${entriesMap.size} unique entries, " +
                    "$duplicates duplicates, " +
                    "$invalidRowsCount invalid rows"
        }

        VocabularyLoadResult(
            vocabulary = vocabulary,
            path = path,
            duplicates = duplicates,
            invalidRows = invalidRowsCount
        )
    }.onFailure { logger.error(it) { "Failed to load vocabulary from path: $path. Error: ${it.message}" } }


    private fun Iterable<Row>.parseAndFindInvalid(): Pair<Map<LexemeKey, LexicalEntry>, Int> {
        var invalidRows = 0
        val entriesMap = mutableMapOf<LexemeKey, LexicalEntry>()
        for (row in this@parseAndFindInvalid) {
            if (row.cellCount == 0) continue
            if (row.cellCount < 2) {
                logger.warn { "Row must contain at least two columns at row ${row.rowNum}" }
                invalidRows++
                continue
            }
            val sourceText = row.getCellTrimmedText(0)
            val translation = row.getCellTrimmedText(1)
            if (sourceText.isEmpty() && translation.isEmpty()) continue
            if (sourceText.isEmpty() || translation.isEmpty()) {
                logger.warn { "Row contains empty cell at row ${row.rowNum}" }
                invalidRows++
                continue
            }
            val score = if (row.cellCount > 2) extractScore(row) else 0
            val entry = LexicalEntry(sourceText, translation, entryScore = score)
            entriesMap[entry.key] = entry
        }
        return entriesMap to invalidRows
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