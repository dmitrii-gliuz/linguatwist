package org.dmitriigliuz.linguatwist.storage.excel

import org.dhatim.fastexcel.reader.Row
import org.dmitriigliuz.linguatwist.storage.VocabularyLoadResult
import org.dmitriigliuz.linguatwist.storage.VocabularyLoader
import java.io.File

class ExcelVocabularyLoader: VocabularyLoader {
    private fun loadFile(path: String): File {
        val file = File(path)
//        TODO Use the string from locale whe it is implemented
        require(file.exists()) { "File does not exist: $path" }
        return file
    }

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
    override fun load(path: String): Result<VocabularyLoadResult> {
        val file = try {
            loadFile(path)
        } catch (e: IllegalArgumentException) {
            return Result.failure(e)
        }
    }
}