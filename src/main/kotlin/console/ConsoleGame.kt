package org.dmitriigliuz.linguatwist.console

import org.dmitriigliuz.linguatwist.core.*
import kotlin.system.exitProcess

val ascii_logo = """
╭───────────────────────────────────────────────────────────────────────────────────────╮
│ ██      ██ ███    ██  ██████  ██    ██  █████  ████████ ██     ██ ██ ███████ ████████ │
│ ██      ██ ████   ██ ██       ██    ██ ██   ██    ██    ██     ██ ██ ██         ██    │
│ ██      ██ ██ ██  ██ ██   ███ ██    ██ ███████    ██    ██  █  ██ ██ ███████    ██    │
│ ██      ██ ██  ██ ██ ██    ██ ██    ██ ██   ██    ██    ██ ███ ██ ██      ██    ██    │
│ ███████ ██ ██   ████  ██████   ██████  ██   ██    ██     ███ ███  ██ ███████    ██    │
╰───────────────────────────────────────────────────────────────────────────────────────╯                                                                                    

""".trimMargin()

const val exitCommand = "exit"
const val skipCommand = "skip"
val maxStringLengthsInRounds = mutableListOf<Int>()
lateinit var spreadsheet: Spreadsheet



fun exitGame(messageText: String = "") {
    if (messageText.isNotEmpty()) println("⛔️ ${localeString("Error")}: $messageText.")
    println("👋 ${localeString("Bye")}")
    if (::spreadsheet.isInitialized) spreadsheet.close()
    exitProcess(0)
}

fun input(messageText: String): String {
    print("👉 $messageText ")
    val userInput = readln().trim()
    if (userInput == exitCommand) exitGame()
    return userInput
}

fun inputIndexes() = input("${localeString("EnterIndexesForMatchingWords")}:")
                .trim()
                .split("""\s+""".toRegex())
                .map { it.toInt() - 1 }

fun setNumberOfRounds(spreadsheet: Spreadsheet): Int {
    val defaultValue = spreadsheet.defaultNumberOfRounds
    return input("${localeString("EnterTheNumberOfRounds")} (${localeString("Default")}: $defaultValue):" )
        .toIntOrNull() ?: defaultValue
}

fun formattedResult(result: Boolean) = buildString {
    appendLine(if (result) "✅ ${localeString("Right")}!" else "❌ ${localeString("Wrong")}!")
    append(if (result) "🔼 +1" else "🔽 -1")
    appendLine(" ${localeString("Point")}.")
}

fun printGreetings() = println("""🚀 ${localeString("Welcome")}! ${localeString("LetsLearnWords")}!
📖 ${localeString("TheGameIsAboutMatchingWords")}
🔢 ${localeString("EnterTheIndexesOfWordsYouThinkMatch")}
📝 ${localeString("EnterExitToQuitAtAnyTime")}
""")


fun printNumberOfLoadedWords(vocabulary: Vocabulary, spreadsheet: Spreadsheet) = printbs {
    var warnings = false
    fun warningIf(condition: Boolean, message: String) {
        if (condition) {
            warnings = true
            appendLine("⚠️ ${localeString("Warning")}: $message.")
        }
    }
    appendLine("✅ ${localeString("TheNumberOfLoadedWords")}: ${vocabulary.size}")
    warningIf(vocabulary.size < recommendedVocabularySize, localeString("TheVocabularyIsSmall"))
    warningIf(spreadsheet.duplicates > 0,
        "${localeString("NumberOfDuplicates")}: ${spreadsheet.duplicates}")
    warningIf(spreadsheet.pairsWithSameWords > 0,
        "${localeString("NumberOfPairsWithSameWords")}: ${spreadsheet.pairsWithSameWords}")
    if (warnings) {
        appendLine("🔍 ${localeString("CheckTheVocabularyFile")}.")
        newLine()
    }
    appendLine("🔤 ${localeString("TheNumberOfRoundsToCoverAllWords")}: ${spreadsheet.numberOfRoundsToCoverAllWords}.")
}

fun printError(e: Exception? = null, text: String = "") {
    val message = text.ifEmpty { e?.message ?: localeString("UnexpectedError") }
    println("⚠️ ${localeString("Error")}: ${message}.\n   ${localeString("TryAgain")}!\n")
}

fun responseToPlayAgainAnswer(play: Boolean, scoresSaved: Boolean) = buildString {
    if (scoresSaved) appendLine("📝 ${localeString("ScoresSavedTo")} $spreadsheetPath.").newLine()
    append(if (play) "🔄 ${localeString("OKLetsPlayAgain")}" else "🎮 ${localeString("ThankYouForPlaying")}")
    appendLine("!")
}

fun startGame() {
    println("\uD83D\uDCDA ${localeString("LoadingVocabularyFile")}: $spreadsheetPath...")
    val vocabulary = try {
        spreadsheet = Spreadsheet(spreadsheetPath)
        spreadsheet.loadVocabulary()
    } catch (e: SpreadsheetException) {
        exitGame(e.message ?: localeString("UnexpectedError"))
        throw e
    }
    printNumberOfLoadedWords(vocabulary, spreadsheet)
    var play = true
    while (play) {
        val game = try {
            Game(vocabulary, setNumberOfRounds(spreadsheet))
        } catch (e: IllegalArgumentException) {
            printError(e)
            continue
        }
        game.run()
        play = input("${localeString("DoYouWantToPlayAgain")} (Y/n)").lowercase() in listOf("y", "yes", "")
        val scoresSaved = spreadsheet.writeScoresSafely(vocabulary)
        println(responseToPlayAgainAnswer(play, scoresSaved))
    }
    spreadsheet.close()
}

fun consoleGame() {
    println(ascii_logo)
    printGreetings()
    Menu.navigate()
}