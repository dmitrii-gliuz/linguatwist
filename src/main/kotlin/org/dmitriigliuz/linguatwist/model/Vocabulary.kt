package org.dmitriigliuz.linguatwist.model

data class Vocabulary(
    val entries: Set<LexicalEntry>,
    private val remainingPool: List<LexicalEntry> = entries.shuffled().sortedBy { it.entryScore }
) {
    val size = entries.size

    private fun findIndependentSet(
        requiredSize: Int,
        candidates: List<LexicalEntry>,
        currentGroup: List<LexicalEntry> = emptyList()
    ): List<LexicalEntry>? {
        if (currentGroup.size == requiredSize) return currentGroup
        if (candidates.isEmpty() || requiredSize > candidates.size) return null
        for (currentEntry in candidates) {
            val relevantCandidates = (candidates - currentEntry).filter { !it.overlaps(currentEntry) }
            val nextSize = requiredSize - 1
            val newGroup = currentGroup + currentEntry
            return findIndependentSet(nextSize, relevantCandidates, newGroup) ?: continue
        }
        return null
    }

    private fun refillPool() = entries
        .shuffled()
        .sortedBy { it.entryScore }
        .map { it.withoutGuessed() }

    fun hasEnoughDistinctEntries(groupSize: Int) =
        findIndependentSet(groupSize, entries.toList()) != null

    fun takeNextGroup(groupSize: Int): Pair<List<LexicalEntry>, Vocabulary> {
        var refilledPool: List<LexicalEntry>? = null
        val independentSet = findIndependentSet(groupSize, remainingPool) ?: run {
            refilledPool = refillPool()
            findIndependentSet(groupSize, refilledPool) ?: error(
                "Cannot find independent set of size $groupSize"
            )
        }

        val newPool = (refilledPool ?: remainingPool) - independentSet.toSet()
        return Pair(independentSet, copy(remainingPool = newPool))
    }

    fun withUpdatedScores(updatedScores: Map<LexicalEntry, Int>) = copy(
        entries = entries.mapTo(HashSet(entries.size)) { entry ->
            updatedScores[entry]?.let { entry.copy(entryScore = it) } ?: entry
        }
    )
}