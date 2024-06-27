package org.dmitriigliuz.learnnewwords

data class WordPair(val originalWord: String, val translation: String) {
    var guessed = false

    private fun getStrikethroughOrNot(text: String) = if (guessed) text.strikethrough() else text

    fun getOriginalWordStrikethroughOrNot() = getStrikethroughOrNot(originalWord)

    fun getTranslationStrikethroughOrNot() = getStrikethroughOrNot(translation)
}

//class Vocabulary {
//
//}