package org.dmitriigliuz.linguatwist.infrastructure.storage.validation

import org.dmitriigliuz.linguatwist.application.context.AppContext
import org.dmitriigliuz.linguatwist.infrastructure.storage.DataLoaderFactory
import org.dmitriigliuz.linguatwist.infrastructure.storage.FileUtils

class FileValidator(context: AppContext) {
    val maxFileSize = context.config.maxFileSize
    fun validateVocableFile(filePath: String): FileValidationResult {
        val issues = mutableListOf<String>()

        try {
            val file = FileUtils.requireFileReadable(filePath)
            if (!DataLoaderFactory.isSupported(filePath)) {
                val supportedExtensions = DataLoaderFactory.getSupportedExtensions().joinToString { ", " }
                issues.add("File extension not supported. Supported extensions: $supportedExtensions")
            }
            if (file.length() == 0L) {
                issues.add("File is empty")
            } else if (file.length() > maxFileSize) {
                issues.add("File size exceeds 50MB")
            }


        } catch (e: Exception) {
            issues.add(e.message ?: "Unknown file validation error")
        }

        return FileValidationResult(issues.isEmpty(), issues)
    }
}