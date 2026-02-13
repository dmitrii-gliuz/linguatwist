package org.dmitriigliuz.linguatwist.model

enum class ValidationError {
    INDEX_OUT_OF_RANGE,
    PAIR_HAS_ALREADY_BEEN_GUESSED
}

sealed class RoundResult {
    data class Correct(val words: Pair<String, String>, val updatedRound: Round) : RoundResult()
    data class Wrong(val words: Pair<String, String>, val updatedRound: Round) : RoundResult()
    data class Invalid(val error: ValidationError) : RoundResult()
}

data class Round(
    val pairs: List<LexicalEntry>,
    val number: Int,
    val guessedIndexes: Set<Int> = emptySet(),
    val mistakesCount: Int = 0,
    val rightColumnPermutation: List<Int> = (0 until pairs.size).shuffled()
) {
    val index = number - 1
    val guessedCount: Int
    get() = guessedIndexes.size

    val isFinished: Boolean
        get() = guessedIndexes.size == pairs.size

    private fun originalPairIndex(rightColumnIndex: Int) = rightColumnPermutation[rightColumnIndex]

    private fun pairAtLeftColumn(leftIndex: Int) = pairs[leftIndex]

    private fun pairAtRightColumn(rightIndex: Int) = pairs[originalPairIndex(rightIndex)]

    private fun getSelectedWords(selection: Pair<Int, Int>) = Pair(
        pairAtLeftColumn(selection.first).originalWord,
        pairAtRightColumn(selection.second).translation
    )

    private fun isCorrectMatch(selection: Pair<Int, Int>) =
        selection.first == originalPairIndex(selection.second)

    fun choosePair(selection: Pair<Int, Int>): RoundResult {
        val (leftIndex, rightIndex) = selection

        if (leftIndex !in 0 until pairs.size || rightIndex !in 0 until pairs.size) {
            return RoundResult.Invalid(ValidationError.INDEX_OUT_OF_RANGE)
        }
        if (pairAtLeftColumn(leftIndex).isGuessed || pairAtRightColumn(rightIndex).isGuessed) {
            return RoundResult.Invalid(ValidationError.PAIR_HAS_ALREADY_BEEN_GUESSED)
        }

        val selectedWords = getSelectedWords(selection)
        return if (isCorrectMatch(selection)) {
            val updatedPairs = pairs.mapIndexed { i, pair -> if (i == leftIndex) pair.withGuessed() else pair }
            val updatedGuessedIndexes = guessedIndexes + leftIndex
            val updatedRound = copy(pairs = updatedPairs, guessedIndexes = updatedGuessedIndexes)
            RoundResult.Correct(selectedWords,  updatedRound)
        } else {
            val rightPairOriginalIndex = originalPairIndex(rightIndex)
            val updatedPairs = pairs.mapIndexed { i, pair ->
                when (i) {
                    leftIndex -> pair.withPenalty()
                    rightPairOriginalIndex -> pair.withPenalty()
                    else -> pair
                }
            }
            val updatedMistakes = mistakesCount + 1
            val updatedRound = copy(pairs = updatedPairs, mistakesCount = updatedMistakes)
            RoundResult.Wrong(selectedWords, updatedRound)
        }
    }
}