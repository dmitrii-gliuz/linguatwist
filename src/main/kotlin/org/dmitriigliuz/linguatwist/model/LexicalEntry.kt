package org.dmitriigliuz.linguatwist.model

import org.dmitriigliuz.linguatwist.legacy.presentation.cli.strikethrough
import org.dmitriigliuz.linguatwist.model.extensions.isSame


data class LexicalEntry(
    val originalWord: String,
    val translation: String,
    val isGuessed: Boolean = false,
    val score: Int = 0
) {
    fun withGuessed() = copy(isGuessed = true, score = score + 1)
    fun withoutGuessed() = copy(isGuessed = false)
    fun withPenalty() = copy(score = score - 1)
    fun sameAs(other: LexicalEntry) = originalWord.isSame(other.originalWord) && translation.isSame(other.translation)

    fun overlaps(other: LexicalEntry): Boolean {
        return originalWord.isSame(other.originalWord) || translation.isSame(other.translation)
    }
}

fun LexicalEntry.applyStrikethroughIfGuessed(text: String) = if (isGuessed) text.strikethrough() else text
