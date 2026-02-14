package org.dmitriigliuz.linguatwist.storage.excel

import org.dhatim.fastexcel.Workbook
import org.dhatim.fastexcel.Worksheet
import org.dmitriigliuz.linguatwist.legacy.storage.AbstractDataWriter
import org.dmitriigliuz.linguatwist.legacy.storage.FileUtils
import org.dmitriigliuz.linguatwist.legacy.storage.model.RawDataRow
import org.dmitriigliuz.linguatwist.legacy.storage.model.WriteOptions
import java.io.File
import java.io.FileOutputStream

class ExcelDataWriter() : AbstractDataWriter() {
    companion object {
        fun writeValue() {
//            TODO Figure out if it is needed
        }
    }

    override val supportedExtensions = setOf("xlsx", "xls")

    fun getOriginalAndTargetFiles(filePath: String, options: WriteOptions): Pair<File?, File> {
        val originalFile = FileUtils.getReadableFileOrNull(filePath) ?.takeIf { options.overwriteFile }
        val targetFile = originalFile?.let { FileUtils.getTempFile(it) } ?: File(filePath)
        return Pair(originalFile, targetFile)
    }

    fun replaceOriginalFileIfExists(originalFile: File?, targetFile: File) =
        originalFile?.let { FileUtils.safeReplaceFile(it, targetFile) }

    private fun writeValueToWorksheet(worksheet: Worksheet, row: Int, column: Int, value: Any?) {
        when (value) {
            is String -> worksheet.value(row, column, value)
            is Number -> worksheet.value(row, column, value)
            is Boolean -> worksheet.value(row, column, value)
            else -> worksheet.value(row, column, value?.toString())
        }
    }

    private fun <T> withWorkbook(targetFile: File, options: WriteOptions, block: (Worksheet) -> T) =
        FileOutputStream(targetFile).use { outputStream ->
            Workbook(outputStream, options.appName, options.appVersion).use { workbook ->
                val worksheet = workbook.newWorksheet(options.sheetName)
                block(worksheet)
            }
        }

    private fun writeHeadersAndGetStartingRow(
        worksheet: Worksheet,
        headers: List<String>?,
        options: WriteOptions
    ): Int {
        if (options.includeHeaders && headers != null) {
            headers.forEachIndexed { columnIndex, header ->
                worksheet.value(0, columnIndex, header)
            }
            return 1
        }
        return 0
    }

    private fun performWriteOperation(
        filePath: String,
        headers: List<String>?,
        options: WriteOptions,
        writeOperation: (Worksheet, Int) -> Unit
    ) {
        val (originalFile, targetFile) = getOriginalAndTargetFiles(filePath, options)

        withWorkbook(targetFile, options) { worksheet ->
            val startRow = writeHeadersAndGetStartingRow(worksheet, headers, options)
            writeOperation(worksheet, startRow)
        }
        replaceOriginalFileIfExists(originalFile, targetFile)
    }

    override fun writeData(
        filePath: String,
        headers: List<String>?,
        rows: List<List<Any?>>,
        options: WriteOptions
    ) = performWriteOperation(filePath, headers, options) { worksheet, startRow ->
        rows.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { columnIndex, value ->
                writeValueToWorksheet(worksheet, startRow + rowIndex, columnIndex, value)
            }
        }
    }


    override fun writeRawData(
        filePath: String,
        rawRows: List<RawDataRow>,
        headers: List<String>?,
        options: WriteOptions
    ) = performWriteOperation(filePath, headers, options) { worksheet, startRow ->
            rawRows.forEachIndexed { rowIndex, rawRow ->
                rawRow.cells.forEachIndexed { columnIndex, cell ->
                    val value = cell.stringValue ?: cell.numericValue
                    writeValueToWorksheet(worksheet, startRow + rowIndex, columnIndex, value)
                }
            }
    }
}