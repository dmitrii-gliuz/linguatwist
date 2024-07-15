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
}