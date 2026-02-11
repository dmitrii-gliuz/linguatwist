package org.dmitriigliuz.linguatwist.storage.excel

import org.dhatim.fastexcel.reader.ReadableWorkbook
import org.dhatim.fastexcel.reader.Row
import org.dhatim.fastexcel.reader.Sheet
import org.dmitriigliuz.linguatwist.storage.AbstractDataLoader
import org.dmitriigliuz.linguatwist.storage.FileUtils
import org.dmitriigliuz.linguatwist.storage.excel.model.ExcelRowData
import org.dmitriigliuz.linguatwist.storage.excel.model.ExcelSheetData
import org.dmitriigliuz.linguatwist.storage.model.RawDataCell
import org.dmitriigliuz.linguatwist.storage.model.RawDataResult
import org.dmitriigliuz.linguatwist.storage.model.RawDataRow

class ExcelDataLoader : org.dmitriigliuz.linguatwist.storage.AbstractDataLoader() {
    override val supportedExttensions: Set<String> = setOf("xlsx", "xls")

    companion object {
        const val MAX_COLUMNS_TO_READ = 1000
        const val EMPTY_CELLS_THRESHOLD = 5
    }

    private fun readExcelCell(excelRow: Row, columnIndex: Int): org.dmitriigliuz.linguatwist.storage.model.RawDataCell? = try {
        val stringValue = excelRow.getCellAsString(columnIndex).orElse(null)
        val numericValue = excelRow.getCellAsNumber(columnIndex).orElse(null)?.toDouble()
        val isEmpty = stringValue.isNullOrBlank() && numericValue == null
        _root_ide_package_.org.dmitriigliuz.linguatwist.storage.model.RawDataCell(
            row = excelRow.rowNum,
            column = columnIndex,
            isEmpty = isEmpty,
            stringValue = stringValue,
            numericValue = numericValue
        )
    } catch (_: Exception) {
        null
    }

    private fun readExcelRowData(excelRow: Row, currentMaxColumns: Int): org.dmitriigliuz.linguatwist.storage.excel.model.ExcelRowData {
        val cells = mutableListOf<org.dmitriigliuz.linguatwist.storage.model.RawDataCell>()
        var maxColumns = currentMaxColumns
        for (columnIndex in 0 until MAX_COLUMNS_TO_READ) {
            val cellData = readExcelCell(excelRow, columnIndex) ?: break
            cells.add(cellData)
            if (!cellData.isEmpty) {
                maxColumns = maxOf(maxColumns, columnIndex + 1)
            } else if (columnIndex > maxColumns + EMPTY_CELLS_THRESHOLD) {
                break
            }
        }
        val row = _root_ide_package_.org.dmitriigliuz.linguatwist.storage.model.RawDataRow(excelRow.rowNum, cells)
        return _root_ide_package_.org.dmitriigliuz.linguatwist.storage.excel.model.ExcelRowData(row, maxColumns)
    }

    private fun readAllRowsFromSheet(sheet: Sheet): org.dmitriigliuz.linguatwist.storage.excel.model.ExcelSheetData {
        val rows = mutableListOf<org.dmitriigliuz.linguatwist.storage.model.RawDataRow>()
        var maxColumns = 0
        sheet.openStream().use { stream ->
            stream.forEach { row ->
                val rowData = readExcelRowData(row, maxColumns)
                rows.add(rowData.row)
                maxColumns = maxOf(maxColumns, rowData.maxColumns)
            }
        }
        return _root_ide_package_.org.dmitriigliuz.linguatwist.storage.excel.model.ExcelSheetData(rows, maxColumns)
    }

    override fun loadRawData(filePath: String): org.dmitriigliuz.linguatwist.storage.model.RawDataResult {
        val file = _root_ide_package_.org.dmitriigliuz.linguatwist.storage.FileUtils.requireFileReadable(filePath)
        val rows = mutableListOf<org.dmitriigliuz.linguatwist.storage.model.RawDataRow>()
        var maxColumns = 0

        file.inputStream().use { inputStream ->
            ReadableWorkbook(inputStream).use { workbook ->
                val sheet = workbook.firstSheet
                val sheetData = readAllRowsFromSheet(sheet)
                rows.addAll(sheetData.rows)
                maxColumns = sheetData.maxColumns

            }
        }
        return _root_ide_package_.org.dmitriigliuz.linguatwist.storage.model.RawDataResult(
            rows,
            rows.size,
            maxColumns,
            "Excel"
        )

    }

}