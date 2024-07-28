package org.dmitriigliuz.linguatwist.core

class Vocabulary(val wordPairs: List<WordPair>) {
    val size = wordPairs.size
    private val unusedPairs = getShuffledPairsSortedByScore()

    private fun getShuffledPairsSortedByScore() = wordPairs.shuffled().sortedBy { it.score }.toMutableList()

    fun takeGroupOfPairs(numberOfPairs: Int): List<WordPair> {
        val seen = mutableListOf<WordPair>()
        val newGroup = mutableListOf<WordPair>()
        while (newGroup.size < numberOfPairs) {
//            Just in case to prevent infinite loop
            if (seen.size + newGroup.size == wordPairs.size) {
                throw IllegalStateException(localeString("AllWordsAreUsed"))
            }
            if (unusedPairs.size < numberOfPairs) with (unusedPairs) {
                clear()
                addAll(getShuffledPairsSortedByScore().filter { !seen.contains(it) && !newGroup.contains(it) })
                forEach { it.guessed = false }
            }
            val wordPair = unusedPairs.removeFirst()
            if (seen.contains(wordPair) || newGroup.contains(wordPair)) continue
            if (newGroup.alreadyContainsOneOfWords(wordPair)) {
                seen.add(wordPair)
                continue
            }
            newGroup.add(wordPair)
        }
        unusedPairs.removeAll(newGroup)
        return newGroup
    }
}