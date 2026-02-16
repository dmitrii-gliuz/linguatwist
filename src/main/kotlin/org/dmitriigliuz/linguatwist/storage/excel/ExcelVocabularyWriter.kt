package org.dmitriigliuz.linguatwist.storage.excel

import org.dmitriigliuz.linguatwist.model.Vocabulary
import org.dmitriigliuz.linguatwist.storage.VocabularyWriter
import org.dmitriigliuz.linguatwist.storage.requireValidExcel
import java.io.File

class ExcelVocabularyWriter : VocabularyWriter {
    override fun save(path: String, vocabulary: Vocabulary): = runCatching {
        val oldFile = File(path).requireValidExcel()
        val voabularyMap = vocabulary.entries.associateBy { it.normalizedSourceText to it.normalizedTranslation }
        }
    }
}