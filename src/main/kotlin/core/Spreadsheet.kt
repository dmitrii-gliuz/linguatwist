package org.dmitriigliuz.linguatwist.core

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class SpreadsheetException(message: String, cause: Throwable? = null) : Exception(message, cause)

class Spreadsheet(path: String) {
    private val file = File(path)
    private val workBook: XSSFWorkbook
    var duplicates = 0
    var pairsWithSameWords = 0

    init {
        try {
            FileInputStream(file).use {
                workBook = XSSFWorkbook(it)
            }
        } catch (e: Exception) {
            throw SpreadsheetException(e.message ?: "unexpected error", e)
        }
    }
    private val sheet = workBook.getSheetAt(0)

    private val wordPairs = buildList {
        for (row in sheet) {
            if (row.rowNum == 0) continue
            try {
                val (originalWord, translation) = getWordsFromTwoFirstCells(row)
                val score = getScore(row)
                if (this.checkIfPairWasUsed(originalWord, translation)) {
                    duplicates++
                } else {
                    val newPair = WordPair(originalWord, translation, score)
                    if (this.checkIfOneOfWordsAlreadyUsed(newPair)) {
                        pairsWithSameWords++
                    }
                    add(newPair)
                }
            } catch (e: IllegalArgumentException) {
                continue
            }
        }
        pairsWithSameWords *= 2
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

    fun close() {
        workBook.close()
    }

    private fun getWordsFromTwoFirstCells(row: Row) = (0..1).map {
        val errorMessage = "The cell is empty"
        val cell = row.getCell(it) ?: throw IllegalArgumentException(errorMessage)
        val value = cell.toString().trim()
        if (value.isEmpty()) throw IllegalArgumentException(errorMessage)
        value
    }

    private fun getScore(row: Row)= try {
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
            FileOutputStream(file).use {
                workBook.write(it)
            }
        } catch (e: Exception) {
            throw SpreadsheetException(e.message ?: "unexpected error", e)
        }
    }
}