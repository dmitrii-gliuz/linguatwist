package org.dmitriigliuz.linguatwist.storage.excel

import org.dhatim.fastexcel.Workbook
import org.dhatim.fastexcel.Worksheet
import org.dhatim.fastexcel.reader.ReadableWorkbook
import org.dhatim.fastexcel.reader.Row
import org.dhatim.fastexcel.reader.Sheet
import org.dmitriigliuz.linguatwist.model.LexicalEntry
import org.dmitriigliuz.linguatwist.model.Vocabulary
import org.dmitriigliuz.linguatwist.storage.VocabularyWriter
import org.dmitriigliuz.linguatwist.storage.createTempFile
import org.dmitriigliuz.linguatwist.storage.requireValidExcel
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.io.use
import kotlin.use

class ExcelVocabularyWriter : VocabularyWriter {
    override fun save(path: String, vocabulary: Vocabulary): = runCatching {
        val oldFile = File(path).requireValidExcel()
        val tempFile = oldFile.createTempFile()
        val vocabularyMap = vocabulary.entries.associateBy { it.normalizedSourceText to it.normalizedTranslation }
        FileOutputStream(tempFile).use { outputStream ->
            Workbook(outputStream, "LinguaTwist", "1.0").use { workbook ->
                val worksheet = workbook.newWorksheet("Sheet1")
                for (pair in readPairsFromExcelRows(oldFile)) {
                    val entry = vocabularyMap[pair] ?: continue

                }

                // Write header
                worksheet.value(0, 0, "Original")
                worksheet.value(0, 1, "Translation")
                worksheet.value(0, 2, "Score")

                // Write data
                vocabulary.entries.forEachIndexed { index, pair ->
                    val row = index + 1
                    worksheet.value(row, 0, pair.sourceText)
                    worksheet.value(row, 1, pair.translation)
                    worksheet.value(row, 2, pair.score)
                }
            }
        }


    }

    fun writeEntry(entry: LexicalEntry, workSheet: Worksheet, rowIndex: Int) {
        workSheet.value(rowIndex, 0, entry.sourceText)
        workSheet.value(rowIndex, 1, entry.translation)
        workSheet.value(rowIndex, 2, entry.score)
    }

    fun extractNormalizedPair(row: Row): Pair<String, String> {
        val normalizedSourceText = row.getCellAsString(0).orElse("").trim().lowercase()
        val normalizedTranslation = row.getCellAsString(1).orElse("").trim().lowercase()
        return normalizedSourceText to normalizedTranslation
    }

    fun readPairsFromExcelRows(file: File): Sequence<Pair<String, String>> = sequence {
        FileInputStream(file).use { inputStream ->
            ReadableWorkbook(inputStream).use { workbook ->
                val sheet = workbook.firstSheet
                sheet.openStream().use { rowStream ->
                    val iterator = rowStream.iterator()
                    require(iterator.hasNext()) { "Excel file is empty" }
                    val header = iterator.next()
                    require(header.cellCount >= 2) { "Excel file header must contain at least two columns" }
                    while (iterator.hasNext()) {
                        val row = iterator.next()
                        if (row.cellCount < 2) continue
                        val pair = extractNormalizedPair(row)
                        yield(pair)
                    }
                }
            }
        }
    }
}
