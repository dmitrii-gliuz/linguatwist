package org.dmitriigliuz.linguatwist.game

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
    companion object {
        fun numberOfRoundsToCoverVocabulary(vocabulary: Vocabulary, config: GameConfig): Int {
            val leftoverPairs = vocabulary.size % config.pairsPerRound
            return vocabulary.size / config.pairsPerRound + if (leftoverPairs > 0) 1 else 0
        }


        fun defaultNumberOfRounds(vocabulary: Vocabulary, config: GameConfig) =
            if (vocabulary.size < config.maxRounds * config.pairsPerRound) {
                numberOfRoundsToCoverVocabulary(vocabulary, config)
            } else config.defaultRounds
    }

    val maxMistakes = totalRounds / 3
    val totalPairs = totalRounds * config.pairsPerRound


    init {
//        TODO Use error messages from locale when it will be implemented
        require(totalRounds in 1..config.maxRounds) { "NumberOfRoundsShouldBeInRange" }
    }

    fun startNewGame(vocabulary: Vocabulary): GameState.InProgress {
        // TODO Fix the access to locale and use a string value from there
        require(vocabulary.size >= config.pairsPerRound) { "NotEnoughWordsToPlayTheGame" }
        require(vocabulary.hasEnoughDistinctEntries(config.pairsPerRound)) { "NotAllWordsAreUnique" }
        return advanceToNewRound(GameState.Initial(vocabulary))
    }


    fun completionPercentage(state: GameState.InProgress) = state.totalGuessedAmount() * 100 / totalPairs

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
            GameState.InProgress(newRound, 0, 0, updatedVocabulary)
        }
        is GameState.InProgress -> {
            val (newGroup, updatedVocabulary) = state.vocabulary.takeNextGroup(config.pairsPerRound)
            val newRound = Round(newGroup, state.round.number + 1)
            state.copy(round = newRound, vocabulary = updatedVocabulary)
        }
        else -> throw IllegalStateException("Unexpected state: $state")
    }

    private fun createResult(failed: Boolean, state: GameState.InProgress): GameResult {
        val performancePercentage = if (failed) 50 * state.totalGuessedAmount() / totalPairs
        else 100 - 50 * state.mistakesCount / (if (maxMistakes > 0) maxMistakes else 1)

        val verbalScore = VerbalScore.entries.first { it.percentage <= performancePercentage }

        return GameResult(
            completionPercentage = completionPercentage(state),
            lastRound = state.round,
            isFailed = failed,
            score = state.gameScore,
            totalGuessedAmount = state.totalGuessedAmount(),
            totalPairsAmount = totalPairs,
            mistakesCount = state.mistakesCount,
            maxMistakes = maxMistakes,
            verbalScore = verbalScore
        )
    }

    private fun processRightAnswer(result: RoundResult.Correct, state: GameState.InProgress, leftIndex: Int): GameState {
        val round = result.updatedRound
        val gameScore = state.gameScore + 1
        val updatedEntry = round.pairAtLeftColumn(leftIndex)
        val updatedScores = state.updatedScores + (updatedEntry to updatedEntry.entryScore)
        val updatedState = state.copy(
            round = round,
            gameScore = gameScore,
            error = null,
            updatedScores = updatedScores
        )
        return if (round.isFinished && round.number == totalRounds) {
            val result = createResult(false, updatedState)
            val updatedVocabulary = state.vocabulary.withUpdatedScores(updatedScores)
            GameState.Finished(result, updatedVocabulary)
        } else if (round.isFinished) {
            advanceToNewRound(updatedState)
        } else updatedState
    }

    private fun processWrongAnswer(result: RoundResult.Wrong, state: GameState.InProgress, indexes: Pair<Int, Int>): GameState {
        val round = result.updatedRound
        val updatedLeftEntry = round.pairAtLeftColumn(indexes.first)
        val updatedRightEntry = round.pairAtRightColumn(indexes.second)
        val gameScore = state.gameScore - 1
        val mistakesCount = state.mistakesCount + 1
        val updatedScores = state.updatedScores +
                (updatedLeftEntry to updatedLeftEntry.entryScore) + (updatedRightEntry to updatedRightEntry.entryScore)
        val updatedState = state.copy(
            round = round,
            gameScore = gameScore,
            mistakesCount = mistakesCount,
            error = null,
            updatedScores = updatedScores
        )
        return if (mistakesCount >= maxMistakes) {
            val result = createResult(true, updatedState)
            val updatedVocabulary = state.vocabulary.withUpdatedScores(updatedScores)
            GameState.Failed(result, updatedVocabulary)
        } else updatedState
    }
}