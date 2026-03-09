package org.dmitriigliuz.linguatwist.model

data class GameResult(
    val completionPercentage: Int,
    val lastRound: Round,
    val isFailed: Boolean,
    val score: Int,
    val totalGuessedAmount: Int,
    val totalPairsAmount: Int,
    val mistakesCount: Int,
    val maxMistakes: Int,
    val verbalScore: VerbalScore,
)
