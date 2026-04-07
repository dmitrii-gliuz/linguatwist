package org.dmitriigliuz.linguatwist.storage

import org.dmitriigliuz.linguatwist.model.Vocabulary

interface VocabularyLoader {
    fun load(path: String): Result<VocabularyLoadResult>
}

data class VocabularyLoadResult(
    val vocabulary: Vocabulary,
    val path: String,
    val duplicates: Int,
    val overlappedPairs: Int,
    val invalidRows: Int
)