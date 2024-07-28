package org.dmitriigliuz.linguatwist.core

data class WordPair(val originalWord: String, val translation: String, var score: Int = 0) {
    var guessed = false

    fun markAsGuessedAndIncreaseScore() {
        guessed = true
        score++
    }

    fun decreaseScore() { score-- }

    fun containsOneOfWords(other: WordPair): Boolean {
        return originalWord.isSame(other.originalWord) || translation.isSame(other.translation)
    }
}

fun List<WordPair>.alreadyContains(original: String, translation: String): Boolean {
    return any { it.originalWord.isSame(original) && it.translation.isSame(translation) }
}

fun List<WordPair>.alreadyContainsOneOfWords(anotherWordPair: WordPair): Boolean {
    forEach { if (it.containsOneOfWords(anotherWordPair)) return true }
    return false
}