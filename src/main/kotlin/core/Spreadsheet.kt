package org.dmitriigliuz.linguatwist.core

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


class Spreadsheet(path: String) {
    private val file = File(path)
//    init {
//        validateFile()
//    }
    private val workBook: XSSFWorkbook

    init {
        FileInputStream(file).use {
            workBook = XSSFWorkbook(it)
        }
    }
    private val sheet = workBook.getSheetAt(0)
    private fun validateFile() {
        if (!file.exists()) throw IllegalArgumentException("The file doesn't exist")
        if (!file.isFile) throw IllegalArgumentException("The path is not a file")
        if (!file.canRead()) throw IllegalArgumentException("The file is not readable")
        if (!file.canWrite()) throw IllegalArgumentException("The file is not writable")
    }
    fun close() {
        workBook.close()
    }

    private fun getWordsFromTwoFirstCells(row: Row) = (0..1).map {
        if (row.getCell(it) == null) throw IllegalArgumentException("The cell is empty")
        row.getCell(it).toString().trim()
    }

    private fun getScore(row: Row): Int {
        return try {
            getScoreCell(row).numericCellValue.toInt()
        } catch (e: IllegalStateException) {
            0
        }
    }

    private fun getScoreCell(row: Row): Cell = row.getCell(2) ?: row.createCell(2)

    fun loadVocabulary(): Vocabulary {
        val wordPairs = mutableListOf<WordPair>()
        for (row in sheet) {
            if (row.rowNum == 0) continue
            try {
                val (originalWord, translation) = getWordsFromTwoFirstCells(row)
                val score = getScore(row)
                wordPairs.add(WordPair(originalWord, translation, score))
            } catch (e: IllegalArgumentException) {
                continue
            }
        }
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
//        try {
        FileOutputStream(file).use {
            workBook.write(it)
        }
//        } catch (e: Exception) {
//            println("⚠️ Error: ${e.message}")
//        }
    }
}