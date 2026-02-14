package org.dmitriigliuz.linguatwist.legacy.storage.model

data class RawDataCell(
    val row: Int,
    val column: Int,
    val stringValue: String?,
    val numericValue: Double?,
    val isEmpty: Boolean
) {
    fun asString() = stringValue?.takeIf { it.isNotBlank() }
    fun asInt() = numericValue?.toInt()
    fun asDouble() = numericValue
}
