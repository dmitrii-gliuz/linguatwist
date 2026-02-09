package org.dmitriigliuz.linguatwist.infrastructure.storage

import org.dmitriigliuz.linguatwist.infrastructure.storage.model.RawDataRow
import org.dmitriigliuz.linguatwist.infrastructure.storage.model.WriteOptions

interface DataWriter {
    fun writeData(
        filePath: String,
        headers: List<String>?,
        rows: List<List<Any?>>,
        options: WriteOptions = WriteOptions()
    )

    fun writeRawData(
        filePath: String,
        rawRows: List<RawDataRow>,
        headers: List<String>? = null,
        options: WriteOptions = WriteOptions()
    )

    fun supports(fileExtension: String): Boolean
}