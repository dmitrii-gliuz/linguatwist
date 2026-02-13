package org.dmitriigliuz.linguatwist.model

sealed class GameState {
    data class Initial(val vocabulary: Vocabulary) : GameState()
    data class InProgress(
        val round: Round,
        val score: Int,
        val mistakesCount: Int,
        val vocabulary: Vocabulary,
        val error: ValidationError? = null
    ) : GameState() {
        fun totalGuessedAmount() = round.guessedCount + (round.number - 1) * round.pairs.size
    }
    data class Finished(val result: GameResult) : GameState()
    data class Failed(val result: GameResult) : GameState()
}