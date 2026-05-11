package org.dmitriigliuz.linguatwist.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class LexemeKeyTest {
    @Test
    fun `trims spaces for source text`() {
        val key = LexemeKey.of("  source  ", "translation")
        assertEquals("source", key.normalizedSourceText)
    }

    @Test
    fun `trims spaces for translation`() {
        val key = LexemeKey.of("source", "  translation   ")
        assertEquals("translation", key.normalizedTranslation)
    }

    @Test
    fun `spaces between words in normalized text are not folded`() {
        assertNotEquals(
            LexemeKey.of("  ice  cream  ", " hot  chocolate"),
            LexemeKey.of("ice cream", "hot chocolate")
        )
    }

    @Test
    fun `transforms source text to lowercase`() {
        assertEquals(
            LexemeKey.of("source", "translation"),
            LexemeKey.of("SOURCE", "translation")
        )
    }

    @Test
    fun `transforms translation to lowercase`() {
        assertEquals(
            LexemeKey.of("source", "TRANSLATION"),
            LexemeKey.of("source", "translation")
        )
    }

    @Test
    fun `same pairs with different casing and spaces are equal`() {
        assertEquals(
            LexemeKey.of(" source  ", "  TrAnSlaTiOn"),
            LexemeKey.of("SOURCE", "translation  ")
        )
    }

    @Test
    fun `empty strings are still empty after normalization`() {
        assertEquals(
            LexemeKey.of("", ""),
            LexemeKey.of("", "")
        )
    }

    @Test
    fun `strings with only space become empty after normalization`() {
        assertEquals(
            LexemeKey.of("", ""),
            LexemeKey.of("  ", " ")
        )
    }

    @Test
    fun `transforms cyrillic letters to lowercase correctly`() {
        assertEquals(
            LexemeKey.of("привет", "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ"),
            LexemeKey.of("ПРИВЕТ", "абвгдеёжзийклмнопрстуфхцчшщъыьэюя")
        )
    }

    @Test
    fun `transform extended european alphabet to lowercase correctly`() {
        assertEquals(
            LexemeKey.of("ÀÁÂÃÄÅĀĂĄÆÇĆĈĊČÐĎĐÈÉÊËĒĔĖĘĚĜĞĠĢĤĦÌÍÎÏĨĪĬĮĲĴĶĹĻĽĿŁÑŃŅŇŊÒÓÔÕÖØŌŎŐŒŔŖŘŚŜŞŠȘẞŦŢŤȚÙÚÛÜŨŪŬŮŰŲŴÝŶŸŹŻŽÞ", "àáâãäåāăąæçćĉċčðďđèéêëēĕėęěĝğġģĥħìíîïĩīĭįĳĵķĺļľŀłñńņňŋòóôõöøōŏőœŕŗřśŝşšșßŧţťțùúûüũūŭůűųŵýŷÿźżžþ"),
            LexemeKey.of("àáâãäåāăąæçćĉċčðďđèéêëēĕėęěĝğġģĥħìíîïĩīĭįĳĵķĺļľŀłñńņňŋòóôõöøōŏőœŕŗřśŝşšșßŧţťțùúûüũūŭůűųŵýŷÿźżžþ", "ÀÁÂÃÄÅĀĂĄÆÇĆĈĊČÐĎĐÈÉÊËĒĔĖĘĚĜĞĠĢĤĦÌÍÎÏĨĪĬĮĲĴĶĹĻĽĿŁÑŃŅŇŊÒÓÔÕÖØŌŎŐŒŔŖŘŚŜŞŠȘẞŦŢŤȚÙÚÛÜŨŪŬŮŰŲŴÝŶŸŹŻŽÞ")
        )
    }

    @Test
    fun `punctuation is same after normalization`() {
        assertEquals(
            LexemeKey.of(".,:;!?'\"`´‘’“”„«»()[]{}<>/\\\\|@#№\$€£¥%‰&*+=~^_…·•-‐‑‒–—―", ".,:;!?'\"`´‘’“”„«»()[]{}<>/\\\\|@#№\$€£¥%‰&*+=~^_…·•-‐‑‒–—―"),
            LexemeKey.of(".,:;!?'\"`´‘’“”„«»()[]{}<>/\\\\|@#№\$€£¥%‰&*+=~^_…·•-‐‑‒–—―", ".,:;!?'\"`´‘’“”„«»()[]{}<>/\\\\|@#№\$€£¥%‰&*+=~^_…·•-‐‑‒–—―")
        )
    }

}