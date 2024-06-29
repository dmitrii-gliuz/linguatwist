package org.dmitriigliuz.learnnewwords

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File


class Spreadsheet(path: String) {
    private val file = File(path)
    private val workBook = XSSFWorkbook(file)
    private val sheet = workBook.getSheetAt(0)

    fun close() {
        workBook.close()
    }

    fun loadVocabulary(): Vocabulary {
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
        return Vocabulary(wordPairs)
    }
}