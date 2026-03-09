package org.dmitriigliuz.linguatwist.model

sealed class GameState {
    data class Initial(val vocabulary: Vocabulary) : GameState()
    data class InProgress(
        val round: Round,
        val gameScore: Int,
        val mistakesCount: Int,
        val vocabulary: Vocabulary,
        val error: ValidationError? = null,
        val updatedScores: Map<LexicalEntry, Int> = emptyMap()
    ) : GameState() {
        fun totalGuessedAmount() = round.guessedCount + (round.number - 1) * round.pairs.size
    }
    data class Finished(val result: GameResult, val updatedVocabulary: Vocabulary) : GameState()
    data class Failed(val result: GameResult, val updatedVocabulary: Vocabulary) : GameState()

    fun getResultOrNull() = when (this) {
        is Finished -> result
        is Failed -> result
        else -> null
    }

    fun getUpdatedVocabularyOrNull() = when (this) {
        is Finished -> updatedVocabulary
        is Failed -> updatedVocabulary
        else -> null
    }


}