package org.dmitriigliuz.learnnewwords

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File


data class Flashcard(val originalWord: String, val translation: String)

fun getCellByIndex(row: Row, cellId: Int): String {
    val result = row.getCell(cellId).toString()
    if (result.isEmpty()) throw IllegalArgumentException("The cell is empty") else return result
}

fun readExcelFile(file: File) = buildList {
    val workBook = XSSFWorkbook(file)
    val sheet = workBook.getSheetAt(0)
    for (row in sheet) {
        if (row.rowNum == 0) continue
        try {
            val (originalWord, translation) = (0..1).map { getCellByIndex(row, it) }
            add(Flashcard(originalWord, translation))
        } catch (e: IllegalArgumentException) {
            continue
        }
    }
    workBook.close()
}

fun printPairs(pairs: List<Flashcard>) {
    val maxOriginalWordLength = pairs.maxOfOrNull { it.originalWord.length }!! + 3
    val originalWords = pairs.map { it.originalWord }
    val translations = pairs.map { it.translation }.shuffled()
    for (n in 1..5) {
        val originalWord = originalWords[n - 1]
        val translation = translations[n - 1]
        val numberOfSpaces = maxOriginalWordLength - originalWord.length
        val spaces = " ".repeat(numberOfSpaces)
        println("$n. $originalWord$spaces$n. $translation")
    }
}

fun main() {
    val file = File("vocabulary.xlsx")
    val vocabulary = readExcelFile(file)
    val game = Game(vocabulary)
    val fiveWords = game.getFiveWords()
    printPairs(fiveWords)

}