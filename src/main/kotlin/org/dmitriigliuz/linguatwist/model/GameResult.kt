package org.dmitriigliuz.linguatwist.model

data class GameResult(
    val isFailed: Boolean,
    val score: Int,
    val percentage: Int,
) {
    val verbalScore = VerbalScore.entries.first { it.percentage <= percentage }
}
