package org.dmitriigliuz.linguatwist.infrastructure.storage.excel.model

import org.dmitriigliuz.linguatwist.infrastructure.storage.model.RawDataRow

data class ExcelRowData(val row: RawDataRow, val maxColumns: Int)
