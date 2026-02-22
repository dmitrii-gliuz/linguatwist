package org.dmitriigliuz.linguatwist.storage.excel

import org.dhatim.fastexcel.reader.ReadableWorkbook
import org.dhatim.fastexcel.reader.Row
import java.io.File
import java.io.FileInputStream
import kotlin.use

fun Row.getCellText(columnIndex: Int) = getCellAsString(columnIndex).orElse("")

fun Row.getCellTrimmedText(columnIndex: Int) = getCellText(columnIndex).trim()

fun readRowsFromExcel(file: File) = sequence {
    FileInputStream(file).use { inputStream ->
        ReadableWorkbook(inputStream).use { workbook ->
            val sheet = workbook.firstSheet
            sheet.openStream().use { rowStream ->
                val iterator = rowStream.iterator()
                require(iterator.hasNext()) { "Excel file is empty" }
                val header = iterator.next()
                require(header.cellCount >= 2) { "Excel file header must contain at least two columns" }
                yield(header)
                while (iterator.hasNext()) {
                    val row = iterator.next()
                    yield(row)
                }
            }
        }
    }
}
