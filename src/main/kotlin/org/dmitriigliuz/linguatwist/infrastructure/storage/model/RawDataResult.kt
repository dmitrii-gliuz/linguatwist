package org.dmitriigliuz.linguatwist.infrastructure.storage.model

data class RawDataResult(
    val rows: List<RawDataRow>,
    val totalRows: Int,
    val maxColumns: Int,
    val sourceFormat: String
) {
    fun getRow(index: Int) = rows.find { it.index == index }

    fun getRowsStartingFrom(startIndex: Int) = rows.filter { it.index >= startIndex }

    val hasData: Boolean
        get() = rows.isNotEmpty()
}
