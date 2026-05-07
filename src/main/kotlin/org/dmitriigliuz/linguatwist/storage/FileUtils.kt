package org.dmitriigliuz.linguatwist.storage

import org.dmitriigliuz.linguatwist.i18n.Messages
import java.io.File
import java.nio.file.Files


fun createBackup(path: String) = File(path).copyTo(File("$path.bak"), overwrite = true)


fun File.requireReadable() = also {
    require(exists()) { "${Messages.file_error_notExists}: $absolutePath" }
    require(canRead()) { "${Messages.file_error_notReadable}: $absolutePath" }
}

fun File.requireValidExcel() = requireReadable(). also {
    require(extension.lowercase() == "xlsx") {
    Messages.file_error_unsupportedExtension(extension)
    }
}

fun File.createTempFile(): File = Files.createTempFile(
    parentFile.toPath(),
    nameWithoutExtension,
    ".$extension"
).toFile()