package org.dmitriigliuz.linguatwist.core

import java.time.Instant
import java.time.Duration

const val numberOfPairsInRound = 5
const val defaultNumberOfRounds = 10
const val recommendedVocabularySize = numberOfPairsInRound * defaultNumberOfRounds
const val maxNumberOfRounds = 100

enum class VerbalScore(val percentage: Int, val emoji: String, val description: String) {
    PERFECT(100, "🎉", "Perfect"),
    ALMOST_PERFECT(95, "👏", "AlmostPerfect"),
    EXCELLENT(90, "✨", "Excellent"),
    VERY_GOOD(85, "👍", "VeryGood"),
    GOOD(80, "😊", "Good"),
    ABOVE_AVERAGE(75, "🙂", "AboveAverage"),
    AVERAGE(70, "😐", "Average"),
    BELOW_AVERAGE(65, "😕", "BelowAverage"),
    POOR(60, "😟", "Poor"),
    VERY_POOR(55, "😢", "VeryPoor"),
    TRY_AGAIN(0, "😭", "TryAgain");
}

class Game(private val vocabulary: Vocabulary, private val numberOfRounds: Int = defaultNumberOfRounds) {
    init {
        if (numberOfRounds !in 1..maxNumberOfRounds) {
            throw IllegalArgumentException(localeString("NumberOfRoundsShouldBeInRange"))
        }
    }
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

    fun spentTime() = getDuration().run { String.format("%02d:%02d:%02d", toHours(), toMinutes() % 60, seconds % 60) }

    fun completionPercentage(): Int {
        val totalWords = numberOfRounds * numberOfPairsInRound
        val guessedWords = rounds.sumOf { it.guessedPairs }
        return guessedWords * 100 / totalWords
    }

    private fun rightAnswersPercentage() = rightAnswersAmount * 100 / answersAmount

    private fun verbalScore(percentage: Int) = VerbalScore.entries.first { it.percentage <= percentage }

    fun percentageAndVerbalScore() = rightAnswersPercentage().run { Pair(this, verbalScore(this)) }

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
        if (mistakes > 3) isFailed = true
        else if (currentRound.isFinished() && currentRoundNumber < numberOfRounds) changeRound()
    }

    fun choosePair(indexes: List<Int>) = currentRound.choosePair(indexes).also { processRoundOutcome(it.first) }

    fun isFinished() = isFailed || (currentRoundNumber == numberOfRounds && currentRound.isFinished())

    private fun createRound() = Round(vocabulary.takeGroupOfPairs(numberOfPairsInRound), currentRoundNumber)
}