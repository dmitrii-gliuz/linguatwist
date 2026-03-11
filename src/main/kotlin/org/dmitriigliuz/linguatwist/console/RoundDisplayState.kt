package org.dmitriigliuz.linguatwist.console

import org.dmitriigliuz.linguatwist.model.GameResult
import org.dmitriigliuz.linguatwist.model.GameState
import org.dmitriigliuz.linguatwist.model.Round


internal data class RoundDisplayState(
    val round: Round,
    val score: Int,
    val completionPercentage: Int,
    val totalMistakesMade: Int,
    val feedbackMessage: String,
    val showPrompt: Boolean = true,
)

internal fun GameState.InProgress.toRoundDisplayState(
    completionPercentage: Int,
    feedbackMessage: String
) = RoundDisplayState(
    round = round,
    score = gameScore,
    completionPercentage = completionPercentage,
    totalMistakesMade = mistakesCount,
    feedbackMessage = feedbackMessage,
)

internal fun GameResult.toDisplayState(
    feedbackMessage: String
) = RoundDisplayState(
    round = lastRound,
    score = score,
    completionPercentage = completionPercentage,
    totalMistakesMade = mistakesCount,
    feedbackMessage = feedbackMessage,
    showPrompt = false
)
