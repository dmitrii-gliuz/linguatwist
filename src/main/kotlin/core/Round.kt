package org.dmitriigliuz.linguatwist.core

class Round(val pairs: List<WordPair>, val number: Int) {
    val size = pairs.size
    val shuffledIndexes = (0 until size).shuffled()
    var guessedPairs = 0
    var mistakes = 0
    private val turns = mutableListOf<List<Int>>()


    fun isFinished() = guessedPairs == size

    private fun getPairInFirstColumn(indexes: List<Int>) = pairs[indexes[0]]

    private fun getPairInSecondColumn(indexes: List<Int>) = pairs[shuffledIndexes[indexes[1]]]

    private fun getTwoWordsByIndexes(indexes: List<Int>) = listOf(
        getPairInFirstColumn(indexes).originalWord,
        getPairInSecondColumn(indexes).translation
        )

    private fun validateIndexes(indexes: List<Int>) {
        if (indexes.size != 2) {
            throw IllegalArgumentException(localeString("YouShouldEnterTwoIndexes"))
        }
        if (indexes.any { it !in 0 until size }) {
            throw IllegalArgumentException("${localeString("IndexesShouldBeInTheRange")} $size")
        }
        if (turns.contains(indexes)) {
            throw IllegalArgumentException(localeString("ThisPairOfIndexesHasAlreadyBeenUsed"))
        }
        val pairInFirstColumn = pairs[indexes[0]]
        val pairInSecondColumn = getPairInSecondColumn(indexes)
        if (pairInFirstColumn.guessed || pairInSecondColumn.guessed) {
            throw IllegalArgumentException(localeString("ThisPairHasAlreadyBeenGuessed"))
        }
    }

    private fun areIndexesBelongToOnePair(indexes: List<Int>): Boolean = indexes[0] == shuffledIndexes[indexes[1]]

    fun choosePair(indexes: List<Int>): Pair<Boolean, List<String>> {
        validateIndexes(indexes)
        turns.add(indexes)
        val pairInFirstColumn = getPairInFirstColumn(indexes)
        val pairInSecondColumn = getPairInSecondColumn(indexes)
        return Pair(if (areIndexesBelongToOnePair(indexes)) {
            pairInFirstColumn.markAsGuessedAndIncreaseScore()
            guessedPairs++
            true
        } else {
            pairInFirstColumn.decreaseScore()
            pairInSecondColumn.decreaseScore()
            mistakes++
            false
        }, getTwoWordsByIndexes(indexes))
    }
}