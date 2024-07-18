package org.dmitriigliuz.linguatwist.core

fun String.isSame(original: String): Boolean {
    return this.lowercase().trim() == original.lowercase().trim()
}