package org.dmitriigliuz.linguatwist.console

import org.dmitriigliuz.linguatwist.model.LexicalEntry
import org.dmitriigliuz.linguatwist.model.Vocabulary
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class PendingSaveTest {
    @Test
    fun `failed save remains available for retry`() {
        val vocabulary = Vocabulary.fromEntries(
            setOf(
                LexicalEntry("word", "translation", entryScore = 0),
                LexicalEntry("anotherWord", "anotherTranslation", entryScore = 10)
            )
        )
        val pendingSave = PendingSave()
        val path = "path/to/file"
        val snapshot = VocabularySnapshot(path, vocabulary)
        pendingSave.update(snapshot)
        val preparedSaveResults = mutableListOf(
            Result.failure(Exception("Failed save")),
            Result.success(Unit)
        )
        val receivedSnapshots = mutableListOf<VocabularySnapshot>()
        fun saveOperation(currentSnapshot: VocabularySnapshot): Result<Unit> {
            receivedSnapshots.add(currentSnapshot)
            return preparedSaveResults.removeFirst()
        }

        val firstAttemptResult = pendingSave.attemptSave { saveOperation(it) }
        val secondAttemptResult = pendingSave.attemptSave { saveOperation(it) }

        assertEquals(listOf(snapshot, snapshot), receivedSnapshots)
        assertNotNull(firstAttemptResult)
        assertNotNull(secondAttemptResult)
        assertTrue(firstAttemptResult.isFailure)
        assertTrue(secondAttemptResult.isSuccess)
    }
}