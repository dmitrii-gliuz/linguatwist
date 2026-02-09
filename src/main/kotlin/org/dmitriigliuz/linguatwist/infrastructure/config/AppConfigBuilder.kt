package org.dmitriigliuz.linguatwist.infrastructure.config

import org.dmitriigliuz.linguatwist.AppConfig

class AppConfigBuilder {
    private var spreadsheetPath: String?  = null
    private var languageTag: String? = null
    private var maxRoundsNumber: Int? = null
    private var defaultRoundsNumber: Int? = null
    private var pairsPerRound: Int? = null
    private var maxMistakes: Int? = null
    private var useEmojis: Boolean? = null
    private val additionalProperties = mutableMapOf<String, String>()

    companion object {
        fun create(): AppConfigBuilder = AppConfigBuilder()

        fun createDefault(): AppConfigBuilder = AppConfigBuilder().apply {
            spreadsheetPath = "spreadsheet.xlsx"
            languageTag = "en"
            maxRoundsNumber = 10
            defaultRoundsNumber = 5
            pairsPerRound = 5
            maxMistakes = 3
            useEmojis = true
        }

        fun from(config: AppConfig): AppConfigBuilder = AppConfigBuilder().apply {
            spreadsheetPath = config.spreadsheetPath
            languageTag = config.languageTag
            maxRoundsNumber = config.maxRoundsNumber
            defaultRoundsNumber = config.defaultRoundsNumber
            pairsPerRound = config.pairsPerRound
            maxMistakes = config.maxMistakes
            useEmojis = config.useEmojis
            additionalProperties.putAll(config.additionalProperties)
        }

    }

}