package org.dmitriigliuz.linguatwist.domain.vocabulary

import org.dmitriigliuz.linguatwist.domain.model.WordPair

data class WordPairProcessingResult(
    val wordPairs: List<WordPair>,
    val duplicates: List<WordPair>,
//    TODO Find the better name for this:
    val pairsWithSameWords: List<WordPair>,
    val invalidRows: Int

)