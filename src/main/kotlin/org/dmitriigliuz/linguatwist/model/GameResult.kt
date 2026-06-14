package org.dmitriigliuz.linguatwist.model

import kotlin.time.Duration

data class GameResult(
    val completionPercentage: Int,
    val lastRound: Round,
    val totalRounds: Int,
    val isFailed: Boolean,
    val score: Int,
    val totalGuessedAmount: Int,
    val totalPairsAmount: Int,
    val mistakesCount: Int,
    val allowedMistakes: Int,
    val verbalScore: VerbalScore,
    val duration: Duration = Duration.ZERO
)
