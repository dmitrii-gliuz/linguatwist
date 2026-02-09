package org.dmitriigliuz.linguatwist.domain.vocabulary

import org.dmitriigliuz.linguatwist.application.context.AppContext
import org.dmitriigliuz.linguatwist.domain.model.WordPair
import org.dmitriigliuz.linguatwist.domain.model.alreadyContainsOneOfWords

class Vocabulary(val wordPairs: List<WordPair>) {
    val size = wordPairs.size
    private val unusedPairs = getShuffledPairsSortedByScore()



    private fun getShuffledPairsSortedByScore() = wordPairs.shuffled().sortedBy { it.score }.toMutableList()

    fun takeGroupOfPairs(context: AppContext): List<WordPair> {
        val pairsPerRound = context.config.pairsPerRound
        val seen = mutableListOf<WordPair>()
        val newGroup = mutableListOf<WordPair>()
        while (newGroup.size < pairsPerRound) {
//            Just in case to prevent infinite loop
            if (seen.size + newGroup.size == wordPairs.size) {
                throw IllegalStateException(context.getString("AllWordsAreUsed"))
            }
            if (unusedPairs.size < pairsPerRound) with (unusedPairs) {
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