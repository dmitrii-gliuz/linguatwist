package org.dmitriigliuz.linguatwist.model

import kotlin.time.Duration


data class GameResult(
    val isFailed: Boolean,
    val score: Int,
    val percentage: Int,
    val duration: Duration
) {
    val verbalScore = VerbalScore.entries.first { it.percentage <= percentage }
}
