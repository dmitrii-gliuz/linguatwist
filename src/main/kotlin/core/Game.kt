package org.dmitriigliuz.linguatwist.core

const val numberOfWordsInRound = 5
const val defaultNumberOfRounds = 5
const val recommendedVocabularySize = numberOfWordsInRound * defaultNumberOfRounds

class Round(val pairs: List<WordPair>, val number: Int) {
    val size = pairs.size
    val shuffledIndexes = (0 until size).shuffled()
    var guessedPairs = 0
    var mistakes = 0
    private val turns = mutableListOf<List<Int>>()


    fun isFinished() = guessedPairs == size

    private fun getPairInFirstColumn(indexes: List<Int>) = pairs[indexes[0]]

    private fun getPairInSecondColumn(indexes: List<Int>) = pairs[shuffledIndexes[indexes[1]]]

    private fun getTwoWordsByIndexes(indexes: List<Int>) = listOf(
        getPairInFirstColumn(indexes).originalWord,
        getPairInSecondColumn(indexes).translation
        )

    private fun validateIndexes(indexes: List<Int>) {
        if (indexes.size != 2) {
            throw IllegalArgumentException(getLocaleMessage("YouShouldEnterTwoIndexes"))
        }
        if (indexes.any { it !in 0 until size }) {
            throw IllegalArgumentException("${getLocaleMessage("IndexesShouldBeInTheRange")} $size")
        }
        if (turns.contains(indexes)) {
            throw IllegalArgumentException(getLocaleMessage("ThisPairOfIndexesHasAlreadyBeenUsed"))
        }
        val pairInFirstColumn = pairs[indexes[0]]
        val pairInSecondColumn = getPairInSecondColumn(indexes)
        if (pairInFirstColumn.guessed || pairInSecondColumn.guessed) {
            throw IllegalArgumentException(getLocaleMessage("ThisPairHasAlreadyBeenGuessed"))
        }
    }

    private fun areIndexesBelongToOnePair(indexes: List<Int>): Boolean = indexes[0] == shuffledIndexes[indexes[1]]

    fun choosePair(indexes: List<Int>): Pair<Boolean, List<String>> {
        validateIndexes(indexes)
        turns.add(indexes)
        val pairInFirstColumn = getPairInFirstColumn(indexes)
        val pairInSecondColumn = getPairInSecondColumn(indexes)
        return Pair(if (areIndexesBelongToOnePair(indexes)) {
            pairInFirstColumn.markAsGuessedAndIncreaseScore()
            guessedPairs++
            true
        } else {
            pairInFirstColumn.decreaseScore()
            pairInSecondColumn.decreaseScore()
            mistakes++
            false
        }, getTwoWordsByIndexes(indexes))
    }
}

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