package org.dmitriigliuz.linguatwist.game

import org.dmitriigliuz.linguatwist.model.GameResult
import org.dmitriigliuz.linguatwist.model.GameState
import org.dmitriigliuz.linguatwist.model.Round
import org.dmitriigliuz.linguatwist.model.RoundResult
import org.dmitriigliuz.linguatwist.model.Vocabulary

class GameEngine(
    private val totalRounds: Int,
    private val config: GameConfig
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

    init {
//        TODO Use error messages from locale when it will be implemented
        require(totalRounds in 1..config.maxRounds) { "NumberOfRoundsShouldBeInRange" }
    }

    private fun advanceToNewRound(state: GameState) = when (state) {
        is GameState.Initial -> {
            val (newGroup, updatedVocabulary) = state.vocabulary.takeNextGroup()
            val newRound = Round(newGroup, 1)
            GameState.InProgress(newRound, 0, 0, updatedVocabulary)
        }
        is GameState.InProgress -> {
            val (newGroup, updatedVocabulary) = state.vocabulary.takeNextGroup()
            val newRound = Round(newGroup, state.round.number + 1)
            state.copy(round = newRound, vocabulary = updatedVocabulary)
        }
        else -> throw IllegalStateException("Unexpected state: $state")
    }

    fun startNewGame(vocabulary: Vocabulary): GameState.InProgress {
        require(vocabulary.size >= config.pairsPerRound) { "NotEnoughWordsToPlayTheGame" }
        return advanceToNewRound(GameState.Initial(vocabulary))
    }

    fun completionPercentage(state: GameState.InProgress) =
        state.totalGuessedAmount() * 100 / (totalRounds * config.pairsPerRound)

    private fun createResult(failed: Boolean, state: GameState.InProgress): GameResult {
        val totalGuessedAmount = state.totalGuessedAmount()
        val totalAnswersAmount = totalGuessedAmount + state.mistakesCount
        val percentage = totalAnswersAmount * 100 / totalGuessedAmount
        return GameResult(failed, state.score, percentage)
    }

    private fun processRightAnswer(result: RoundResult.Correct, state: GameState.InProgress): GameState {
        val round = result.updatedRound
        val score = state.score + 1
        val updatedState = state.copy(
            round = round,
            score = score,
            error = null
        )
        return if (round.isFinished && round.number == totalRounds) {
            val result = createResult(false, updatedState)
            GameState.Finished(result)
        } else if (round.isFinished) {
            advanceToNewRound(updatedState)
        } else updatedState
    }

    private fun processWrongAnswer(result: RoundResult.Wrong, state: GameState.InProgress): GameState {
        val score = state.score - 1
        val mistakesCount = state.mistakesCount + 1
        val updatedState = state.copy(
            round = result.updatedRound,
            score = score, mistakesCount = mistakesCount,
            error = null
        )
        return if (mistakesCount >= config.maxMistakes) {
            val result = createResult(true, updatedState)
            GameState.Failed(result)
        } else updatedState
    }

    fun processAnswer(indexes: Pair<Int, Int>, state: GameState.InProgress): GameState {
        return when (val roundResult = state.round.choosePair(indexes)) {
            is RoundResult.Correct -> processRightAnswer(roundResult, state)
            is RoundResult.Wrong -> processWrongAnswer(roundResult, state)
            is RoundResult.Invalid -> state.copy(error = roundResult.error)
        }
    }
}