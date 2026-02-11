package org.dmitriigliuz.linguatwist.storage.excel.model

import org.dmitriigliuz.linguatwist.storage.model.RawDataRow

data class ExcelRowData(val row: org.dmitriigliuz.linguatwist.storage.model.RawDataRow, val maxColumns: Int)
