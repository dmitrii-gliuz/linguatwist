package org.dmitriigliuz.linguatwist.legacy.storage

import java.io.File

object FileUtils {

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
            throw _root_ide_package_.org.dmitriigliuz.linguatwist.legacy.storage.StorageException("Failed to delete original file: ${originalFile.path}")
        }
        if (!tempFile.renameTo(originalFile)) {
            throw _root_ide_package_.org.dmitriigliuz.linguatwist.legacy.storage.StorageException("Failed to replace file: ${originalFile.path}")
        }
    }
}