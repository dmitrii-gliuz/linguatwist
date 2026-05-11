package org.dmitriigliuz.linguatwist.model

import java.text.Normalizer
import java.util.Locale

@ConsistentCopyVisibility
data class LexemeKey private constructor(val normalizedSourceText: String, val normalizedTranslation: String) {
    companion object {
        private fun normalize(text: String): String = text
            .trim()
            .lowercase(Locale.ROOT)
            .let { Normalizer.normalize(it, Normalizer.Form.NFC) }

        fun of(sourceText: String, translation: String) =
            LexemeKey(normalize(sourceText), normalize(translation))
    }
}