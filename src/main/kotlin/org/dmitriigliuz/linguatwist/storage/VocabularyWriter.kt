package org.dmitriigliuz.linguatwist.storage

import org.dmitriigliuz.linguatwist.model.Vocabulary

interface VocabularyWriter {
    fun save(path: String, vocabulary: Vocabulary): Result<Unit>
}