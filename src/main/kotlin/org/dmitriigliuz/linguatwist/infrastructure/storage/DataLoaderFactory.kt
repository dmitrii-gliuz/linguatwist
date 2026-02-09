package org.dmitriigliuz.linguatwist.infrastructure.storage

import org.dmitriigliuz.linguatwist.infrastructure.storage.excel.ExcelDataLoader

class DataLoaderFactory {
    companion object {
        private val loaders = listOf(ExcelDataLoader())

        fun createLoader(filePath: String): DataLoader {
            val extension = FileUtils.getFileExtension(filePath)
            return loaders.find { it.supports(extension) }
                ?: throw StorageException("No loader found for file extension: $extension")
        }

        fun isSupported(filePath: String): Boolean {
            val extension = FileUtils.getFileExtension(filePath)
            return loaders.any { it.supports(extension) }
        }

        fun getSupportedExtensions() = listOf("xlsx", "xls")
    }
}