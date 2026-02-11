package org.dmitriigliuz.linguatwist.legacy.presentation.ui.emoji

enum class Emoji(val unicode: String, val fallback: String) {
    // State emojis
    WELCOME("🚀", "[*]"),
    SUCCESS("✅", "[+]"),
    ERROR("❌", "[!]"),
    WARNING("⚠️", "[?]"),
    BYE("👋", "[bye]"),

    // Game emojis
    SCORE("🌟", "★"),
    MISTAKES("❌", "[-]"),
    RIGHT("✅", "[+]"),
    WRONG("❌", "[-]"),
    POINT_UP("🔼", "[+]"),
    POINT_DOWN("🔽", "[-]"),

    // Progress emojis
    PROGRESS("🟩", "█"),
    PROGRESS_EMPTY("⬜", "░"),

    // Action emojis
    LOADING("📂", "..."),
    SAVING("💾", "[S]"),
    INPUT("👉", ">"),
    ENTER("👉", ">"),

    // Game actions
    ROUNDS("🔤", "[R]"),
    GAME_RULES("📖", "[?]"),
    GAME_DESCRIPTION("📝", "[i]"),
    ENTER_INDEXES("🔢", "[#]"),

    // Results and messages
    CONGRATULATIONS("🎉", "!!!"),
    SORRY("😢", ":("),
    YOUR_RESULT("🎯", "[R]"),
    COMPLETION_TIME("⏱", "[T]"),

    // File and data handling
    READY("✅", "[OK]"),
    CHECK_VOCABULARY("🔍", "[?]"),
    SCORES_SAVED("💾", "[S]"),
    LETS_PLAY_AGAIN("🔄", "[R]"),
    THANK_YOU("🙏", "[T]")
}