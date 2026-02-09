package org.dmitriigliuz.linguatwist.infrastructure.storage

import java.io.File

object FileUtils {
    fun requireFileExists(filePath: String): File {
        val file = File(filePath)
        if (!file.exists()) throw StorageException("File or directory not found: $filePath")
        return file
    }

    fun requireFileReadable(filePath: String): File = requireFileExists(filePath).apply {
        if (!canRead()) throw StorageException("File is not readable: $filePath")
    }

    fun getWithConditionOrNull(filePath: String, condition: (File) -> Boolean): File? {
        val file = File(filePath)
        return if (file.exists() && condition(file)) file else null
    }

    fun getReadableFileOrNull(filePath: String): File? = getWithConditionOrNull(filePath) { it.canRead() }

    fun getWriteableFileOrNull(filePath: String): File? = getWithConditionOrNull(filePath) { it.canWrite() }

    fun requireDirectoryWriteable(directoryPath: String): File = requireFileExists(directoryPath).apply {
        if (!isDirectory) throw StorageException("Path is not a directory: $directoryPath")
        if (!canWrite()) throw StorageException("Directory is not writeable: $directoryPath")
    }

    fun fileExists(filePath: String): Boolean = File(filePath).exists()

    fun isFileReadable(filePath: String): Boolean {
        val file = File(filePath)
        return file.exists() && file.canRead()
    }

    fun getFileExtension(filePath: String): String = File(filePath).extension.lowercase()

    fun generateTempFileName(originalFilename: String, suffix: String = "_temp"): String {
        val file = File(originalFilename)
        val nameWithoutExtension = file.nameWithoutExtension
        val extension = file.extension
        return if (extension.isNotEmpty()) {
            "$nameWithoutExtension$suffix.$extension"
        } else {
            "$nameWithoutExtension$suffix"
        }
    }

    fun getTempFile(originalFile: File, suffix: String = "_temp"): File {
        val tempFileName = generateTempFileName(originalFile.name, suffix)
        return File(originalFile.parent, tempFileName)
    }

    fun safeReplaceFile(originalFile: File, tempFile: File) {
        if (!originalFile.delete()) {
            throw StorageException("Failed to delete original file: ${originalFile.path}")
        }
        if (!tempFile.renameTo(originalFile)) {
            throw StorageException("Failed to replace file: ${originalFile.path}")
        }
    }

    fun isSupportedExtension(filePath: String, supportedExtensions: Set<String>): Boolean {
        return getFileExtension(filePath) in supportedExtensions
    }
}