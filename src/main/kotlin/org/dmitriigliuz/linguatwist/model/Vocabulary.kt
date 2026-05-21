package org.dmitriigliuz.linguatwist.model

data class Vocabulary(
    val entriesMap: Map<LexemeKey, LexicalEntry>,
    private val remainingKeysPool: List<LexemeKey> = entriesMap.keys.shuffled().sortedBy { entriesMap[it]?.entryScore }
) {
    val size = entriesMap.size

    private fun findIndependentKeysSet(
        requiredSize: Int,
        candidates: List<LexemeKey>,
        currentIndex: Int = 0,
        collectedGroup: MutableList<LexemeKey> = mutableListOf(),
        seenOriginals: MutableSet<String> = mutableSetOf(),
        seenTranslations: MutableSet<String> = mutableSetOf()
    ): List<LexemeKey>? {
        val needed = requiredSize - collectedGroup.size
        if (needed == 0) return collectedGroup
        val restCandidatesAmount = candidates.size - currentIndex
        if (needed > restCandidatesAmount) return null
        for (i in currentIndex until candidates.size) {
            val currentKey = candidates[i]
            val (sourceText, translation) = currentKey
            if (sourceText in seenOriginals || translation in seenTranslations) continue
            collectedGroup.add(currentKey)
            seenOriginals.add(sourceText)
            seenTranslations.add(translation)
            val result = findIndependentKeysSet(
                requiredSize,
                candidates,
                i + 1,
                collectedGroup,
                seenOriginals,
                seenTranslations
            )
            if (result == null) {
                collectedGroup.removeLast()
                seenOriginals.remove(sourceText)
                seenTranslations.remove(translation)
            } else return result
        }
        return null
    }

    private fun refillPool() = entriesMap.keys.shuffled().sortedBy { entriesMap[it]?.entryScore }

    fun hasEnoughDistinctEntries(groupSize: Int): Boolean {
        require(groupSize > 1) { "groupSize must be more than 1, got $groupSize" }
        return findIndependentKeysSet(groupSize, entriesMap.keys.toList()) != null
    }

    fun takeNextGroup(groupSize: Int): Pair<List<LexicalEntry>, Vocabulary> {
        var refilledKeysPool: List<LexemeKey>? = null
        val independentKeysSet = findIndependentKeysSet(groupSize, remainingKeysPool) ?: run {
            refilledKeysPool = refillPool()
            findIndependentKeysSet(groupSize, refilledKeysPool) ?: error(
                "Cannot find independent set of size $groupSize"
            )
        }

        val newPool = (refilledKeysPool ?: remainingKeysPool) - independentKeysSet.toSet()
        return Pair(independentKeysSet.map { entriesMap.getValue(it).withoutGuessed() }, copy(remainingKeysPool = newPool))
    }

    fun withUpdatedScores(updatedScores: Map<LexemeKey, Int>) = if (updatedScores.isEmpty()) this else {
        val updatedEntries = updatedScores
            .mapValues { (key, score) -> entriesMap.getValue(key).copy(entryScore = score) }
        copy(entriesMap = entriesMap + updatedEntries)
    }
}