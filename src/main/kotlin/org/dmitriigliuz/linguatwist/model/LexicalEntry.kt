package org.dmitriigliuz.linguatwist.model


data class LexicalEntry(
    val sourceText: String,
    val translation: String,
    val isGuessed: Boolean = false,
    val entryScore: Int = 0,
) {
    val key: LexemeKey = LexemeKey.of(sourceText, translation)

    fun withGuessed() = copy(isGuessed = true, entryScore = entryScore + 1)
    fun withoutGuessed() = copy(isGuessed = false)
    fun withPenalty() = copy(entryScore = entryScore - 1)
}
