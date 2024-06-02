package org.dmitriigliuz.learnnewwords

class Game(private val vocabulary: List<Flashcard>) {
    private val unusedWords = vocabulary.shuffled().toMutableList()

    fun getShuffledVocabulary() = vocabulary.shuffled().toMutableList()

    fun getFiveWords() = buildList {
        repeat (5) {
            if (unusedWords.isNotEmpty()) {
                add(unusedWords.removeLast())
            } else {
                unusedWords.addAll(getShuffledVocabulary())
            }
        }
    }
}