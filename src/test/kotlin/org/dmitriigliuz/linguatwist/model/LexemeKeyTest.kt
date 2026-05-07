package org.dmitriigliuz.linguatwist.model

import kotlin.test.Test
import kotlin.test.assertEquals

class LexemeKeyTest {
    @Test
    fun `normalized keys are equal`() {
        assertEquals(
            LexemeKey.of("a", "b"),
            LexemeKey.of("   A", "b  ")
        )
    }

    @Test
    fun `spaces inside normalized text are not folded`() {
        assertEquals(
            LexemeKey.of("  ice cream  ", " hot chocolate"),
            LexemeKey.of("ice cream", "hot chocolate")
        )
    }
}