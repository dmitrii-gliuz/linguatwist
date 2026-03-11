package org.dmitriigliuz.linguatwist.storage

import io.github.oshai.kotlinlogging.KotlinLogging
import org.dmitriigliuz.linguatwist.model.LexicalEntry

private val logger = KotlinLogging.logger { }

internal data class DeduplicationStats(
    var duplicates: Int = 0,
    var overlappedPairs: Int = 0
)

internal fun Sequence<LexicalEntry>.filterDuplicates(stats: DeduplicationStats) = sequence {
    val seenEntries = mutableSetOf<LexicalEntry>()
    val seenSourceTexts = mutableSetOf<String>()
    val seenTranslations = mutableSetOf<String>()

    for (entry in this@filterDuplicates) {
        if (entry in seenEntries) {
            stats.duplicates++
            logger.debug { "Found duplicate entry: sourceText='${entry.sourceText}', translation='${entry.translation}'" }
            continue
        }
        if (entry.normalizedSourceText in seenSourceTexts || entry.normalizedTranslation in seenTranslations) {
            stats.overlappedPairs += 2
            logger.debug { "Found overlapped pair: sourceText='${entry.sourceText}', translation='${entry.translation}'" }
        }

        seenEntries.add(entry)
        seenSourceTexts.add(entry.normalizedSourceText)
        seenTranslations.add(entry.normalizedTranslation)
        yield(entry)
    }
}