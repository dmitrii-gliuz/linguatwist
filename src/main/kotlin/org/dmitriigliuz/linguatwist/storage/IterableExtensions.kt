package org.dmitriigliuz.linguatwist.storage

import io.github.oshai.kotlinlogging.KotlinLogging
import org.dmitriigliuz.linguatwist.model.LexicalEntry

private val logger = KotlinLogging.logger { }

internal data class DeduplicationStats(
    var duplicates: Int = 0,
    var overlappedPairs: Int = 0
)

internal fun Iterable<LexicalEntry>.filterDuplicates(stats: DeduplicationStats) = buildList {
    val seenEntries = mutableSetOf<LexicalEntry>()
    val seenSourceTexts = mutableSetOf<String>()
    val seenTranslations = mutableSetOf<String>()

    for (entry in this@filterDuplicates) {
        if (entry in seenEntries) {
            stats.duplicates++
            logger.trace { "Found duplicate entry while filtering vocabulary" }
            continue
        }
        if (entry.normalizedSourceText in seenSourceTexts || entry.normalizedTranslation in seenTranslations) {
            stats.overlappedPairs += 2
            logger.trace { "Found overlapped pair while filtering vocabulary" }
        }

        seenEntries.add(entry)
        seenSourceTexts.add(entry.normalizedSourceText)
        seenTranslations.add(entry.normalizedTranslation)
        add(entry)
    }
}


