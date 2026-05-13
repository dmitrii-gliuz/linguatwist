package org.dmitriigliuz.linguatwist.storage.excel

import io.github.oshai.kotlinlogging.KotlinLogging
import org.dhatim.fastexcel.reader.ReadableWorkbook
import org.dhatim.fastexcel.reader.Row
import org.dmitriigliuz.linguatwist.i18n.Messages
import java.io.File
import java.io.FileInputStream

private val logger = KotlinLogging.logger {}


fun Row.getCellTrimmedText(columnIndex: Int) = getCellText(columnIndex).trim()

fun Row.getCellTrimmedTextOrDefault(columnIndex: Int, default: String = "") = if (cellCount > columnIndex) {
    getCellTrimmedText(columnIndex).ifEmpty { default }
} else default


fun readExcelRowsWithHeader(file: File, onHeader: (Row) -> Unit = {}) = buildList {
    logger.debug { "Starting to read Excel file: ${file.absolutePath}" }
    FileInputStream(file).use { inputStream ->
        ReadableWorkbook(inputStream).use { workbook ->
            val sheet = workbook.firstSheet
            logger.debug { "Opened workbook and accessing first sheet: ${sheet.name}" }
            sheet.openStream().use { rowStream ->
                val iterator = rowStream.iterator()
                require(iterator.hasNext()) { Messages.excel_error_emptyFile() }
                val header = iterator.next()
                require(header.cellCount >= 2) { Messages.excel_error_headerTooShort() }
                onHeader(header)
                logger.debug { "Starting to iterate through data rows" }
                while (iterator.hasNext()) {
                    val row = iterator.next()
                    add(row)
                }
                logger.debug { "Finished reading all rows from Excel file" }
            }
        }
    }
}
