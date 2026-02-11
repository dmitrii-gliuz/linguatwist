package org.dmitriigliuz.linguatwist.presentation.cli

import org.dmitriigliuz.linguatwist.domain.model.LexicalEntry

fun LexicalEntry.applyStrikethroughIfGuessed(text: String) = if (guessed) text.strikethrough() else text