package org.dmitriigliuz.linguatwist.domain.vocabulary

import org.dmitriigliuz.linguatwist.domain.model.LexicalEntry

data class WordPairProcessingResult(
    val lexicalEntries: List<LexicalEntry>,
    val duplicates: List<LexicalEntry>,
//    TODO Find the better name for this:
    val pairsWithSameWords: List<LexicalEntry>,
    val invalidRows: Int

)