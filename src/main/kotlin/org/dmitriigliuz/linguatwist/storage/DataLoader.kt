package org.dmitriigliuz.linguatwist.storage

import org.dmitriigliuz.linguatwist.storage.model.RawDataResult

/**
 * Represents a mechanism for loading and structuring raw data from multiple sources.
 * This interface supports processing raw data into an organized, structured format
 * while maintaining metadata such as row count, column count, and source format.
 */
interface DataLoader {
    /**
     * Loads raw data based on the specified file pattern and processes it into a structured format.
     *
     * @param filePatt the file pattern used to locate and load the raw data
     * @return a [org.dmitriigliuz.linguatwist.storage.model.RawDataResult] containing the processed rows of data, total rows count,
     *         maximum number of columns, and the source data format
     */
    fun loadRawData(filePath: String): org.dmitriigliuz.linguatwist.storage.model.RawDataResult

    fun supports(fileExtension: String): Boolean
}