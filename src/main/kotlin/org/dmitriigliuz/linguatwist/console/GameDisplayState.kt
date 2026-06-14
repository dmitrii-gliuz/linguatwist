package org.dmitriigliuz.linguatwist.console

import org.dmitriigliuz.linguatwist.model.GameResult
import org.dmitriigliuz.linguatwist.model.GameState
import org.dmitriigliuz.linguatwist.model.Round


enum class RoundDisplayPrompt {
    ENTER_PAIR, CONFIRM_EXIT, NONE
}

data class GameDisplayState(
    val round: Round,
    val feedback: Feedback,
    val totalRounds: Int,
    val score: Int,
    val completionPercentage: Int,
    val mistakesCount: Int,
    val allowedMistakes: Int,
    val promptType: RoundDisplayPrompt = RoundDisplayPrompt.ENTER_PAIR,
    val userInput: String = ""
) {
    companion object {
        val validInputRegex = Regex("""\d+ ?\d*""")
    }
    val selectedIndexes: Pair<Int?, Int?>
        get() {
            return if (promptType != RoundDisplayPrompt.ENTER_PAIR || userInput.isBlank() || !userInput.matches((validInputRegex))) {
                null to null
            } else {
                val selectedRowNumbers = userInput.split(" ")
                    .mapNotNull { it.toIntOrNull()?.let { number -> number - 1 } }

                val first = selectedRowNumbers.firstOrNull()
                val second = if (first != null && selectedRowNumbers.size > 1) selectedRowNumbers[1] else null
                first to second
            }
        }
    fun confirmExit() = copy(promptType = RoundDisplayPrompt.CONFIRM_EXIT)
}

internal fun GameState.InProgress.toRoundDisplayState(feedback: Feedback) = GameDisplayState(
    round = round,
    feedback = feedback,
    totalRounds = totalRounds,
    score = gameScore,
    completionPercentage = completionPercentage,
    mistakesCount = mistakesCount,
    allowedMistakes = allowedMistakes,
)

internal fun GameResult.toDisplayState(feedback: Feedback) = GameDisplayState(
    round = lastRound,
    feedback = feedback,
    totalRounds = totalRounds,
    score = score,
    completionPercentage = completionPercentage,
    mistakesCount = mistakesCount,
    allowedMistakes = allowedMistakes,
    promptType = RoundDisplayPrompt.NONE
)
