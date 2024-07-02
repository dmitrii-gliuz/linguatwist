package org.dmitriigliuz.learnnewwords

data class WordPair(val originalWord: String, val translation: String) {
    var guessed = false

    fun originalWordLength() = originalWord.length

    private fun applyStrikethroughIfGuessed(text: String) = if (guessed) text.strikethrough() else text

    fun originalWordStrikethroughIfGuessed() = applyStrikethroughIfGuessed(originalWord)

    fun translationStrikethroughIfGuessed() = applyStrikethroughIfGuessed(translation)
}

class Vocabulary(private val wordPairs: List<WordPair>) {
    val size = wordPairs.size
    private val unusedPairs = wordPairs.shuffled().toMutableList()
    val optimalNumberOfRounds
        get() = size / numberOfWordsInRound

    fun takeGroupOfPairs(numberOfPairs: Int): List<WordPair> {
        if (unusedPairs.size < numberOfPairs) {
            unusedPairs.clear()
            unusedPairs.addAll(wordPairs.shuffled())
        }
        val group = unusedPairs.take(numberOfPairs)
        unusedPairs.removeAll(group)
        return group
    }
}