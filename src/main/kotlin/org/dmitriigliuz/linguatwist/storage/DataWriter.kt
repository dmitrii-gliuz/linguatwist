package org.dmitriigliuz.linguatwist.storage

import org.dmitriigliuz.linguatwist.storage.model.RawDataRow
import org.dmitriigliuz.linguatwist.storage.model.WriteOptions

interface DataWriter {
    fun writeData(
        filePath: String,
        headers: List<String>?,
        rows: List<List<Any?>>,
        options: org.dmitriigliuz.linguatwist.storage.model.WriteOptions = _root_ide_package_.org.dmitriigliuz.linguatwist.storage.model.WriteOptions()
    )

    fun writeRawData(
        filePath: String,
        rawRows: List<org.dmitriigliuz.linguatwist.storage.model.RawDataRow>,
        headers: List<String>? = null,
        options: org.dmitriigliuz.linguatwist.storage.model.WriteOptions = _root_ide_package_.org.dmitriigliuz.linguatwist.storage.model.WriteOptions()
    )

    fun supports(fileExtension: String): Boolean
}