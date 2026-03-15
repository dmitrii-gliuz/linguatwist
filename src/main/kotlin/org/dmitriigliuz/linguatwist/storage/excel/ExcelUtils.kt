package org.dmitriigliuz.linguatwist.storage.excel

import io.github.oshai.kotlinlogging.KotlinLogging
import org.dhatim.fastexcel.reader.ReadableWorkbook
import org.dhatim.fastexcel.reader.Row
import java.io.File
import java.io.FileInputStream
import kotlin.use

private val logger = KotlinLogging.logger {}

private const val EMPTY_CELL_TEXT = ""

fun Row.getCellTrimmedText(columnIndex: Int) =
    getCellAsString(columnIndex).orElse(EMPTY_CELL_TEXT).trim()

fun readRowsFromExcel(file: File) = sequence {
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
                yield(header)
                logger.info { "Starting to iterate through data rows" }
                while (iterator.hasNext()) {
                    logger.debug { "Entering loop iteration: hasNext=${iterator.hasNext()}" }
                    val row = iterator.next()
                    yield(row)
                    logger.debug { "Exiting loop iteration for row ${row.rowNum}" }
                }
                logger.info { "Finished reading all rows from Excel file" }
            }
        }
    }
}
