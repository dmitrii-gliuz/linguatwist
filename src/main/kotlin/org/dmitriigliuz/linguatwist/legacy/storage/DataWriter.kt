package org.dmitriigliuz.linguatwist.legacy.storage

import org.dmitriigliuz.linguatwist.legacy.storage.model.RawDataRow
import org.dmitriigliuz.linguatwist.legacy.storage.model.WriteOptions

interface DataWriter {
    fun writeData(
        filePath: String,
        headers: List<String>?,
        rows: List<List<Any?>>,
        options: org.dmitriigliuz.linguatwist.legacy.storage.model.WriteOptions = _root_ide_package_.org.dmitriigliuz.linguatwist.legacy.storage.model.WriteOptions()
    )

    fun writeRawData(
        filePath: String,
        rawRows: List<org.dmitriigliuz.linguatwist.legacy.storage.model.RawDataRow>,
        headers: List<String>? = null,
        options: org.dmitriigliuz.linguatwist.legacy.storage.model.WriteOptions = _root_ide_package_.org.dmitriigliuz.linguatwist.legacy.storage.model.WriteOptions()
    )

    fun supports(fileExtension: String): Boolean
}