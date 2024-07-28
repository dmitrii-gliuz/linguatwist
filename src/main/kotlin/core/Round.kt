package org.dmitriigliuz.linguatwist.core

class Round(val pairs: List<WordPair>, val number: Int) {
    val index = number - 1
    val shuffledIndexes = (0 until numberOfPairsInRound).shuffled()
    var guessedPairs = 0
    var mistakes = 0
    private val turns = mutableListOf<List<Int>>()

    fun isFinished() = guessedPairs == numberOfPairsInRound

    private fun realIndexFromShuffled(indexes: List<Int>) = shuffledIndexes[indexes[1]]

    private fun pairInFirstColumn(indexes: List<Int>) = pairs[indexes.first()]

    private fun pairInSecondColumn(indexes: List<Int>) = pairs[realIndexFromShuffled(indexes)]

    private fun getTwoWordsByIndexes(indexes: List<Int>) = listOf(
        pairInFirstColumn(indexes).originalWord,
        pairInSecondColumn(indexes).translation
        )

    private fun validateIndexes(indexes: List<Int>) = with(indexes) {
        if (size != 2) {
            throw IllegalArgumentException(localeString("YouShouldEnterTwoIndexes"))
        } else if (any { it !in 0 until numberOfPairsInRound}) {
            throw IllegalArgumentException("${localeString("IndexesShouldBeInTheRange")} $numberOfPairsInRound")
        }else if (turns.contains(this)) {
            throw IllegalArgumentException(localeString("ThisPairOfIndexesHasAlreadyBeenUsed"))
        } else if (pairInFirstColumn(this).guessed || pairInSecondColumn(this).guessed) {
            throw IllegalArgumentException(localeString("ThisPairHasAlreadyBeenGuessed"))
        }
    }

    private fun areIndexesBelongToOnePair(indexes: List<Int>) = indexes.first() == realIndexFromShuffled(indexes)

    fun choosePair(indexes: List<Int>): Pair<Boolean, List<String>> {
        validateIndexes(indexes)
        turns.add(indexes)
        val pairInFirstColumn = pairInFirstColumn(indexes)
        return Pair(if (areIndexesBelongToOnePair(indexes)) {
            pairInFirstColumn.markAsGuessedAndIncreaseScore()
            guessedPairs++
            true
        } else {
            pairInFirstColumn.decreaseScore()
            pairInSecondColumn(indexes).decreaseScore()
            mistakes++
            false
        }, getTwoWordsByIndexes(indexes))
    }
}