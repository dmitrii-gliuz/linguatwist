package org.dmitriigliuz.linguatwist.game

import org.dmitriigliuz.linguatwist.legacy.Spreadsheet
import org.dmitriigliuz.linguatwist.legacy.application.context.AppContext
import org.dmitriigliuz.linguatwist.model.Round
import org.dmitriigliuz.linguatwist.model.VerbalScore
import org.dmitriigliuz.linguatwist.model.Vocabulary
import java.time.Duration
import java.time.Instant

class GameEngine(
    private val numberOfRounds: Int,
    private val context: AppContext,
) {
    private val spreadsheet = Spreadsheet(context)
    private val vocabulary: Vocabulary = spreadsheet.loadVocabulary()

    init {
        if (numberOfRounds !in 1..context.config.maxRoundsNumber) {
            throw IllegalArgumentException(context.getString("NumberOfRoundsShouldBeInRange"))
        }
    }

    val totalWords = numberOfRounds * context.config.pairsPerRound
    private var currentRoundNumber = 1
    private val rounds = mutableListOf(createRound())
    var mistakes = 0
    var score = 0
    var isFailed = false
    private var answersAmount = 0
    private var rightAnswersAmount = 0
    private val startedAt = Instant.now()

    val currentRound: Round
        get() = rounds[currentRoundNumber - 1]

    private fun getDuration(): Duration {
        val finishedAt = Instant.now()
        return Duration.between(startedAt, finishedAt)
    }

    fun spentTime() = getDuration().run {
        String.format("%02d:%02d:%02d", toHours(), toMinutes() % 60, seconds % 60)
    }

    fun guessedPairsAmount() = rounds.sumOf { it.guessedCount }

    fun completionPercentage() = guessedPairsAmount() * 100 / totalWords

    private fun rightAnswersPercentage() = rightAnswersAmount * 100 / answersAmount

    private fun verbalScore(percentage: Int) = VerbalScore.entries.first { it.percentage <= percentage }

    fun percentageAndVerbalScore() = rightAnswersPercentage().run {
        Pair(this, verbalScore(this))
    }

    private fun changeRound() {
        currentRoundNumber++
        rounds.add(createRound())
    }

    private fun processRoundOutcome(result: Boolean) {
        answersAmount++
        if (result) {
            rightAnswersAmount++
            score++
        } else {
            score--
            mistakes++
        }
        if (mistakes > context.config.maxMistakes) isFailed = true
        else if (currentRound.isFinished() && currentRoundNumber < numberOfRounds) changeRound()
    }

    fun choosePair(indexes: Pair<Int, Int>) = currentRound
        .choosePair(indexes)
        .also { processRoundOutcome(it.first) }

    fun isFinished() = isFailed || (currentRoundNumber == numberOfRounds && currentRound.isFinished())

    private fun createRound() = Round(
        vocabulary.takeNextGroup(context), currentRoundNumber
    )
}