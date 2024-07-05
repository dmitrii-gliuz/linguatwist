package org.dmitriigliuz.linguatwist.core

import org.dmitriigliuz.linguatwist.console.strikethrough

data class WordPair(val originalWord: String, val translation: String, var score: Int = 0) {
    var guessed = false

    fun markAsGuessedAndIncreaseScore() {
        guessed = true
        score++
    }
    fun decreaseScore() {
        score--
    }

    fun originalWordLength() = originalWord.length

    private fun applyStrikethroughIfGuessed(text: String) = if (guessed) text.strikethrough() else text

    fun originalWordStrikethroughIfGuessed() = applyStrikethroughIfGuessed(originalWord)

    fun translationStrikethroughIfGuessed() = applyStrikethroughIfGuessed(translation)
}

class Vocabulary(val wordPairs: List<WordPair>) {
    val size = wordPairs.size
    private val unusedPairs = getShuffledPairsSortedByScore()
    val optimalNumberOfRounds
        get() = size / numberOfWordsInRound

    private fun getShuffledPairsSortedByScore() = wordPairs.shuffled().sortedBy { it.score }.toMutableList()

    fun takeGroupOfPairs(numberOfPairs: Int): List<WordPair> {
        if (unusedPairs.size < numberOfPairs) {
            unusedPairs.clear()
            unusedPairs.addAll(getShuffledPairsSortedByScore())
        }
        val group = unusedPairs.take(numberOfPairs)
        unusedPairs.removeAll(group)
        return group
    }
}