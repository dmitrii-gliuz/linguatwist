package org.dmitriigliuz.linguatwist.game

data class GameConfig(
    val maxRounds: Int = 100,
    val defaultRounds: Int = 10,
    val pairsPerRound: Int = 5,
    val spreadsheetPath: String = "vocabulary.xlsx",
    val languageTag: String = "en",
    val useEmojis: Boolean = true,
    val appName: String = "LinguaTwist",
    val appVersion: String = "0.2"

)
