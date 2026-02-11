package org.dmitriigliuz.linguatwist.domain.vocabulary

import org.dmitriigliuz.linguatwist.application.context.AppContext
import org.dmitriigliuz.linguatwist.infrastructure.storage.DataWriterFactory
import org.dmitriigliuz.linguatwist.infrastructure.storage.model.WriteOptions

class VocabularyExporter(private val context: AppContext) {
    fun export(vocabulary: Vocabulary, filePath: String) {
        val writer = DataWriterFactory.Companion.createWriter(filePath)
        val headers = listOf("Original", "Translation", "Score")

        val rows = vocabulary.entries.map { pair ->
            listOf<Any>(pair.originalWord, pair.translation, pair.score)
        }

        val options = WriteOptions(
            sheetName = "Vocabulary",
            appName = context.config.appName,
            appVersion = context.config.appVersion
        )
        writer.writeData(filePath, headers, rows, options)
    }

    fun exportToExcel(vocabulary: Vocabulary, filePath: String) {
        val newFilePath = if (filePath.endsWith(".xlsx")) filePath else "$filePath.xlsx"
        export(vocabulary, newFilePath)
    }

}