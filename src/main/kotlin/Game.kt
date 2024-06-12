package org.dmitriigliuz.learnnewwords


data class WordPair(val originalWord: String, val translation: String) {
    var guessed = false

    private fun getStrikethroughOrNot(text: String) = if (guessed) text.strikethrough() else text

    fun getOriginalWordStrikethroughOrNot() = getStrikethroughOrNot(originalWord)

    fun getTranslationStrikethroughOrNot() = getStrikethroughOrNot(translation)
}

class TwoColumns(private val pairs: List<WordPair>) {
    private val shuffledIndexes = (0..4).shuffled()

    fun getTwoWordsByIndexes(indexes: List<Int>) = listOf(
        pairs[indexes[0]].originalWord,
        pairs[shuffledIndexes[indexes[1]]].translation
        )

    fun choosePair(indexes: List<Int>): Boolean {
        return if (checkIndexes(indexes)) {
            val pairIndex = indexes[0]
            pairs[pairIndex].guessed = true
            true
        } else false
    }

    private fun checkIndexes(indexes: List<Int>) = indexes[0] == shuffledIndexes[indexes[1]]

    override fun toString() = buildString {
        val maxLength = pairs.maxOf { it.originalWord.length } + 3
        for (n in 1..5) {
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

class Game(vocabularyPath: String) {
    private val vocabulary = readExcelFile(vocabularyPath)
    private val unusedWords = vocabulary.shuffled().toMutableList()
    private val currentPairs: MutableList<WordPair> = mutableListOf()

    private fun getShuffledVocabulary() = vocabulary.shuffled().toMutableList()

    private fun getFiveWords() = buildList {
        repeat (5) {
            if (unusedWords.isNotEmpty()) {
                add(unusedWords.removeLast())
            } else {
                unusedWords.addAll(getShuffledVocabulary())
            }
        }
    }

    fun setNewFivePairs() {
        currentPairs.addAll(getFiveWords())
    }

    fun getTwoColumns() = TwoColumns(currentPairs)
}