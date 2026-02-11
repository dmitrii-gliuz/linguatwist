package org.dmitriigliuz.linguatwist.legacy.domain.vocabulary

import org.dmitriigliuz.linguatwist.model.LexicalEntry

data class WordPairProcessingResult(
    val lexicalEntries: List<LexicalEntry>,
    val duplicates: List<LexicalEntry>,
//    TODO Find the better name for this:
    val pairsWithSameWords: List<LexicalEntry>,
    val invalidRows: Int

)