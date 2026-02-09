package org.dmitriigliuz.linguatwist.infrastructure.config

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import org.dmitriigliuz.linguatwist.infrastructure.storage.FileUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.Properties

class ConfigLoader {
    companion object {
        private val CONFIG_FILENAMES = listOf(
            "linguatwist.properties",
            "application.properties",
            "config.properties"
        )

        private val CONFIG_SEARCH_PATHS = listOf(
            ".",                                                  // Current working directory
            "./config",                                           // Config subdirectory in current directory
            System.getProperty("user.home") + "/.linguatwist",    // User's home directory config
        )
    }

    val logger: Logger = LoggerFactory.getLogger(ConfigLoader::class.java)

    private fun findConfigFile(explicitPath: String?): File? =
        explicitPath?.let { FileUtils.requireFileReadable(it) }
            .also { if (it == null) logger.warn("Configuration file not found: $it") }

    fun parseCommandLineArgs(args: Array<String>) {
        val parser = ArgParser("linguatwist")
        val spreadsheet by parser.option(
            ArgType.String,
            shortName = "s",
            fullName = "spreadsheet",
            description = "Path to the vocabulary spreadsheet file"
        )

        val language by parser.option(
            ArgType.String,
            shortName = "l",
            fullName = "language",
            description = "Language code (en, ru, uk, etc.)"
        )


        val useEmojis by parser.option(
            ArgType.Boolean,
            shortName = "e",
            fullName = "emojis",
            description = "Enable or disable emoji symbols in the game output"
        ).default(true)

    }

    fun loadProperties(): Properties {
        val properties = Properties()
        val fileName = "linguatwist.properties"

        // Try to load properties from an external config file first
        val externalConfigFile = File("config/$fileName")
        if (externalConfigFile.exists()) {
            FileInputStream(externalConfigFile).use {properties.load(it) }
        } else {
            // Fallback to loading configuration from classpath resources if an external file is not found
            val resourceStream = this::class.java.classLoader.getResourceAsStream(fileName) ?: throw FileNotFoundException(
                "Configuration file not found: $fileName")
            logger.info("Loading configuration file from classpath: $fileName")
            resourceStream.use { properties.load(it) }
        }
        return properties
    }

    fun loadConfig(
        configFilePath: String? = null,
        commanndLineArgs: Array<String> = emptyArray()
    ): ConfigLoadResult {
        logger.debug("Loading configuration...")
        val warnnings = mutableListOf<String>()
        var source = ConfigSource.DEFAULT
        var loadedFrom: String? = null


    }

}