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
    if (messageText.isNotEmpty()) println("${Emojis.CriticalError}${localeString("Error")}: $messageText.")
    println("${Emojis.Bye}${localeString("Bye")}")
    if (::spreadsheet.isInitialized) spreadsheet.close()
    exitProcess(0)
}

fun input(messageText: String): String {
    print("${Emojis.Enter}$messageText ")
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
    appendLine(if (result) "${Emojis.Right}${localeString("Right")}!" else "${Emojis.Wrong}${localeString("Wrong")}!")
    append(if (result) "${Emojis.Point}+1" else "${Emojis.PointMinus}-1")
    appendLine(" ${localeString("Point")}.")
}

fun printGreetings() = println(
    """${Emojis.Welcome}${localeString("Welcome")}! ${localeString("LetsLearnWords")}!
${Emojis.GameRules}${localeString("TheGameIsAboutMatchingWords")}
${Emojis.EnterIndexes}${localeString("EnterTheIndexesOfWordsYouThinkMatch")}
${Emojis.GameDescription}${localeString("EnterExitToQuitAtAnyTime")}
"""
)

fun warningString(warning: String) = "${Emojis.Warning}${localeString("Warning")}: $warning."


fun printNumberOfLoadedWords(vocabulary: Vocabulary, spreadsheet: Spreadsheet) = printbs {
    var warnings = false
    fun warningIf(condition: Boolean, message: String) {
        if (condition) {
            warnings = true
            appendLine(warningString(message))
        }
    }
    appendLine("${Emojis.Ready}${localeString("TheNumberOfLoadedWords")}: ${vocabulary.size}")
    warningIf(vocabulary.size < recommendedVocabularySize, localeString("TheVocabularyIsSmall"))
    warningIf(spreadsheet.duplicates > 0,
        "${localeString("NumberOfDuplicates")}: ${spreadsheet.duplicates}")
    warningIf(spreadsheet.pairsWithSameWords > 0,
        "${localeString("NumberOfPairsWithSameWords")}: ${spreadsheet.pairsWithSameWords}")
    warningIf(spreadsheet.incorrectRows > 0, "${localeString("IncorrectRows")}: ${spreadsheet.incorrectRows}")
    if (warnings) {
        appendLine("${Emojis.CheckVocabulary}${localeString("CheckTheVocabularyFile")}.").newLine()
    }
    appendLine("${Emojis.NumberOfRoundsToCoverAllWords}${localeString("TheNumberOfRoundsToCoverAllWords")}: ${spreadsheet.numberOfRoundsToCoverAllWords}.")
}

fun printError(e: Exception? = null, text: String = "") {
    val message = text.ifEmpty { e?.message ?: localeString("UnexpectedError") }
    println("${Emojis.Warning}${localeString("Error")}: ${message}.\n   ${localeString("TryAgain")}!\n")
}

fun responseToPlayAgainAnswer(play: Boolean, scoresSaved: Boolean) = buildString {
    if (scoresSaved) appendLine("${Emojis.ScoresSaved}${localeString("ScoresSavedTo")} $spreadsheetPath.").newLine()
    append(if (play) "${Emojis.LetsPlayAgain}${localeString("OKLetsPlayAgain")}" else "${Emojis.ThankYou}${localeString("ThankYouForPlaying")}")
    appendLine("!")
}

fun startGame() {
    println("${Emojis.LoadingFile}${localeString("LoadingVocabularyFile")}: $spreadsheetPath...")
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
    if (!checkIfLocaleAvailable(languageTag)) {
        println(warningString(localeString("TheLanguageIsNotAvailable")))
    }
    printGreetings()
    Menu.navigate()
}