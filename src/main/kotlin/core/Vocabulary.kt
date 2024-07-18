package org.dmitriigliuz.linguatwist.core

class Vocabulary(val wordPairs: List<WordPair>) {
    val size = wordPairs.size
    private val unusedPairs = getShuffledPairsSortedByScore()

    private fun getShuffledPairsSortedByScore() = wordPairs.shuffled().sortedBy { it.score }.toMutableList()

    fun takeGroupOfPairs(numberOfPairs: Int): List<WordPair> {
        val seen = mutableListOf<WordPair>()
        val newGroup = mutableListOf<WordPair>()
        while (newGroup.size < numberOfPairs) {
            if (seen.size + newGroup.size == wordPairs.size) {
                throw IllegalStateException("All words are used")
            }
            if (unusedPairs.size < numberOfPairs) {
                unusedPairs.clear()
                unusedPairs.addAll(getShuffledPairsSortedByScore().filter { !seen.contains(it) && !newGroup.contains(it) })
                unusedPairs.forEach { it.guessed = false }
            }
            val wordPair = unusedPairs.removeFirst()
            if (seen.contains(wordPair) || newGroup.contains(wordPair)) continue
            if (newGroup.checkIfOneOfWordsAlreadyUsed(wordPair)) {
                seen.add(wordPair)
                continue
            }
            newGroup.add(wordPair)
        }
        unusedPairs.removeAll(newGroup)
        return newGroup
    }
}