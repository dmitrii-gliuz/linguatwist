package org.dmitriigliuz.linguatwist.storage.model

data class WriteOptions(
    val sheetName: String = "Sheet1",
    val appName: String = "Application",
    val appVersion: String = "1.0",
    val encoding: String = "UTF-8",
    val delimiter: Char  = ',',
    val includeHeaders: Boolean = true,
    val overwriteFile: Boolean = true
)
