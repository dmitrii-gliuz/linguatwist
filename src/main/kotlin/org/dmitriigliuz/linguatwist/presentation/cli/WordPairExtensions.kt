package org.dmitriigliuz.linguatwist.presentation.cli

import org.dmitriigliuz.linguatwist.domain.model.WordPair

fun WordPair.applyStrikethroughIfGuessed(text: String) = if (guessed) text.strikethrough() else text