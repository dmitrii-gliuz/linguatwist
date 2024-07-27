package org.dmitriigliuz.linguatwist.core

data class WordPair(val originalWord: String, val translation: String, var score: Int = 0) {
    var guessed = false

    fun markAsGuessedAndIncreaseScore() {
        guessed = true
        score++
    }
    fun decreaseScore() {
        score--
    }

    fun checkIfOneOfWordsIsSame(wordPair: WordPair): Boolean {
        return originalWord.isSame(wordPair.originalWord) || translation.isSame(wordPair.translation)
    }
}

fun List<WordPair>.checkIfPairWasUsed(original: String, translation: String): Boolean {
    return this.any { it.originalWord.isSame(original) && it.translation.isSame(translation) }
}

fun List<WordPair>.checkIfOneOfWordsAlreadyUsed(anotherWordPair: WordPair): Boolean {
    for (wordPair in this) {
        if (wordPair.checkIfOneOfWordsIsSame(anotherWordPair)) {
            return true
        }
    }
    return false
}