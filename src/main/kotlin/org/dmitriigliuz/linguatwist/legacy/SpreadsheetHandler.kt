package org.dmitriigliuz.linguatwist.legacy

import org.dhatim.fastexcel.Workbook
import org.dhatim.fastexcel.reader.ReadableWorkbook
import org.dhatim.fastexcel.reader.Row
import org.dmitriigliuz.linguatwist.legacy.application.context.AppContext
import org.dmitriigliuz.linguatwist.model.Vocabulary
import org.dmitriigliuz.linguatwist.model.LexicalEntry
import org.dmitriigliuz.linguatwist.storage.StorageException
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class Spreadsheet(private val context: AppContext) {
    private val logger = LoggerFactory.getLogger(Spreadsheet::class.java)
    private val path = context.config.spreadsheetPath
    private val file = File(path)
    var duplicates = 0
    var pairsWithSameWords = 0
    var incorrectRows = 0

    private fun getTextCellValue(row: Row, columnIndex: Int): String {
        val cell = row.getCellAsString(columnIndex).orElse("")
        logger.debug("Reading cell at row ${row.rowNum}, column $columnIndex: '$cell'")
        val value = cell.trim()
        if (value.isEmpty() || value.none { it.isLetter() }) {
            logger.warn("Invalid cell value at row ${row.rowNum}, column $columnIndex: '$value'")
            throw IllegalArgumentException("Invalid cell value")
        }
        return value
    }

    fun getIntCellValue(row: Row, columnIndex: Int, defaultValue: Int = 0): Int = row
        .getCellAsNumber(columnIndex)
        .orElse(defaultValue.toBigDecimal())
        .toInt()

    private fun readWordPairs(): List<LexicalEntry> = buildList {
        logger.debug("Reading word pairs from spreadsheet at: $path")
        if (!file.exists()) {
            logger.error("Spreadsheet file not found at: $path")
            throw StorageException(context.getString("FileNotFound").format(path))
        }

        FileInputStream(file).use { inputStream ->
            ReadableWorkbook(inputStream).use { workbook ->
                val sheet = workbook.firstSheet
                sheet.openStream().use { rowStream ->
                    rowStream.skip(1) // Skip header
                        .forEach { row ->
                            try {
                                val originalWord = getTextCellValue(row, 0)
                                logger.debug("Original word: '$originalWord'")
                                val translation = getTextCellValue(row, 1)
                                logger.debug("Translation: '$translation'")
                                // TODO Use another method to extract score
                                val score = getTextCellValue(row, 2).toIntOrNull() ?: 0
                                val entry = LexicalEntry(originalWord, translation, score)

                                if (!this.any { it.sameAs(entry) }) {
                                    if (this.any {it.overlaps(entry) }) pairsWithSameWords += 2
                                    add(entry)
                                } else {
                                    duplicates++
                                }
                            } catch (e: Exception) {
                                logger.warn("Invalid row at index ${row.rowNum}: ${e.message}")
                                incorrectRows++
                            }
                        }
                }
            }
            logger.debug("Word pairs size : ${size}, duplicates: $duplicates, pairs with same words: $pairsWithSameWords, incorrect rows: $incorrectRows")
        }
    }

//    TODO Nove it to  vocabulary class
    fun writeScores(vocabulary: Vocabulary) {
        val tempFile = File(file.parent, "${file.nameWithoutExtension}_temp.xlsx")

        FileOutputStream(tempFile).use { outputStream ->
            Workbook(outputStream, "LinguaTwist", "1.0").use { workbook ->
                val worksheet = workbook.newWorksheet("Sheet1")

                // Write header
                worksheet.value(0, 0, "Original")
                worksheet.value(0, 1, "Translation")
                worksheet.value(0, 2, "Score")

                // Write data
                vocabulary.entries.forEachIndexed { index, pair ->
                    val row = index + 1
                    worksheet.value(row, 0, pair.originalWord)
                    worksheet.value(row, 1, pair.translation)
                    worksheet.value(row, 2, pair.score)
                }
            }
        }

        // Replace original file
        if (!file.delete() || !tempFile.renameTo(file)) {
            throw StorageException("Failed to update spreadsheet file")
        }
    }

//    TODO Use vocabulary factory to create vocabulary from word pairs
    private val wordPairs by lazy { readWordPairs() }

//    TODO Use vocabulary factory to create vocabulary from word pairs
    fun loadVocabulary(): Vocabulary = Vocabulary(wordPairs)

//    TODO Move it to game initializer
    val numberOfRoundsToCoverAllWords: Int
        get() {
            if (wordPairs.size < context.config.pairsPerRound) {
                throw StorageException(context.getString("NotEnoughWordsToPlayTheGame"))
            }
            val leftoverPairs = wordPairs.size % context.config.pairsPerRound
            val fullRoundsNeeded = wordPairs.size / context.config.pairsPerRound + if (leftoverPairs > 0) 1 else 0
            var result = fullRoundsNeeded
            if (pairsWithSameWords > fullRoundsNeeded) {
                result += pairsWithSameWords - fullRoundsNeeded
            }
            return result
        }

//    TODO Move it to game initializer
    val defaultNumberOfRounds =
        if (wordPairs.size < context.config.recommendedVocabularySize) {
            numberOfRoundsToCoverAllWords
        } else context.config.defaultRoundsNumber
}