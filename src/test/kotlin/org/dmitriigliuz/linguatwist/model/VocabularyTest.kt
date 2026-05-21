package org.dmitriigliuz.linguatwist.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VocabularyTest {
    private val threeEntriesVocabulary = vocabularyOf(
        LexicalEntry("a", "b"),
        LexicalEntry("c", "d"),
        LexicalEntry("e", "f")
    )

    private val fifteenEntries = (1..15).map { LexicalEntry("a$it", "b$it") }
    private val fifteenEntriesVocabulary = Vocabulary.fromEntries(fifteenEntries)

    private val conflictingEntriesVocabulary = vocabularyOf(
        LexicalEntry("a", "x"),
        LexicalEntry("a", "y"), // same source as a/x
        LexicalEntry("b", "x"), // same translation as a/x
        LexicalEntry("c", "z"),
        LexicalEntry("d", "w"),
    )

    private val emptyVocabulary = Vocabulary(emptyMap())

    @Test
    fun `size return number of entries used to create vocabulary`() {
        assertEquals(3, threeEntriesVocabulary.size)
    }

    @Test
    fun `size of empty vocabulary is 0`() = assertEquals(0, emptyVocabulary.size)

    @Test
    fun `hasEnoughDistinctEntries returns true if there are enough independent entries`() {
        assertTrue { threeEntriesVocabulary.hasEnoughDistinctEntries(2) }
    }

    @Test
    fun `hasEnoughDistinctEntries returns false if number of entries is less than groupSize`() {
        val entries = listOf(
            LexicalEntry("a", "b"),
            LexicalEntry("c", "d")
        ).associateBy { it.key }
        val vocabulary = Vocabulary(entries)
        assertFalse { vocabulary.hasEnoughDistinctEntries(3) }
    }

    @Test
    fun `hasEnoughDistinctEntries returns false if entries have same source text`() {
        val entries = listOf(
            LexicalEntry("a", "b"),
            LexicalEntry("a", "c")
        ).associateBy { it.key }
        val vocabulary = Vocabulary(entries)
        assertFalse { vocabulary.hasEnoughDistinctEntries(2) }
    }

    @Test
    fun `hasEnoughDistinctEntries returns false if entries have same translation`() {
        val entries = listOf(
            LexicalEntry("a", "b"),
            LexicalEntry("c", "b")
        ).associateBy { it.key }
        val vocabulary = Vocabulary(entries)
        assertFalse { vocabulary.hasEnoughDistinctEntries(2) }
    }

    @Test
    fun `hasEnoughDistinctEntries returns false if there are not enough independent entries`() {
        val entries = listOf(
            LexicalEntry("a", "x"),
            LexicalEntry("a", "y"),
            LexicalEntry("b", "x"),
            LexicalEntry("b", "y")
        ).associateBy { it.key }
        val vocabulary = Vocabulary(entries)
        assertFalse { vocabulary.hasEnoughDistinctEntries(3) }
    }

    @Test
    fun `hasEnoughDistinctEntries returns true if it is possible to skip conflicting entries`() {
        val entries = listOf(
            LexicalEntry("a", "b"),
            LexicalEntry("a", "c"),
            LexicalEntry("c", "b"),
            LexicalEntry("c", "d")
        )
        val vocabulary = Vocabulary(entries.associateBy { it.key })
        assertTrue { vocabulary.hasEnoughDistinctEntries(2) }
    }

    @Test
    fun `hasEnoughDistinctEntries returns true if first candidate blocks valid group`() {
        assertTrue { conflictingEntriesVocabulary.hasEnoughDistinctEntries(4) }
    }

    @Test
    fun `hasEnoughDistinctEntries throws if groupSize is less than 2`() {
        assertFailsWith<IllegalArgumentException> { threeEntriesVocabulary.hasEnoughDistinctEntries(0) }
        assertFailsWith<IllegalArgumentException> { threeEntriesVocabulary.hasEnoughDistinctEntries(1) }
    }

    @Test
    fun `hasEnoughDistinctEntries returns false if vocabulary is empty`() {
        val vocabulary = Vocabulary(emptyMap())
        assertFalse { vocabulary.hasEnoughDistinctEntries(2) }
    }

    @Test
    fun `normalized duplicates don't create independent entries`() {
        val entries = listOf(
            LexicalEntry("TEST", "ENTRY"),
            LexicalEntry("test", "Entry"),
            LexicalEntry("b", "x"),
            LexicalEntry("B", "y")
        ).associateBy { it.key }
        val vocabulary = Vocabulary(entries)
        assertFalse { vocabulary.hasEnoughDistinctEntries(3) }
    }

    @Test
    fun `takeNextGroup returns list of required size`() {
        for (groupSize in 2..15) {
            val group = fifteenEntriesVocabulary.takeNextGroup(groupSize).first
            assertEquals(groupSize, group.size, "Failed for size $groupSize")
        }
    }


    @Test
    fun `takeNextGroup returns independent entries when candidates conflicts`() {
        val group = conflictingEntriesVocabulary.takeNextGroup(3).first
        assertEquals(3, group.map { it.key.normalizedSourceText }.toSet().size)
        assertEquals(3, group.map { it.key.normalizedTranslation }.toSet().size)
    }

    @Test
    fun `takeNextGroup returns entries without guessed flag`() {
        val entries = fifteenEntries.map { it.withGuessed() }
        val vocabulary = Vocabulary.fromEntries(entries)
        val group = vocabulary.takeNextGroup(3).first
        assertTrue { group.none { it.isGuessed } }
    }

    @Test
    fun `takeNextGroup resets guessed flag when vocabulary has entries with mixed guessed state`() {
        val entries = fifteenEntries.mapIndexed { index, entry ->
            if (index % 2 == 0) entry.withGuessed() else entry
        }
        val vocabulary = Vocabulary.fromEntries(entries)
        val guessedKeys = vocabulary.entriesMap.filter { it.value.isGuessed }.map { it.key }
        val notGuessedKeys = vocabulary.entriesMap.filter { !it.value.isGuessed }.map { it.key }
        val group = vocabulary.takeNextGroup(15).first

        assertTrue { group.any { it.key in guessedKeys } }
        assertTrue { group.any { it.key in notGuessedKeys } }
        assertTrue { group.none { it.isGuessed } }
    }

    @Test
    fun `takeNextGroup returns group with same score which was in vocabulary before`() {
        val entries = fifteenEntries.mapIndexed { index, entry -> entry.copy(entryScore = index + 1) }
        val keysToScore = entries.associate { it.key to it.entryScore }
        val vocabulary = Vocabulary.fromEntries(entries)
        val group = vocabulary.takeNextGroup(3).first
        group.forEach {
            assertEquals( it.entryScore, keysToScore.getValue(it.key))
        }
    }

    private fun vocabularyOf(vararg entries: LexicalEntry) = Vocabulary(entries.associateBy { it.key })
}