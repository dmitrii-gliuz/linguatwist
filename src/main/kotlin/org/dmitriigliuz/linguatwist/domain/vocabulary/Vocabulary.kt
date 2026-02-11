package org.dmitriigliuz.linguatwist.domain.vocabulary

import org.dmitriigliuz.linguatwist.domain.model.LexicalEntry

data class Vocabulary(
    val entries: Set<LexicalEntry>,
    private val entriesPerRound: Int,
    private val remainingPool: List<LexicalEntry> = entries.shuffled().sortedBy { it.score }
) {
    val size = entries.size

    init {
//      TODO Fix the access to locale and use a string value from there
        require(hasEnoughDistinctEntries()) { "NotAllWordsAreUnique" }
    }

    private fun refillPool(seen: Set<LexicalEntry>, newGroup: List<LexicalEntry>) = entries
        .shuffled()
        .sortedBy { it.score }
        .filter { !seen.contains(it) && !newGroup.contains(it) }
        .map { it.withoutGuessed() }

    private fun hasEnoughDistinctEntries(): Boolean {
        val uniqueEntries = mutableSetOf<LexicalEntry>()
        for (entry in entries) {
            if (!uniqueEntries.any { it.overlaps(entry) }) uniqueEntries.add(entry)
            if (uniqueEntries.size >= entriesPerRound) return true
        }
        return false
    }

    fun takeNextGroup(): Pair<List<LexicalEntry>, Vocabulary> {
        var currentPool = remainingPool.toMutableList()
        val newGroup: List<LexicalEntry> = buildList {
            val seen = mutableSetOf<LexicalEntry>()

            while (this.size < entriesPerRound) {
                if (currentPool.size < entriesPerRound) currentPool = refillPool(seen, this).toMutableList()
                val entry = currentPool.removeFirst()
                if (this.any { it.overlaps(entry) }) seen.add(entry) else this.add(entry)
            }
        }
        return Pair(newGroup, copy(remainingPool = currentPool.toList()))
    }
}