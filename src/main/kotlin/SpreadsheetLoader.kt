package org.dmitriigliuz.learnnewwords

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File

fun getCellByIndex(row: Row, cellId: Int): String {
    val result = row.getCell(cellId).toString().trim()
    if (result.isEmpty()) throw IllegalArgumentException("The cell is empty") else return result
}

fun readExcelFile(path: String) = buildList {
    val file = File(path)
    val workBook = XSSFWorkbook(file)
    val sheet = workBook.getSheetAt(0)
    for (row in sheet) {
        if (row.rowNum == 0) continue
        try {
            val (originalWord, translation) = (0..1).map { getCellByIndex(row, it) }
            add(WordPair(originalWord, translation))
        } catch (e: IllegalArgumentException) {
            continue
        }
    }
    workBook.close()
}