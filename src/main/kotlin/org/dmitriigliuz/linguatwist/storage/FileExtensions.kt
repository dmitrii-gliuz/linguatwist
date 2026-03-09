package org.dmitriigliuz.linguatwist.storage

import java.io.File

fun File.requireReadable() = also {
    require(exists()) { "File does not exist: $absolutePath" }
    require(canRead()) { "File is not readable: $absolutePath" }
}

fun File.requireValidExcel() = requireReadable(). also {
    require(extension == "xlsx") {
    "File extension is not supported: ${extension}, it should be 'xlsx'"
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

