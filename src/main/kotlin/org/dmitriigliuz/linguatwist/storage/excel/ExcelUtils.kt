package org.dmitriigliuz.linguatwist.storage.excel

import io.github.oshai.kotlinlogging.KotlinLogging
import org.dhatim.fastexcel.reader.ReadableWorkbook
import org.dhatim.fastexcel.reader.Row
import java.io.File
import java.io.FileInputStream
import kotlin.use

private val logger = KotlinLogging.logger {}


fun Row.getCellTrimmedText(columnIndex: Int) = getCellText(columnIndex).trim()

fun Row.getCellTrimmedTextOrDefault(columnIndex: Int, default: String) = if (cellCount > columnIndex) {
    getCellTrimmedText(columnIndex).ifEmpty { default }
} else {
    default
}


fun readExcelRowsWithHeader(
    file: File,
    onHeader: (Row) -> Unit = {},
): Sequence<Row> = sequence {
    logger.info { "Starting to read Excel file: ${file.absolutePath}" }
    FileInputStream(file).use { inputStream ->
        ReadableWorkbook(inputStream).use { workbook ->
            val sheet = workbook.firstSheet
            logger.info { "Opened workbook and accessing first sheet: ${sheet.name}" }
            sheet.openStream().use { rowStream ->
                val iterator = rowStream.iterator()
                require(iterator.hasNext()) { "Excel file is empty" }
                val header = iterator.next()
                require(header.cellCount >= 2) { "Excel file header must contain at least two columns" }
                onHeader(header)
                logger.info { "Starting to iterate through data rows" }
                while (iterator.hasNext()) {
                    val row = iterator.next()
                    yield(row)
                }
                logger.info { "Finished reading all rows from Excel file" }
            }
        }
    }
}
