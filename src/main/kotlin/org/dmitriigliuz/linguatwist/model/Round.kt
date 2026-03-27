package org.dmitriigliuz.linguatwist.model

enum class ValidationError {
    INDEX_OUT_OF_RANGE,
    PAIR_HAS_ALREADY_BEEN_GUESSED,
    PAIR_HAS_ALREADY_BEEN_WRONG
}

sealed class RoundResult {
    data class Correct(val words: Pair<String, String>, val updatedRound: Round) : RoundResult()
    data class Wrong(val words: Pair<String, String>, val updatedRound: Round) : RoundResult()
    data class Invalid(val error: ValidationError) : RoundResult()
}

data class Round(
    private val pairs: List<LexicalEntry>,
    val number: Int,
    val guessedIndexes: Set<Int> = emptySet(),
    val wrongAttempts: Set<Pair<Int, Int>> = emptySet(),
    val mistakesInRound: Int = 0,
    val rightColumnPermutation: List<Int> = pairs.indices.shuffled()
) {
    val previousNumber = number - 1
    val guessedCount: Int
        get() = guessedIndexes.size

    val isFinished: Boolean
        get() = guessedIndexes.size == pairs.size

    val size: Int
        get() = pairs.size

    fun originalPairIndex(rightColumnIndex: Int) = rightColumnPermutation[rightColumnIndex]

    fun pairAtLeftColumn(leftIndex: Int): LexicalEntry = pairs[leftIndex]

    fun pairAtRightColumn(rightIndex: Int) = pairs[originalPairIndex(rightIndex)]

    private fun getSelectedWords(selection: Pair<Int, Int>) = Pair(
        pairAtLeftColumn(selection.first).sourceText,
        pairAtRightColumn(selection.second).translation
    )

    private fun isCorrectMatch(selection: Pair<Int, Int>) =
        selection.first == originalPairIndex(selection.second)

    fun choosePair(selection: Pair<Int, Int>): RoundResult {
        val (leftIndex, rightIndex) = selection

        return if (leftIndex !in pairs.indices || rightIndex !in pairs.indices) {
             RoundResult.Invalid(ValidationError.INDEX_OUT_OF_RANGE)
        } else if (pairAtLeftColumn(leftIndex).isGuessed || pairAtRightColumn(rightIndex).isGuessed) {
            RoundResult.Invalid(ValidationError.PAIR_HAS_ALREADY_BEEN_GUESSED)
        } else if (selection in wrongAttempts) {
            RoundResult.Invalid(ValidationError.PAIR_HAS_ALREADY_BEEN_WRONG)
        } else {
            val selectedWords = getSelectedWords(selection)
            if (isCorrectMatch(selection)) {
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
                val updatedWrongAttempts = wrongAttempts + selection
                val updatedMistakes = mistakesInRound + 1
                val updatedRound = copy(
                    pairs = updatedPairs,
                    wrongAttempts = updatedWrongAttempts,
                    mistakesInRound = updatedMistakes
                )
                RoundResult.Wrong(selectedWords, updatedRound)
            }
        }


    }
}