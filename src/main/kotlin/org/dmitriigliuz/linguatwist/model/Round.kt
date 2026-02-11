package org.dmitriigliuz.linguatwist.model

import org.dmitriigliuz.linguatwist.legacy.application.context.AppContext

class Round(val pairs: List<LexicalEntry>, val number: Int) {
    val index = number - 1
    val shuffledIndexes = (0 until pairs.size).shuffled()
    var guessedPairs = 0
    var mistakes = 0
    private val turns = mutableListOf<List<Int>>()

    fun isFinished() = guessedPairs == pairs.size

    private fun realIndexFromShuffled(indexes: List<Int>) = shuffledIndexes[indexes[1]]

    private fun pairInFirstColumn(indexes: List<Int>) = pairs[indexes.first()]

    private fun pairInSecondColumn(indexes: List<Int>) = pairs[realIndexFromShuffled(indexes)]

    private fun getTwoWordsByIndexes(indexes: List<Int>) = listOf(
        pairInFirstColumn(indexes).originalWord,
        pairInSecondColumn(indexes).translation
        )

    private fun validateIndexes(indexes: List<Int>, context: AppContext) = with(indexes) {
        if (size != 2) {
            throw IllegalArgumentException(context.getString("YouShouldEnterTwoIndexes"))
        } else if (any { it !in 0 until pairs.size}) {
            throw IllegalArgumentException("${context.getString("IndexesShouldBeInTheRange")} $pairs.size")
        }else if (turns.contains(this)) {
            throw IllegalArgumentException(context.getString("ThisPairOfIndexesHasAlreadyBeenUsed"))
        } else if (pairInFirstColumn(this).guessed || pairInSecondColumn(this).guessed) {
            throw IllegalArgumentException(context.getString("ThisPairHasAlreadyBeenGuessed"))
        }
    }

    private fun areIndexesBelongToOnePair(indexes: List<Int>) = indexes.first() == realIndexFromShuffled(indexes)

    fun choosePair(indexes: List<Int>, context: AppContext): Pair<Boolean, List<String>> {
//        TODO Update logic according to immutable WordPair
        validateIndexes(indexes, context)
        turns.add(indexes)
        val pairInFirstColumn = pairInFirstColumn(indexes)
        return Pair(if (areIndexesBelongToOnePair(indexes)) {
            //        TODO Update logic according to immutable WordPair
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