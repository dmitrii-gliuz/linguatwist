package org.dmitriigliuz.linguatwist.storage

import org.dmitriigliuz.linguatwist.storage.excel.ExcelDataWriter

class DataWriterFactory {
    companion object {
        private val writers = listOf(_root_ide_package_.org.dmitriigliuz.linguatwist.storage.excel.ExcelDataWriter())

        fun createWriter(filePath: String): org.dmitriigliuz.linguatwist.storage.DataWriter {
            val extension = _root_ide_package_.org.dmitriigliuz.linguatwist.storage.FileUtils.getFileExtension(filePath)
            return writers.find { it.supports(extension) }
                ?: throw _root_ide_package_.org.dmitriigliuz.linguatwist.storage.StorageException("No writer found for file extension: $extension")
        }

        fun isSupported(filePath: String): Boolean {
            val extension = _root_ide_package_.org.dmitriigliuz.linguatwist.storage.FileUtils.getFileExtension(filePath)
            return writers.any { it.supports(extension) }
        }

        fun getSupportedExtensions() = listOf("xlsx", "xls")
    }
}