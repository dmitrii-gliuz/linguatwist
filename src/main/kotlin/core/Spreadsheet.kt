package org.dmitriigliuz.linguatwist.core

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

lateinit var spreadsheetPath: String

class SpreadsheetException(message: String?, cause: Throwable? = null) : Exception(message, cause)

class Spreadsheet(path: String) {
    private val file = File(path)
    private val workBook: XSSFWorkbook = try {
        FileInputStream(file).use { XSSFWorkbook(it) }
    } catch (e: Exception) {
        throw SpreadsheetException(e.message, e)
    }
    var duplicates = 0
    var pairsWithSameWords = 0
    var incorrectRows = 0
    private var isClosed = false
    private val sheet = workBook.getSheetAt(0)

    private val wordPairs = buildList {
        for (row in sheet) {
            if (row.rowNum == 0) continue
            val newPair = try {
                val (originalWord, translation) = getWordsFromTwoFirstCells(row)
                if (alreadyContains(originalWord, translation)) {
                    duplicates++
                    continue
                } else {
                    val score = getScore(row)
                    WordPair(originalWord, translation, score).also {
                        if (alreadyContainsOneOfWords(it)) pairsWithSameWords += 2
                    }
                }
            } catch (e: IllegalArgumentException) {
                incorrectRows++
                continue
            }
            add(newPair)
        }
    }

    val numberOfRoundsToCoverAllWords: Int
        get() {
            if (wordPairs.size < numberOfPairsInRound) {
                throw SpreadsheetException(localeString("NotEnoughWordsToPlayTheGame"))
            }
            val numberOfRoundsWithUniquePairs = wordPairs.size / numberOfPairsInRound + if (wordPairs.size % numberOfPairsInRound > 0) 1 else 0
            var result = numberOfRoundsWithUniquePairs
            if (pairsWithSameWords > numberOfRoundsWithUniquePairs) {
                result += pairsWithSameWords - numberOfRoundsWithUniquePairs
            }
            return result
        }

    val defaultNumberOfRounds =
        if (wordPairs.size < recommendedVocabularySize) {
            numberOfRoundsToCoverAllWords
        } else org.dmitriigliuz.linguatwist.core.defaultNumberOfRounds

    fun close() { if (!isClosed) workBook.close().also { isClosed = true } }

    private fun validateCellAndGetString(cell: Cell?): String {
        if (cell == null) throw IllegalArgumentException(localeString("CellIEmpty"))
        val value = cell.toString().trim()
        if (value.isEmpty()) throw IllegalArgumentException(localeString("CellIEmpty"))
        if (value.none { char -> char.isLetter() }) throw IllegalArgumentException(localeString("CellContainsNoLetters"))
        return value
    }

    private fun getWordsFromTwoFirstCells(row: Row) = (0..1).map { validateCellAndGetString(row.getCell(it)) }

    private fun getScore(row: Row) = try {
            getScoreCell(row).numericCellValue.toInt()
        } catch (e: IllegalStateException) {
            0
        }

    private fun getScoreCell(row: Row): Cell = row.getCell(2) ?: row.createCell(2)

    private fun validateVocabularySize(wordPairs: List<WordPair>) {
        if (wordPairs.size < numberOfPairsInRound) {
            throw SpreadsheetException(localeString("NotEnoughWordsToPlayTheGame"))
        }
        val numberOfUniquePairs = wordPairs.size - pairsWithSameWords
        val minimalNumberOfUniquePairsInRound = numberOfPairsInRound - 1
        if (numberOfUniquePairs < minimalNumberOfUniquePairsInRound) {
            throw SpreadsheetException(
                "${localeString("RequiredNumberOfUniquePairs")}: $minimalNumberOfUniquePairsInRound." +
                        " ${localeString("Found")}: $numberOfUniquePairs"
            )
        }
    }

    fun loadVocabulary(): Vocabulary {
        validateVocabularySize(wordPairs)
        return Vocabulary(wordPairs)
    }

    private fun findPairRow(pair: WordPair): Int {
        for (row in sheet) {
            if (row.rowNum == 0) continue
            try {
                val (originalWord, translation) = getWordsFromTwoFirstCells(row)
                if (originalWord == pair.originalWord && translation == pair.translation) {
                    return row.rowNum
                }
            } catch (e: IllegalArgumentException) {
                continue
            }
        }
        return -1
    }

    fun writeScores(vocabulary: Vocabulary) {
        vocabulary.wordPairs.forEach { pair ->
            val row = findPairRow(pair)
            if (row != -1) {
                val scoreCell = sheet.getRow(row).getCell(2)
                scoreCell.setCellValue(pair.score.toDouble())
            }
        }
        try {
            FileOutputStream(file).use { workBook.write(it) }
        } catch (e: Exception) {
            throw SpreadsheetException(e.message, e)
        }
    }
}