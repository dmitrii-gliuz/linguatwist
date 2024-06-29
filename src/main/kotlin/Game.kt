package org.dmitriigliuz.learnnewwords

const val numberOfWordsInRound = 5

class Round(private val pairs: List<WordPair>) {
    private val size = pairs.size
    private val shuffledIndexes = (0 until size).shuffled()
    private var guessedPairs = 0
    var mistakes = 0


    fun isFinished() = guessedPairs == size

    private fun getTwoWordsByIndexes(indexes: List<Int>) = listOf(
        pairs[indexes[0]].originalWord,
        pairs[shuffledIndexes[indexes[1]]].translation
        )

    fun choosePair(indexes: List<Int>): Pair<Boolean, List<String>> {
        val pair = pairs[indexes[0]]
        if (pair.guessed) throw IllegalArgumentException("This pair is already guessed")
        return Pair(if (checkIndexes(indexes)) {
            pair.guessed = true
            guessedPairs++
            true
        } else {
            mistakes++
            false
        }, getTwoWordsByIndexes(indexes))

    }

    private fun checkIndexes(indexes: List<Int>) = indexes[0] == shuffledIndexes[indexes[1]]

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

class Game(private val vocabulary: Vocabulary, private val roundsNumber: Int = 2) {
    private val rounds = List(roundsNumber) { createRound() }
    var currentRoundNumber = 1
    var mistakes = 0

    fun mistakesInRound() = currentRound().mistakes

    fun currentRound() = rounds[currentRoundNumber - 1]

    fun choosePair(indexes: List<Int>): Pair<Boolean, List<String>> {
        val round = currentRound()
        val result = round.choosePair(indexes)
        if (!result.first) mistakes++
        if (round.isFinished() && currentRoundNumber < roundsNumber) {
            currentRoundNumber++
        }
        return result
    }

    fun isFinished() = currentRoundNumber == roundsNumber && currentRound().isFinished()

    private fun createRound() = Round(vocabulary.takeGroupOfPairs(numberOfWordsInRound))
}