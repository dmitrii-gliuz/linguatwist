package org.dmitriigliuz.linguatwist

import org.dmitriigliuz.linguatwist.storage.FileUtils

/**
 * Configuration class for the application. Provides various settings and parameters
 * that control the behavior and operation of the application.
 *
 * @property spreadsheetPath Path to the default vocabulary file.
 * @property maxFileSize Maximum allowed file size for input in bytes.
 * @property maxRoundsNumber Maximum number of rounds allowed in a game session.
 * @property defaultRoundsNumber Default number of rounds per game session.
 * @property pairsPerRound Number of word pairs shown in each game round.
 * @property maxMistakes Maximum number of mistakes allowed before the game ends.
 * @property recommendedVocabularySize Recommended minimum vocabulary size based on default game length.
 * @property languageTag Language code used in the application (e.g., "en", "ru", "uk").
 * @property useEmojis Indicates whether emoji symbols should be displayed in the game output.
 * @property appName The name of the application.
 * @property appVersion The current version of the application.
 */
data class AppConfig(
    //    === Files ===
    /**
     * Path to the default vocabulary file
     */
    val spreadsheetPath: String = "vocabulary.xlsx",

    /**
     * Maximum allowed size for input files in bytes (50MB)
     */
    val maxFileSize: Long = 50 * 1024 * 1024,

    //    === Game ===
    /**
     * Maximum number of rounds allowed in a game session
     */
    val maxRoundsNumber: Int = 100,

    /**
     * Default number of rounds per game session
     */
    val defaultRoundsNumber: Int = 10,

    /**
     * Number of word pairs shown per round
     */
    val pairsPerRound: Int = 5,

    /**
     * Maximum number of mistakes allowed before game over
     */
    val maxMistakes: Int = 3,

    /**
     * Recommended minimum vocabulary size based on default game length
     */
    val recommendedVocabularySize: Int = pairsPerRound * defaultRoundsNumber,

    //    ==== Localization ===
    /**
     * Language code (e.g. "en", "ru", "uk")
     */
    val languageTag: String = "en",

    //    === UI ===
    /**
     * Whether to show emoji symbols in game output
     */
    val useEmojis: Boolean = true,

    //    === System ===
    /**
     * Display the name of the application
     */
    val appName: String = "LinguaTwist",

    /**
     * Current version number
     */
    val appVersion: String = "0.1",

    //    === Other ===
    /**
     * Additional properties to be used by the application
     */
    val additionalProperties: Map<String, String> = emptyMap()

    ) {
    /**
     * Checks if the file specified by the given path is in a supported format.
     *
     * @param filePath the path to the file whose format needs to be checked
     * @return true if the file format is supported, false otherwise
     */
    fun isSupportedFormat(filePath: String): Boolean {
        val extension = _root_ide_package_.org.dmitriigliuz.linguatwist.storage.FileUtils.getFileExtension(filePath)
        return SUPPORTED_FORMATS.contains(extension)
    }

    companion object {
        /**
         * A list of file formats that are supported by the application.
         *
         * This variable defines the valid file extensions that the application can process.
         * It is utilized in methods like `isSupportedFormat` to ensure that only files
         * with supported formats, such as "xlsx" and "xls", are accepted.
         */
        val SUPPORTED_FORMATS = listOf("xlsx", "xls")

        /**
         * Creates an instance of AppConfig with the provided parameters.
         *
         * @param spreadsheetPath the path to the spreadsheet file to be used
         * @param languageTag the language tag defining the application's language
         * @param useEmojis indicates whether emojis should be used in the application
         */
        fun create(spreadsheetPath: String, languageTag: String, useEmojis: Boolean) =
            AppConfig(spreadsheetPath, languageTag = languageTag, useEmojis = useEmojis)

        fun createQuickStart() = AppConfig()
    }

    fun getProperty(key: String): String? = additionalProperties[key]

    fun withProperty(key: String, value: String): AppConfig = copy(additionalProperties = additionalProperties + (key to value))

    fun validate(): List<String> = buildList {
        if (maxRoundsNumber <= 0) add("maxRounds must be positive")
        if (defaultRoundsNumber <= 0) add("defaultRounds must be positive")
        if (pairsPerRound <= 0) add("pairsPerRound must be positive")
        if (maxMistakes < 0) add("maxMistakes cannot be negative")
        if (spreadsheetPath.isBlank()) add("spreadsheetPath cannot be empty")
    }
}