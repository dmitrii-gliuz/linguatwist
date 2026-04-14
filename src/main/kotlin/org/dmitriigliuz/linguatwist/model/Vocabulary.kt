package org.dmitriigliuz.linguatwist.model

data class Vocabulary(
    val entries: Set<LexicalEntry>,
    private val remainingPool: List<LexicalEntry> = entries.shuffled().sortedBy { it.entryScore }
) {
    val size = entries.size

    private fun findIndependentSet(
        requiredSize: Int,
        candidates: List<LexicalEntry>,
        currentIndex: Int = 0,
        collectedGroup: MutableList<LexicalEntry> = mutableListOf(),
        seenOriginals: MutableSet<String> = mutableSetOf(),
        seenTranslations: MutableSet<String> = mutableSetOf()
    ): List<LexicalEntry>? {
        val needed = requiredSize - collectedGroup.size
        if (needed == 0) return collectedGroup
        val restCandidatesAmount = candidates.size - currentIndex
        if (needed > restCandidatesAmount) return null
        for (i in currentIndex until candidates.size) {
            val currentEntry = candidates[i]
            if (currentEntry.normalizedSourceText in seenOriginals || currentEntry.normalizedTranslation in seenTranslations) continue
            collectedGroup.add(currentEntry)
            seenOriginals.add(currentEntry.normalizedSourceText)
            seenTranslations.add(currentEntry.normalizedTranslation)
            val result = findIndependentSet(
                requiredSize,
                candidates,
                i + 1,
                collectedGroup,
                seenOriginals,
                seenTranslations
            )
            if (result == null) {
                collectedGroup.removeLast()
                seenOriginals.remove(currentEntry.normalizedSourceText)
                seenTranslations.remove(currentEntry.normalizedTranslation)
            } else return result
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

    fun withUpdatedScores(updatedScores: Map<LexicalEntry, Int>) = if (updatedScores.isEmpty()) this else copy(
        entries = entries.mapTo(HashSet(entries.size)) { entry ->
            updatedScores[entry]?.let { entry.copy(entryScore = it) } ?: entry
        }
    )
}