package org.dmitriigliuz.linguatwist.domain.model

import org.dmitriigliuz.linguatwist.domain.model.extensions.isSame


data class LexicalEntry(
    val originalWord: String,
    val translation: String,
    val guessed: Boolean = false,
    val score: Int = 0
) {
    fun withGuessed() = copy(guessed = true, score = score + 1)
    fun withoutGuessed() = copy(guessed = false)
    fun withPenalty() = copy(score = score - 1)
    fun sameAs(other: LexicalEntry) = originalWord.isSame(other.originalWord) && translation.isSame(other.translation)

    fun overlaps(other: LexicalEntry): Boolean {
        return originalWord.isSame(other.originalWord) || translation.isSame(other.translation)
    }
}
