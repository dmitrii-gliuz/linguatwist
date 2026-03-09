package org.dmitriigliuz.linguatwist.model

data class LexicalEntry(
    val sourceText: String,
    val translation: String,
    val isGuessed: Boolean = false,
    val entryScore: Int = 0
) {
    val normalizedSourceText: String
        get() = sourceText.trim().lowercase()

    val normalizedTranslation: String
        get() = translation.trim().lowercase()

    fun withGuessed() = copy(isGuessed = true, entryScore = entryScore + 1)
    fun withoutGuessed() = copy(isGuessed = false)
    fun withPenalty() = copy(entryScore = entryScore - 1)
    fun isSourceTextSame(other: LexicalEntry) = normalizedSourceText == other.normalizedSourceText
    fun isTranslationSame(other: LexicalEntry) = normalizedTranslation == other.normalizedTranslation

    fun overlaps(other: LexicalEntry) = isSourceTextSame(other) || isTranslationSame(other)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LexicalEntry) return false
        return isSourceTextSame(other) && isTranslationSame(other)
    }

    override fun hashCode() = 31 * normalizedSourceText.hashCode() + normalizedTranslation.hashCode()
}
