package org.dmitriigliuz.linguatwist.model.extensions

fun String.isSame(original: String) = this.lowercase().trim() == original.lowercase()