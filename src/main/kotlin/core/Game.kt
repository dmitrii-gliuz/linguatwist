package org.dmitriigliuz.linguatwist.core

import java.time.Instant
import java.time.Duration

const val numberOfWordsInRound = 5
const val defaultNumberOfRounds = 5
const val recommendedVocabularySize = numberOfWordsInRound * defaultNumberOfRounds

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
        if (numberOfRounds !in 1..100) {
            throw IllegalArgumentException(getLocaleMessage("NumberOfRoundsShouldBeInRange"))
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


    private fun getDuration(): Duration {
        val finishedAt = Instant.now()
        return Duration.between(startedAt, finishedAt)
    }

    fun getSpentTime(): String {
        val duration = getDuration()
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        val seconds = duration.seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    fun completionPercentage(): Int {
        val totalWords = numberOfRounds * numberOfWordsInRound
        val guessedWords = rounds.sumOf { it.guessedPairs }
        return guessedWords * 100 / totalWords
    }

    private fun rightAnswersPercentage() = rightAnswersAmount * 100 / answersAmount

    private fun verbalScore(percentage: Int) = VerbalScore.entries.first { it.percentage <= percentage }

    fun percentageAndVerbalScore(): Pair<Int, VerbalScore> {
        val percentage = rightAnswersPercentage()
        return Pair(percentage, verbalScore(percentage))
    }

    fun currentRound() = rounds[currentRoundNumber - 1]

    private fun changeRoundIfFinished() {
        if (currentRound().isFinished() && currentRoundNumber < numberOfRounds) {
            currentRoundNumber++
            rounds.add(createRound())
        }
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
        else changeRoundIfFinished()
    }

    fun choosePair(indexes: List<Int>): Pair<Boolean, List<String>> {
        val resultAndChosenWords = currentRound().choosePair(indexes)
        val (result, _) = resultAndChosenWords
        processRoundOutcome(result)
        return resultAndChosenWords
    }

    fun isFinished() = isFailed || (currentRoundNumber == numberOfRounds && currentRound().isFinished())

    private fun createRound() = Round(vocabulary.takeGroupOfPairs(numberOfWordsInRound), currentRoundNumber)
}