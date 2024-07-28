package org.dmitriigliuz.linguatwist.console

import org.dmitriigliuz.linguatwist.core.WordPair

fun WordPair.applyStrikethroughIfGuessed(text: String) = if (guessed) text.strikethrough() else text