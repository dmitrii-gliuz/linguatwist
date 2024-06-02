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

fun main() {
    val file = File("vocabulary.xlsx")
    val vocabulary = readExcelFile(file)
    val game = Game(vocabulary)
    game.getFiveWords().forEach {
        println("O: ${it.originalWord} T: ${it.translation}")
    }

}