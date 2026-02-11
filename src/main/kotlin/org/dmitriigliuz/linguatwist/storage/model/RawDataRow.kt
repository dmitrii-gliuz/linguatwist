package org.dmitriigliuz.linguatwist.storage.model

data class RawDataRow(
    val index: Int,
    val cells: List<RawDataCell>
) {
    fun getCell(columnIndex: Int) = cells.find { it.column == columnIndex }

    fun getCellValue(columnIndex: Int) = getCell(columnIndex)?.asString()

    fun getCellIntValue(columnIndex: Int) = getCell(columnIndex)?.asInt()

    val  isEmpty: Boolean
        get() = cells.all { it.isEmpty }
}
