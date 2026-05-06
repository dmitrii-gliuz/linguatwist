package org.dmitriigliuz.linguatwist.game

import org.dmitriigliuz.linguatwist.i18n.Messages
import org.dmitriigliuz.linguatwist.model.GameResult
import org.dmitriigliuz.linguatwist.model.GameState
import org.dmitriigliuz.linguatwist.model.Round
import org.dmitriigliuz.linguatwist.model.RoundResult
import org.dmitriigliuz.linguatwist.model.VerbalScore
import org.dmitriigliuz.linguatwist.model.Vocabulary


data class TurnOutcome(
    val roundResult: RoundResult,
    val newState: GameState
)


class GameEngine(
    private val totalRounds: Int,
    private val config: GameConfig,
) {
    val allowedMistakes = maxOf(1, totalRounds / 3)

    fun startNewGame(vocabulary: Vocabulary)= advanceToNewRound(GameState.Initial(vocabulary))

    fun processAnswer(indexes: Pair<Int, Int>, state: GameState.InProgress): TurnOutcome {
        val roundResult = state.round.choosePair(indexes)
        val newState = when (roundResult) {
            is RoundResult.Correct -> processRightAnswer(roundResult, state, indexes.first)
            is RoundResult.Wrong -> processWrongAnswer(roundResult, state, indexes)
            is RoundResult.Invalid -> state.copy(error = roundResult.error)
        }
        return TurnOutcome(roundResult, newState)
    }

    private fun advanceToNewRound(state: GameState) = when (state) {
        is GameState.Initial -> {
            val (newGroup, updatedVocabulary) = state.vocabulary.takeNextGroup(config.pairsPerRound)
            val newRound = Round(newGroup, 1)
            GameState.InProgress(
                round = newRound,
                gameScore = 0,
                mistakesCount = 0,
                vocabulary =  updatedVocabulary,
                totalPairs = totalRounds * config.pairsPerRound)
        }
        is GameState.InProgress -> {
            val (newGroup, updatedVocabulary) = state.vocabulary.takeNextGroup(config.pairsPerRound)
            val newRound = Round(newGroup, state.round.number + 1)
            state.copy(
                round = newRound,
                vocabulary = updatedVocabulary,
            )
        }
        else -> throw IllegalStateException("${Messages.state_error_unexpected}: $state")
    }

    private fun processRightAnswer(
        result: RoundResult.Correct,
        state: GameState.InProgress,
        leftIndex: Int
    ): GameState {
        val round = result.updatedRound
        val gameScore = state.gameScore + 1
        val updatedEntry = round.pairAtLeftColumn(leftIndex)
        val updatedScores = mapOf(updatedEntry.key to updatedEntry.entryScore)
        val updatedVocabulary = state.vocabulary.withUpdatedScores(updatedScores)
        val updatedState = state.copy(
            round = round,
            gameScore = gameScore,
            error = null,
            vocabulary = updatedVocabulary
        )
        return if (round.isFinished && round.number == totalRounds) {
            val gameResult = createResult(false, updatedState)
            GameState.Finished(gameResult, updatedVocabulary)
        } else if (round.isFinished) {
            advanceToNewRound(updatedState)
        } else updatedState
    }

    private fun processWrongAnswer(
        result: RoundResult.Wrong,
        state: GameState.InProgress,
        indexes: Pair<Int, Int>
    ): GameState {
        val round = result.updatedRound
        val updatedLeftEntry = round.pairAtLeftColumn(indexes.first)
        val updatedRightEntry = round.pairAtRightColumn(indexes.second)
        val gameScore = state.gameScore - 1
        val mistakesCount = state.mistakesCount + 1
        val updatedScores = mapOf(
            updatedLeftEntry.key to updatedLeftEntry.entryScore,
            updatedRightEntry.key to updatedRightEntry.entryScore
        )
        val updatedVocabulary = state.vocabulary.withUpdatedScores(updatedScores)
        val updatedState = state.copy(
            round = round,
            gameScore = gameScore,
            mistakesCount = mistakesCount,
            error = null,
            vocabulary = updatedVocabulary
        )
        return if (mistakesCount > allowedMistakes) {
            val result = createResult(true, updatedState)
            GameState.Failed(result, updatedVocabulary)
        } else updatedState
    }

    private fun createResult(failed: Boolean, state: GameState.InProgress): GameResult = with(state) {
        val performancePercentage = (
                if (failed) 50 * state.totalGuessedAmount / totalPairs
                else 100 - 50 * mistakesCount / allowedMistakes
                ).coerceIn(0, 100)

        val verbalScore = VerbalScore.entries.first { it.percentage <= performancePercentage }

        return GameResult(
            completionPercentage,
            lastRound = round,
            isFailed = failed,
            score = gameScore,
            totalGuessedAmount = totalGuessedAmount,
            totalPairsAmount = totalPairs,
            mistakesCount = mistakesCount,
            allowedMistakes = allowedMistakes,
            verbalScore = verbalScore
        )
    }
}