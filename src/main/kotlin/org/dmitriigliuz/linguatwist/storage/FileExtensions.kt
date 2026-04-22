package org.dmitriigliuz.linguatwist.storage

import org.dmitriigliuz.linguatwist.i18n.Messages
import java.io.File

fun File.requireReadable() = also {
    require(exists()) { "${Messages.file_error_notExists}: $absolutePath" }
    require(canRead()) { "${Messages.file_error_notReadable}: $absolutePath" }
}

fun File.requireValidExcel() = requireReadable(). also {
    require(extension.lowercase() == "xlsx") {
    Messages.file_error_unsupportedExtension(extension)
    }
}

fun File.generateTempFileName(suffix: String = "_temp") = buildString {
    append(nameWithoutExtension)
    append(suffix)
    if (extension.isNotEmpty()) append(".${extension}")
}

fun File.createTempFile(suffix: String = "_temp"): File {
    return File(parent, generateTempFileName(suffix))
}

