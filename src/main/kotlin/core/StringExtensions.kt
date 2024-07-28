package org.dmitriigliuz.linguatwist.core

fun String.isSame(original: String) = this.lowercase().trim() == original.lowercase()