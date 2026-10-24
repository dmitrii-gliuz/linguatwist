package org.dmitriigliuz.linguatwist.storage

import io.github.oshai.kotlinlogging.KotlinLogging
import org.dmitriigliuz.linguatwist.i18n.Messages
import java.io.File
import java.nio.file.Files


private val logger = KotlinLogging.logger { }

fun createBackup(path: String) = runCatching {
    File(path).copyTo(File("$path.bak"), overwrite = true)
}.fold(
    onSuccess = { Result.success(Unit) },
    onFailure = {
        logger.error(it) { "Failed to create backup for $path" }
        Result.failure(BackupFailedException(it))
    }
)

class BackupFailedException(cause: Throwable) : Exception(cause)


fun File.requireReadable() = also {
    require(exists()) { "${Messages.file_error_notExists()}: $absolutePath" }
    require(canRead()) { "${Messages.file_error_notReadable()}: $absolutePath" }
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