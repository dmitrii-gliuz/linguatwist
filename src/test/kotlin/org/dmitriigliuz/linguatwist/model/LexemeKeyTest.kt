package org.dmitriigliuz.linguatwist.model

import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class LexemeKeyTest {
    private val sourceString = "source"
    private val translationString = "translation"

    @Test
    fun `trims source text`() {
        val key = LexemeKey.of("  $sourceString  ", translationString)
        assertEquals(sourceString, key.normalizedSourceText)
    }

    @Test
    fun `trims translation`() {
        val key = LexemeKey.of(sourceString, "  $translationString   ")
        assertEquals(translationString, key.normalizedTranslation)
    }

    @Test
    fun `keeps inner whitespace unchanged`() {
        val key = LexemeKey.of("  ice  cream  ", " hot  chocolate")
        assertEquals("ice  cream", key.normalizedSourceText)
        assertEquals("hot  chocolate", key.normalizedTranslation)
    }

    @Test
    fun `lowercases source text`() {
        val key1 = LexemeKey.of("SOURCE", translationString)
        val key2 = LexemeKey.of("Source", translationString)
        val key3 = LexemeKey.of("SoUrCe", translationString)
        val key4 = LexemeKey.of("Source Text", translationString)
        assertEquals(sourceString, key1.normalizedSourceText)
        assertEquals(sourceString, key2.normalizedSourceText)
        assertEquals(sourceString, key3.normalizedSourceText)
        assertEquals("source text", key4.normalizedSourceText)
    }

    @Test
    fun `lowercases translation`() {
        val key1 = LexemeKey.of(sourceString, "TRANSLATION")
        val key2 = LexemeKey.of(sourceString, "Translation")
        val key3 = LexemeKey.of(sourceString, "TrAnSlAtIoN")
        val key4 = LexemeKey.of(sourceString, "Translation Text")
        assertEquals(translationString, key1.normalizedTranslation)
        assertEquals(translationString, key2.normalizedTranslation)
        assertEquals(translationString, key3.normalizedTranslation)
        assertEquals("translation text", key4.normalizedTranslation)
    }

    @Test
    fun `normalizes equal pairs with different casing to equal keys`() {
        assertEquals(
            LexemeKey.of(" Source  ", "  TrAnSlaTiOn"),
            LexemeKey.of("  SOURCE ", "translation  ")
        )
    }

    @Test
    fun `keeps empty strings empty`() {
        val key = LexemeKey.of("", "")
        assertEquals("", key.normalizedSourceText)
        assertEquals("", key.normalizedTranslation)
    }

    @Test
    fun `trims whitespace-only strings to empty strings`() {
        val key = LexemeKey.of(" ", "    ")
        assertEquals("", key.normalizedSourceText)
        assertEquals("", key.normalizedTranslation)
    }

    @Test
    fun `lowercases cyrillic letters`() {
        val uppercaseHello = "ПРИВЕТ"
        val lowercaseHello = "привет"
        val uppercaseAlphabet = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ"
        val lowercaseAlphabet = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя"
        val key = LexemeKey.of(uppercaseHello, uppercaseAlphabet)
        assertEquals(lowercaseHello, key.normalizedSourceText)
        assertEquals(lowercaseAlphabet, key.normalizedTranslation)
    }

    @Test
    fun `lowercases latin letters with diacritics`() {
        val uppercase = "ÄÖÜẞÉÈÊÇÑŁŚŹŻĀČŘŠŽÞÐ"
        val lowercase = "äöüßéèêçñłśźżāčřšžþð"
        val keyUppercaseSource = LexemeKey.of(uppercase, translationString)
        val keyLowercaseSource = LexemeKey.of(lowercase, translationString)
        val keyUppercaseTranslation = LexemeKey.of(sourceString, uppercase)
        val keyLowercaseTranslation = LexemeKey.of(sourceString, lowercase)
        assertEquals(lowercase, keyUppercaseSource.normalizedSourceText)
        assertEquals(lowercase, keyLowercaseSource.normalizedSourceText)
        assertEquals(lowercase, keyUppercaseTranslation.normalizedTranslation)
        assertEquals(lowercase, keyLowercaseTranslation.normalizedTranslation)
    }

    @Test
    fun `keeps punctuation unchanged`() {
        val punctuationString = ".,:;!?'\"`´‘’“”„«»()[]{}<>/\\\\|@#№\$€£¥%‰&*+=~^_…·•-‐‑‒–—―"
        val key = LexemeKey.of(punctuationString, punctuationString)
        assertEquals(punctuationString, key.normalizedSourceText)
        assertEquals(punctuationString, key.normalizedTranslation)
    }

    @Test
    fun `different source text creates different key`() {
        val key = LexemeKey.of(sourceString, translationString)
        val keyDifferentSource = LexemeKey.of("another source string", translationString)
        assertNotEquals(key, keyDifferentSource)
    }

    @Test
    fun `different translation creates different key`() {
        val key = LexemeKey.of(sourceString, translationString)
        val keyDifferentTranslation = LexemeKey.of(sourceString, "another translation string")
        assertNotEquals(key, keyDifferentTranslation)
    }

    @Test
    fun `normalizes canonically equivalent unicode to equal keys`() {
        val composedEAcute = "\u00E9"
        val decomposedEAcute = "e\u0301"
        val keyDecomposed = LexemeKey.of(decomposedEAcute, decomposedEAcute)
        val keyComposed = LexemeKey.of(composedEAcute, composedEAcute)
        assertEquals(keyComposed, keyDecomposed)
    }

    /**
     * Validates that ASCII letters are normalized consistently regardless of the system's current locale settings.
     *
     * The test ensures that string normalization behaves identically under different locale contexts, such as
     * Turkish ("tr-TR") and US English ("en-US"), which have distinct behavior for specific characters (e.g., 'I' vs 'ı').
     *
     * Steps:
     * - Sets the locale to Turkish and creates a `LexemeKey` instance from an uppercase string.
     * - Switches the locale to English and creates another `LexemeKey` instance from the same string.
     * - Validates that the `LexemeKey` instances are equivalent and their normalized source texts match
     *   the expected lowercase representation.
     *
     * Restores the original locale after execution to avoid side effects on the environment.
     */
    @Test
    fun `normalizes ASCII letters independently of system locale`() {
        val originalLocale = Locale.getDefault()
        val lowercaseTestString = "input"
        val uppercaseTestString = "INPUT"
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))
            val onTurkish = LexemeKey.of(uppercaseTestString, translationString)
            Locale.setDefault(Locale.forLanguageTag("en-US"))
            val onEnglish = LexemeKey.of(uppercaseTestString, translationString)
            assertEquals(onTurkish, onEnglish)
            assertEquals(lowercaseTestString, onTurkish.normalizedSourceText)
            assertEquals(lowercaseTestString, onEnglish.normalizedSourceText)
        } finally {
            Locale.setDefault(originalLocale)
        }
    }
}