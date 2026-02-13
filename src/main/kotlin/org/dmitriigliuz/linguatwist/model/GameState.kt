package org.dmitriigliuz.linguatwist.model

sealed class GameState {
    data class InProgress(val round: Round, val totalRounds: Int, val score: Int, val mistakes: Int) : GameState()
    data class Finished(val result: GameResult) : GameState()
    data class Failed(val result: GameResult) : GameState()
}