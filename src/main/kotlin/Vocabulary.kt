package org.dmitriigliuz.learnnewwords

data class WordPair(val originalWord: String, val translation: String) {
    var guessed = false

    private fun getStrikethroughOrNot(text: String) = if (guessed) text.strikethrough() else text

    fun getOriginalWordStrikethroughOrNot() = getStrikethroughOrNot(originalWord)

    fun getTranslationStrikethroughOrNot() = getStrikethroughOrNot(translation)
}

class Vocabulary(private val wordPairs: List<WordPair>) {
    val size = wordPairs.size
    private val unusedPairs = getShuffledPairs().toMutableList()

    private fun getShuffledPairs() = wordPairs.shuffled()

    fun optimalNumberOfRounds() = size / numberOfWordsInRound

    fun takeGroupOfPairs(numberOfPairs: Int): List<WordPair> {
        if (unusedPairs.size < numberOfPairs) {
            unusedPairs.clear()
            unusedPairs.addAll(getShuffledPairs())
        }
        val group = unusedPairs.take(numberOfPairs)
        unusedPairs.removeAll(group)
        return group
    }
}