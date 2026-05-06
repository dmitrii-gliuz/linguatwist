package org.dmitriigliuz.linguatwist.model

sealed class GameState {
    data class Initial(val vocabulary: Vocabulary) : GameState()
    data class InProgress(
        val round: Round,
        val gameScore: Int,
        val mistakesCount: Int,
        val vocabulary: Vocabulary,
        val error: ValidationError? = null,
        val totalPairs: Int
    ) : GameState() {
        val completionPercentage: Int
            get() = totalGuessedAmount * 100 / totalPairs

        val totalGuessedAmount
            get() = round.guessedCount + (round.previousNumber) * round.size
    }
    data class Finished(val result: GameResult, val vocabulary: Vocabulary) : GameState()
    data class Failed(val result: GameResult, val vocabulary: Vocabulary) : GameState()
    object Aborted : GameState()

    fun getResultOrNull() = when (this) {
        is Finished -> result
        is Failed -> result
        else -> null
    }

    fun getUpdatedVocabularyOrNull() = when (this) {
        is Finished -> vocabulary
        is Failed -> vocabulary
        is InProgress -> vocabulary
        else -> null
    }
}
