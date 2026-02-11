package org.dmitriigliuz.linguatwist.storage

import org.dmitriigliuz.linguatwist.storage.excel.ExcelDataLoader

class DataLoaderFactory {
    companion object {
        private val loaders = listOf(_root_ide_package_.org.dmitriigliuz.linguatwist.storage.excel.ExcelDataLoader())

        fun createLoader(filePath: String): org.dmitriigliuz.linguatwist.storage.DataLoader {
            val extension = _root_ide_package_.org.dmitriigliuz.linguatwist.storage.FileUtils.getFileExtension(filePath)
            return loaders.find { it.supports(extension) }
                ?: throw _root_ide_package_.org.dmitriigliuz.linguatwist.storage.StorageException("No loader found for file extension: $extension")
        }

        fun isSupported(filePath: String): Boolean {
            val extension = _root_ide_package_.org.dmitriigliuz.linguatwist.storage.FileUtils.getFileExtension(filePath)
            return loaders.any { it.supports(extension) }
        }

        fun getSupportedExtensions() = listOf("xlsx", "xls")
    }
}