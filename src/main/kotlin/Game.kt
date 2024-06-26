package org.dmitriigliuz.learnnewwords

data class WordPair(val originalWord: String, val translation: String) {
    var guessed = false

    private fun getStrikethroughOrNot(text: String) = if (guessed) text.strikethrough() else text

    fun getOriginalWordStrikethroughOrNot() = getStrikethroughOrNot(originalWord)

    fun getTranslationStrikethroughOrNot() = getStrikethroughOrNot(translation)
}

class Round(private val pairs: List<WordPair>) {
    private val size = pairs.size
    private val shuffledIndexes = (0 until size).shuffled()
    private var guessedPairs = 0
    var mistakes = 0

    fun solved() = guessedPairs == size

    fun getTwoWordsByIndexes(indexes: List<Int>) = listOf(
        pairs[indexes[0]].originalWord,
        pairs[shuffledIndexes[indexes[1]]].translation
        )

    fun choosePair(indexes: List<Int>): Boolean {
        val pair = pairs[indexes[0]]
        if (pair.guessed) throw IllegalArgumentException("This pair is already guessed")
        return if (checkIndexes(indexes)) {
            pair.guessed = true
            guessedPairs++
            true
        } else {
            mistakes++
            false
        }

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

class Game(private val vocabulary: List<WordPair>) {
    val vocabularySize = vocabulary.size
    private val unusedWords = vocabulary.shuffled().toMutableList()
    private val currentPairs: MutableList<WordPair> = mutableListOf()
    private var groupSize = 4
    private val rounds = mutableListOf<Round>()

    fun start(numberOfRounds: Int) {
        rounds.clear()
        setNewPairsGroup()
    }

    fun isFinished(): Boolean {
        return true
    }

    private fun getShuffledVocabulary() = vocabulary.shuffled().toMutableList()

    private fun getWordsGroup() = buildList {
        repeat (groupSize) {
            if (unusedWords.isNotEmpty()) {
                add(unusedWords.removeLast())
            } else {
                unusedWords.addAll(getShuffledVocabulary())
            }
        }
    }

    fun setNewPairsGroup() {
        currentPairs.clear()
        currentPairs.addAll(getWordsGroup())
    }

    fun getTwoColumns() = Round(currentPairs)
}