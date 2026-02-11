package org.dmitriigliuz.linguatwist.legacy.presentation.cli

import org.dmitriigliuz.linguatwist.model.LexicalEntry

fun LexicalEntry.applyStrikethroughIfGuessed(text: String) = if (guessed) text.strikethrough() else text