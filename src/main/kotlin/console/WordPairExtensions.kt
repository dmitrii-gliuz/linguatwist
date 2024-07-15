package org.dmitriigliuz.linguatwist.console

import org.dmitriigliuz.linguatwist.core.WordPair

fun WordPair.originalWordLength() = originalWord.length

fun WordPair.applyStrikethroughIfGuessed(text: String) = if (guessed) text.strikethrough() else text

fun WordPair.originalWordStrikethroughIfGuessed() = applyStrikethroughIfGuessed(originalWord)

fun WordPair.translationStrikethroughIfGuessed() = applyStrikethroughIfGuessed(translation)