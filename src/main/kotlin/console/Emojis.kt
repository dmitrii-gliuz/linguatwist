package org.dmitriigliuz.linguatwist.console
var useEmojis = true
enum class Emojis(private val emoji: String) {
    Welcome("🚀"),
    CriticalError("⛔️"),
    Warning("⚠️"),
    Bye("👋"),
    Right("✅"),
    Wrong("❌"),
    Point("🔼"),
    PointMinus("🔽"),
    Score("🌟"),
    Mistakes("❌"),
    ProgressBar("🟩"),
    ProgressBarEmpty("⬜"),
    Enter("👉"),
    Exit("⛔️"),
    GameRules("📖"),
    GameDescription("📝"),
    EnterIndexes("🔢"),
    Ready("✅"),
    CheckVocabulary("🔍"),
    NumberOfRoundsToCoverAllWords("🔤"),
    ScoresSaved("💾"),
    LetsPlayAgain("🔄"),
    ThankYou("🙏"),
    LoadingFile("📂"),
    Sorry("😢"),
    Congratulations("🎉"),
    YourResult("🎯"),
    CompletionTime("⏱");

    override fun toString(): String = if (useEmojis) "$emoji " else ""

    fun withoutSpace() = if (useEmojis) emoji else ""

}