package org.dmitriigliuz.linguatwist.model

data class LexemeKey(val normalizedSourceText: String, val normalizedTranslation: String) {
    companion object {
        fun of(sourceText: String, translation: String) =
            LexemeKey(sourceText.trim().lowercase(), translation.trim().lowercase())
    }
}


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
