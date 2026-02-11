package org.dmitriigliuz.linguatwist

import kotlin.system.exitProcess
import org.dmitriigliuz.linguatwist.legacy.presentation.cli.GameSession
import org.dmitriigliuz.linguatwist.legacy.application.context.AppContextFactory
import kotlinx.cli.*
import org.dmitriigliuz.linguatwist.legacy.infrastructure.config.ConfigLoader


fun main(args: Array<String>) {
    val parser = ArgParser("linguatwist")



    try {
        parser.parse(args)
        val configLoader = ConfigLoader()
        val config = configLoader.loadConfig(
            spreadsheetPath = spreadsheet,
            languageTag = language,
            useEmojis = if (useEmojis) true else null
        )

        val appContext = AppContextFactory.create(config).getOrElse { error ->
            println("❌ Failed to initialize application: ${error.message}")
            exitProcess(1)
        }
        GameSession(appContext).start()
    } catch (e: IllegalStateException) {
        println(e)
        exitProcess(1)
    }
    catch (e: Exception) {
        println("An unexpected error occurred: ${e.message}")
        exitProcess(1)
    }
}