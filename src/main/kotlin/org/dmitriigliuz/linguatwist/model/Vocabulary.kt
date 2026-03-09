package org.dmitriigliuz.linguatwist.model

data class Vocabulary(
    val entries: Set<LexicalEntry>,
    private val remainingPool: List<LexicalEntry> = entries.shuffled().sortedBy { it.score }
) {
    val size = entries.size

    private fun refillPool(seen: Set<LexicalEntry>, newGroup: List<LexicalEntry>) = entries
        .shuffled()
        .sortedBy { it.score }
        .filter { !seen.contains(it) && !newGroup.contains(it) }
        .map { it.withoutGuessed() }

    fun hasEnoughDistinctEntries(groupSize: Int): Boolean {
        val uniqueEntries = mutableSetOf<LexicalEntry>()
        for (entry in entries) {
            if (!uniqueEntries.any { it.overlaps(entry) }) uniqueEntries.add(entry)
            if (uniqueEntries.size >= groupSize) return true
        }
        return false
    }

    fun takeNextGroup(groupSize: Int): Pair<List<LexicalEntry>, Vocabulary> {
        var currentPool = remainingPool.toMutableList()
        val newGroup: List<LexicalEntry> = buildList {
            val seen = mutableSetOf<LexicalEntry>()

            while (this.size < groupSize) {
                if (currentPool.size < groupSize) currentPool = refillPool(seen, this).toMutableList()
                val entry = currentPool.removeFirst()
                if (this.any { it.overlaps(entry) }) seen.add(entry) else this.add(entry)
            }
        }
        return Pair(newGroup, copy(remainingPool = currentPool.toList()))
    }

    fun withUpdatedScores(updatedScores: Map<LexicalEntry, Int>) = copy(
        entries = entries.mapTo(HashSet(entries.size)) { entry ->
            updatedScores[entry]?.let { entry.copy(score = it) } ?: entry
        }
    )
}