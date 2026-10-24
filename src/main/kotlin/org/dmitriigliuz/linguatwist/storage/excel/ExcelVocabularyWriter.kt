package org.dmitriigliuz.linguatwist.storage.excel

import io.github.oshai.kotlinlogging.KotlinLogging
import org.dhatim.fastexcel.Workbook
import org.dhatim.fastexcel.reader.Row
import org.dmitriigliuz.linguatwist.model.LexemeKey
import org.dmitriigliuz.linguatwist.model.LexicalEntry
import org.dmitriigliuz.linguatwist.model.Vocabulary
import org.dmitriigliuz.linguatwist.storage.VocabularyWriter
import org.dmitriigliuz.linguatwist.storage.createTempFile
import org.dmitriigliuz.linguatwist.storage.requireValidExcel
import java.io.File
import java.io.FileOutputStream
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.io.use

private const val ORIGINAL_COLUMN_INDEX = 0
private const val TRANSLATION_COLUMN_INDEX = 1
private const val SCORE_COLUMN_INDEX = 2

private val logger = KotlinLogging.logger { }


class ExcelVocabularyWriter : VocabularyWriter {
    override fun save(path: String, vocabulary: Vocabulary): Result<Unit> {
        logger.debug { "Starting vocabulary save to path: $path" }
        val oldFile = File(path)
        var tempFile: File? = null
        return runCatching {
            tempFile = oldFile.createTempFile()
            oldFile.requireValidExcel()
            logger.debug { "Excel file validated successfully: $path" }
            val vocabularyMap = vocabulary.entriesMap
            logger.debug { "Created vocabulary map with ${vocabularyMap.size} entries" }
            FileOutputStream(tempFile).use { outputStream ->
                Workbook(outputStream, "LinguaTwist", "1.0").use { workbook ->
                    logger.debug { "Created workbook, starting to process rows" }
                    val worksheet = workbook.newWorksheet("Sheet1")
                    val rows = readExcelRowsWithHeader(oldFile) { headerRow ->
                        val headerOriginal = headerRow.getCellTrimmedTextOrDefault(
                            ORIGINAL_COLUMN_INDEX,
                            "Original"
                        )
                        val headerTranslation = headerRow.getCellTrimmedTextOrDefault(
                            TRANSLATION_COLUMN_INDEX,
                            "Translation"
                        )
                        val headerScore = headerRow.getCellTrimmedTextOrDefault(
                            SCORE_COLUMN_INDEX,
                            "Score"
                        )
                        worksheet.value(0, ORIGINAL_COLUMN_INDEX, headerOriginal)
                        worksheet.value(0, TRANSLATION_COLUMN_INDEX, headerTranslation)
                        worksheet.value(0, SCORE_COLUMN_INDEX, headerScore)
                        val lastColumnIndex = maxOf(headerRow.cellCount - 1, SCORE_COLUMN_INDEX)
                        if (headerRow.cellCount > SCORE_COLUMN_INDEX) {
                            for (columnIndex in (SCORE_COLUMN_INDEX + 1)..lastColumnIndex) {
                                worksheet.value(
                                    0,
                                    columnIndex,
                                    headerRow.getCellTrimmedTextOrDefault(columnIndex, "")
                                )
                            }
                        }
                    }
                    logger.debug { "Read rows from Excel file" }
                    rows.forEachIndexed { rowIndex, row ->
                        if (rowIndex > 0 && rowIndex % 50 == 0) {
                            logger.debug { "Processing row $rowIndex with ${row.cellCount} cells" }
                        }
                        val lastColumnIndex = maxOf(row.cellCount - 1, SCORE_COLUMN_INDEX)
                        for (columnIndex in 0..lastColumnIndex) {
                            val cellValueToWrite =  if (columnIndex == SCORE_COLUMN_INDEX) {
                                resolveScore(row, vocabularyMap)
                            } else row.getCellTrimmedTextOrDefault(columnIndex, "")

                            val targetRowIndex = rowIndex + 1 // Because we already processed the header row
                            worksheet.value(targetRowIndex, columnIndex, cellValueToWrite)
                        }
                            if (rowIndex > 0 && rowIndex % 100 == 0) {
                                logger.debug { "Processed $rowIndex rows" }
                            }
                    }
                    logger.debug { "Finished writing all rows to workbook" }
                }
            }
            logger.debug { "Moving temp file to replace original file" }
            try {
                Files.move(
                    tempFile.toPath(),
                    oldFile.toPath(),
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
                )
            } catch (e: AtomicMoveNotSupportedException) {
                logger.warn(e) { "Atomic move is not supported, falling back to regular move" }
                Files.move(
                    tempFile.toPath(),
                    oldFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
            }
            logger.info { "Successfully saved ${vocabulary.entriesMap.size} entries to path: $path" }
        }.onFailure {
            logger.error(it) { "Failed to save vocabulary to path: $path. Error: ${it.message}" }
            tempFile?.delete()
        }
    }

    private fun resolveScore(row: Row, vocabularyMap: Map<LexemeKey, LexicalEntry>): String {
        val pair = extractLexemeKey(row)
        val oldScore = row.getCellTrimmedTextOrDefault(SCORE_COLUMN_INDEX, "0")
        val newScore = vocabularyMap[pair]?.entryScore?.toString()
        return newScore ?: oldScore
    }

    private fun extractLexemeKey(row: Row): LexemeKey {
        val sourceText = row.getCellTrimmedTextOrDefault(ORIGINAL_COLUMN_INDEX)
        val translation = row.getCellTrimmedTextOrDefault(TRANSLATION_COLUMN_INDEX)
        return LexemeKey.of(sourceText, translation)
    }
}
