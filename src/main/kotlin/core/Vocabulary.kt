package org.dmitriigliuz.linguatwist.core

class Vocabulary(val wordPairs: List<WordPair>) {
    val size = wordPairs.size
    private val unusedPairs = getShuffledPairsSortedByScore()
    val numberOfRoundsToCoverAllWords = size / numberOfWordsInRound + if (size % numberOfWordsInRound > 0) 1 else 0

    private fun getShuffledPairsSortedByScore() = wordPairs.shuffled().sortedBy { it.score }.toMutableList()

    fun takeGroupOfPairs(numberOfPairs: Int): List<WordPair> {
        if (unusedPairs.size < numberOfPairs) {
            unusedPairs.clear()
            unusedPairs.addAll(getShuffledPairsSortedByScore())
            unusedPairs.forEach { it.guessed = false }
        }
        val group = unusedPairs.take(numberOfPairs)
        unusedPairs.removeAll(group)
        return group
    }
}