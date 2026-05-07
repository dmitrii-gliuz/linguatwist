package org.dmitriigliuz.linguatwist.console

interface EmojiSet {
    val vocabularyPath: String
        get() = ""
    val numberOfRounds: String
        get() = ""
    val playAgain: String
        get() = ""
    val correct: String
        get() = ""
    val plusOnePoint: String
        get() = ""
    val wrong: String
        get() = ""
    val minusOnePoint: String
        get() = ""
    val error: String
        get() = ""
    val processing: String
        get() = ""
    val allowedMistakes: String
        get() = ""
    val welcome: String
        get() = ""
    val bye: String
        get() = ""
    val advice: String
        get() = ""
    val controls: String
        get() = ""
    val letsGo: String
        get() = ""
    val youFailed: String
        get() = ""
    val youWon: String
        get() = ""
    val guessedPairs: String
        get() = ""
    val mistakes: String
        get() = ""
    val duration: String
        get() = ""
    val vocabularyLoaded: String
        get() = ""
    val warning: String
        get() = ""
    val invalidRows: String
        get() = ""
    val score: String
        get() = ""
    val inputIndexes: String
        get() = ""
    val startGame: String
        get() = ""
    val exit: String
        get() = ""
    val scorePerfect: String
        get() = ""
    val scoreAlmostPerfect: String
        get() = ""
    val scoreExcellent: String
        get() = ""
    val scoreVeryGood: String
        get() = ""
    val scoreGood: String
        get() = ""
    val scoreAboveAverage: String
        get() = ""
    val scoreAverage: String
        get() = ""
    val scoreBelowAverage: String
        get() = ""
    val scorePoor: String
        get() = ""
    val scoreVeryPoor: String
        get() = ""
    val scoreTryAgain: String
        get() = ""
    val saveScores: String
        get() = ""

    fun framed(leftEmoji: String, text: String, rightEmoji: String = leftEmoji): String =
        if (leftEmoji.isBlank() && rightEmoji.isBlank()) text else "$leftEmoji $text $rightEmoji"
}

object DefaultEmoji : EmojiSet {
    const val BOOKS = "📚"
    const val WAVE_HAND = "👋"
    const val GOAL = "🎯"
    const val ROCKET = "🚀"
    const val ARROW_UP = "🔼"
    const val ARROW_DOWN = "🔽"
    const val LAMP = "💡"
    const val JOYSTICK = "🕹️"
    const val CRYING_FACE = "😭"
    const val CONFETTI = "🎉"
    const val GREEN_MARK = "✅"
    const val RED_X = "❌"
    const val HOURGLASS = "⌛"
    const val WRITING_HAND = "✍️"
    const val GAMEPAD = "🎮"
    override val vocabularyPath = BOOKS
    override val numberOfRounds = GOAL
    override val playAgain = "🔄"
    override val correct = GREEN_MARK
    override val plusOnePoint = ARROW_UP
    override val wrong = RED_X
    override val minusOnePoint = ARROW_DOWN
    override val error = "🚫"
    override val processing = HOURGLASS
    override val allowedMistakes = "💀"
    override val welcome = WAVE_HAND
    override val bye = WAVE_HAND
    override val advice = LAMP
    override val controls = JOYSTICK
    override val letsGo = ROCKET
    override val youFailed = CRYING_FACE
    override val youWon = CONFETTI
    override val guessedPairs = GREEN_MARK
    override val mistakes = RED_X
    override val duration = HOURGLASS
    override val vocabularyLoaded = BOOKS
    override val warning = "⚠️"
    override val invalidRows = RED_X
    override val score = "🌟"
    override val inputIndexes = WRITING_HAND
    override val startGame = GAMEPAD
    override val exit = "🚪"
    override val scorePerfect = CONFETTI
    override val scoreAlmostPerfect= "👏"
    override val scoreExcellent = "✨"
    override val scoreVeryGood = "👍"
    override val scoreGood = "😊"
    override val scoreAboveAverage = "🙂"
    override val scoreAverage = "😐"
    override val scoreBelowAverage = "😕"
    override val scorePoor = "😟"
    override val scoreVeryPoor = "😢"
    override val scoreTryAgain = CRYING_FACE
    override val saveScores = "💾"
}


object NoEmoji : EmojiSet


fun String.prefixed(text: String): String =
    if (isBlank()) text else "$this $text"
