package org.dmitriigliuz.linguatwist.domain.model.extensions

fun String.isSame(original: String) = this.lowercase().trim() == original.lowercase()