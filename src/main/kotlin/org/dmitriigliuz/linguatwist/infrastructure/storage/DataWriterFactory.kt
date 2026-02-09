package org.dmitriigliuz.linguatwist.infrastructure.storage

import org.dmitriigliuz.linguatwist.infrastructure.storage.excel.ExcelDataWriter

class DataWriterFactory {
    companion object {
        private val writers = listOf(ExcelDataWriter())

        fun createWriter(filePath: String): DataWriter {
            val extension = FileUtils.getFileExtension(filePath)
            return writers.find { it.supports(extension) }
                ?: throw StorageException("No writer found for file extension: $extension")
        }

        fun isSupported(filePath: String): Boolean {
            val extension = FileUtils.getFileExtension(filePath)
            return writers.any { it.supports(extension) }
        }

        fun getSupportedExtensions() = listOf("xlsx", "xls")
    }
}