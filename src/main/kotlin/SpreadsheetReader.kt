package org.dmitriigliuz.learnnewwords

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File


class SpreadsheetReader(private val path: String) {
    fun load(): List<WordPair> {
        val file = File(path)
        val workBook = XSSFWorkbook(file)
        val sheet = workBook.getSheetAt(0)
        val wordPairs = mutableListOf<WordPair>()
        for (row in sheet) {
            if (row.rowNum == 0) continue
            try {
                val (originalWord, translation) = (0..1).map {
                    if (row.getCell(it) == null) throw IllegalArgumentException("The cell is empty")
                    row.getCell(it).toString().trim()
                }
                wordPairs.add(WordPair(originalWord, translation))
            } catch (e: IllegalArgumentException) {
                continue
            }
        }
        workBook.close()
        return wordPairs
    }
}