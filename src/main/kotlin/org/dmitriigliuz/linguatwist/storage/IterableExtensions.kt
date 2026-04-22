package org.dmitriigliuz.linguatwist.storage

import io.github.oshai.kotlinlogging.KotlinLogging
import org.dmitriigliuz.linguatwist.model.LexicalEntry

private val logger = KotlinLogging.logger { }

internal data class DeduplicationStats(
    var duplicates: Int = 0,
    var overlappedPairs: Int = 0
)

internal fun Iterable<LexicalEntry>.filterDuplicates(stats: DeduplicationStats): List<LexicalEntry> {
    val current = this.toList()
    val unique = current.distinct()
    val duplicates = current.size - unique.size
    stats.duplicates = duplicates
    logger.debug { "Removed $duplicates duplicates" }
    return unique
}


