package org.dmitriigliuz.linguatwist.storage.excel

import io.github.oshai.kotlinlogging.KotlinLogging
import org.dhatim.fastexcel.Workbook
import org.dhatim.fastexcel.reader.Row
import org.dmitriigliuz.linguatwist.model.LexicalEntry
import org.dmitriigliuz.linguatwist.model.Vocabulary
import org.dmitriigliuz.linguatwist.storage.VocabularyWriter
import org.dmitriigliuz.linguatwist.storage.createTempFile
import org.dmitriigliuz.linguatwist.storage.requireValidExcel
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.io.use


private const val SCORE_COLUMN_INDEX = 2

private val logger = KotlinLogging.logger { }


class ExcelVocabularyWriter : VocabularyWriter {
    override fun save(path: String, vocabulary: Vocabulary): Result<Unit> {
        logger.debug { "Starting vocabulary save to path: $path" }
        val oldFile = File(path)
        val tempFile = oldFile.createTempFile()
        return runCatching {
            oldFile.requireValidExcel()
            logger.debug { "Excel file validated successfully: $path" }
            val vocabularyMap = vocabulary.entries.associateBy { it.normalizedSourceText to it.normalizedTranslation }
            logger.debug { "Created vocabulary map with ${vocabularyMap.size} entries" }
            FileOutputStream(tempFile).use { outputStream ->
                Workbook(outputStream, "LinguaTwist", "1.0").use { workbook ->
                    logger.debug { "Created workbook, starting to process rows" }
                    val worksheet = workbook.newWorksheet("Sheet1")
                    val rows = readRowsFromExcel(oldFile)
                    logger.debug { "Read rows from Excel file" }
                    rows.forEachIndexed { rowIndex, row ->
                        for (columnIndex in 0 until row.cellCount) {
                            var cellValue = row.getCellText(columnIndex)
                            if (rowIndex > 0 && columnIndex == SCORE_COLUMN_INDEX) {
                                cellValue = findScore(row, vocabularyMap)
                            }
                            worksheet.value(rowIndex, columnIndex, cellValue)
                        }
                            if (rowIndex > 0 && rowIndex % 100 == 0) {
                                logger.debug { "Processed $rowIndex rows" }
                            }
                    }
                    logger.debug { "Finished writing all rows to workbook" }
                }
            }
            logger.debug { "Moving temp file to replace original file" }
            Files.move(
                tempFile.toPath(),
                oldFile.toPath(),
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING
            )
            logger.info { "Successfully saved ${vocabulary.entries.size} entries to path: $path" }
        }.onFailure {
            logger.error(it) { "Failed to save vocabulary to path: $path. Error: ${it.message}" }
            tempFile.delete()
        }
    }

    private fun findScore(row: Row, vocabularyMap: Map<Pair<String, String>, LexicalEntry>): String {
        val pair = extractNormalizedPair(row)
        val oldScore = row.getCellText(SCORE_COLUMN_INDEX)
        val newScore = vocabularyMap[pair]?.entryScore?.toString()
        return newScore ?: oldScore
    }

    private fun extractNormalizedPair(row: Row): Pair<String, String> {
        val sourceText = row.getCellTrimmedText(0).lowercase()
        val translation = row.getCellTrimmedText(1).lowercase()
        return sourceText to translation
    }
}
