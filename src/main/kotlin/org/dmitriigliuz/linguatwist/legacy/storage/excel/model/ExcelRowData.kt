package org.dmitriigliuz.linguatwist.legacy.storage.excel.model

import org.dmitriigliuz.linguatwist.legacy.storage.model.RawDataRow

data class ExcelRowData(val row: org.dmitriigliuz.linguatwist.legacy.storage.model.RawDataRow, val maxColumns: Int)
