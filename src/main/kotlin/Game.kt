package org.dmitriigliuz.learnnewwords

const val numberOfWordsInRound = 5
const val defaultNumberOfRounds = 10
const val recommendedVocabularySize = numberOfWordsInRound * defaultNumberOfRounds

class Round(private val pairs: List<WordPair>, val number: Int) {
    private val size = pairs.size
    private val shuffledIndexes = (0 until size).shuffled()
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

    private fun checkIndexes(indexes: List<Int>): Boolean = indexes[0] == shuffledIndexes[indexes[1]]

    fun choosePair(indexes: List<Int>): Pair<Boolean, List<String>> {
        val pairInFirstColumn = pairs[indexes[0]]
        val pairInSecondColumn = getPairInSecondColumn(indexes)
        if (pairInFirstColumn.guessed || pairInSecondColumn.guessed) {
            throw IllegalArgumentException("This pair has already been guessed")
        }
        if (turns.contains(indexes)) {
            throw IllegalArgumentException("This pair of indexes has already been used")
        }
        turns.add(indexes)
        return Pair(if (checkIndexes(indexes)) {
            pairInFirstColumn.guessed = true
            guessedPairs++
            true
        } else {
            mistakes++
            false
        }, getTwoWordsByIndexes(indexes))
    }

    override fun toString() = buildString {
        val maxLength = pairs.maxOf { it.originalWord.length } + 3
        for (n in 1..size) {
            val index = n - 1
            val firstWord = pairs[index].originalWord
            val firstWordOutput = pairs[index].getOriginalWordStrikethroughOrNot()
            val secondWordOutput = pairs[shuffledIndexes[index]].getTranslationStrikethroughOrNot()
            val numberOfSpaces = maxLength - firstWord.length
            val spaces = " ".repeat(numberOfSpaces)
            append("$n. $firstWordOutput$spaces$n. $secondWordOutput")
            append("\n")
        }
    }
}

enum class VerbalScore(val percentage: Int, val emoji: String, val description: String) {
    PERFECT(100, "🎉", "Perfect"),
    ALMOST_PERFECT(95, "👏", "Almost Perfect"),
    EXCELLENT(90, "✨", "Excellent"),
    VERY_GOOD(85, "👍", "Very Good"),
    GOOD(80, "😊", "Good"),
    ABOVE_AVERAGE(75, "🙂", "Above Average"),
    AVERAGE(70, "😐", "Average"),
    BELOW_AVERAGE(65, "😕", "Below Average"),
    POOR(60, "😟", "Poor"),
    VERY_POOR(55, "😢", "Very Poor"),
    TRY_AGAIN(0, "😭", "Try again");
}

class Game(private val vocabulary: Vocabulary, private val roundsNumber: Int = defaultNumberOfRounds) {
    private val rounds = List(roundsNumber) { createRound(it + 1) }
    var currentRoundNumber = 1
    var mistakes = 0
    var score = 0
    var isFailed = false
    private var answersAmount = 0
    private var rightAnswersAmount = 0

    fun completionPercentage(): Int {
        val totalWords = roundsNumber * numberOfWordsInRound
        val guessedWords = rounds.sumOf { it.guessedPairs }
        return guessedWords * 100 / totalWords
    }

    private fun rightAnswersPercentage() = rightAnswersAmount * 100 / answersAmount

    private fun getVerbalScore(percentage: Int) = VerbalScore.entries.first { it.percentage <= percentage }

    fun getPercentageAndVerbalScore(): Pair<Int, VerbalScore> {
        val percentage = rightAnswersPercentage()
        return Pair(percentage, getVerbalScore(percentage))
    }

    fun currentRound() = rounds[currentRoundNumber - 1]

    fun previousRound() = rounds[currentRoundNumber - 2]

    private fun handleResult(result: Boolean) {
        answersAmount++
        if (result) {
            rightAnswersAmount++
            score++
        } else {
            score--
            mistakes++
        }
        if (mistakes == 3) isFailed = true
        if (currentRound().isFinished() && currentRoundNumber < roundsNumber) {
            currentRoundNumber++
        }
    }

    fun choosePair(indexes: List<Int>): Pair<Boolean, List<String>> {
        val round = currentRound()
        val resultAndChosenWords = round.choosePair(indexes)
        val (result, _) = resultAndChosenWords
        handleResult(result)
        return resultAndChosenWords
    }

    fun isFinished() = isFailed || (currentRoundNumber == roundsNumber && currentRound().isFinished())

    private fun createRound(number: Int) = Round(vocabulary.takeGroupOfPairs(numberOfWordsInRound), number)
}