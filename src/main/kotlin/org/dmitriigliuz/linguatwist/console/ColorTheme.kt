package org.dmitriigliuz.linguatwist.console

import com.github.ajalt.mordant.rendering.TextColors.Companion.rgb
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.TextStyles.*
import com.github.ajalt.mordant.rendering.Theme


interface ColorTheme {
    // Semantic styles
    val progressBarCompleted: TextStyle
    val progressBarPending: TextStyle
    val feedbackPanelBorder: TextStyle
    val panelBorder: TextStyle
    val coloredMessage: TextStyle
    val title: TextStyle
    val success: TextStyle
    val error: TextStyle
    val warning: TextStyle
    val muted: TextStyle
    val logo: TextStyle
    val selectTitle: TextStyle
    val selectSelected: TextStyle
    val selectUnselected: TextStyle
    val selectCursor: TextStyle

    // Game-specific
    val scoreStyle: TextStyle
    val mistakeStyle: TextStyle
    val guessedWord: TextStyle
    val promptStyle: TextStyle

    fun toTerminalTheme(): Theme {
        val newTheme = Theme(Theme.Default) {
            styles["success"] = success
            styles["info"] = coloredMessage
            styles["warning"] = warning
            styles["danger"] = error
            styles["muted"] = muted
            styles["progressbar.complete"] = progressBarCompleted
            styles["progressbar.pending"] = progressBarPending
            styles["progressbar.border"] = feedbackPanelBorder
            styles["panel.border"] = panelBorder
            styles["title"] = title
            styles["select.title"] = selectTitle
            styles["select.cursor"] = selectCursor
            styles["select.selected"] = selectSelected
            styles["select.unselected-title"] = selectUnselected
            styles["prompt.default"] = coloredMessage + bold
            styles["prompt.choices"] = coloredMessage + bold
        }
        return newTheme
    }
}


object CyberpunkTheme : ColorTheme {
    // Palette
    val neonCyan = rgb("#00ffff")
    val neonMagenta = rgb("#ff00ff")
    val neonIndigo = rgb("#7B68EE")
    val neonGreen = rgb("#00ff41")
    val neonRed = rgb("#ff0000")
    val neonYellow = rgb("#ffff00")
    val dimGray = rgb("#666699")
    val darkPurple = rgb("#1a1a2e")
    val warmYellow = rgb("#FCE300")

    // Semantic styles
    override val progressBarCompleted = neonGreen
    override val progressBarPending = darkPurple
    override val feedbackPanelBorder = dimGray
    override val panelBorder = neonIndigo
    override val coloredMessage = neonCyan
    override val title = (bold + neonCyan)
    override val success = (neonGreen)
    override val error = (bold + neonRed)
    override val warning = neonYellow
    override val muted = dimGray
    override val logo = (bold + warmYellow)
    override val selectTitle = (bold + neonIndigo)
    override val selectCursor = neonCyan
    override val selectSelected = bold + neonGreen
    override val selectUnselected = TextStyle()

    // Game-specific
    override val scoreStyle = neonGreen
    override val mistakeStyle = neonMagenta
    override val guessedWord = (strikethrough + dimGray)
    override val promptStyle = neonCyan
}

object DaylightTheme : ColorTheme {
    // Palette
    val black = rgb("#000000")
    val blue = rgb("#1a0099")
    val magenta = rgb("#6a1080")
    val indigo = rgb("#5B4FBE")
    val green = rgb("#145a27")
    val red = rgb("#c0392b")
    val yellow = rgb("#7a4e00")
    val dimGray = rgb("#555555")
    val softGray = rgb("#e8e8f0")

    // Semantic styles
    override val progressBarCompleted = green
    override val progressBarPending = softGray
    override val feedbackPanelBorder = dimGray
    override val panelBorder = indigo
    override val coloredMessage = blue
    override val title = (bold + blue)
    override val success = (bold + green)
    override val error = (bold + red)
    override val warning = (bold + yellow)
    override val muted = dimGray
    override val logo = (bold + black)
    override val selectTitle = title
    override val selectCursor = blue
    override val selectSelected = bold + green
    override val selectUnselected = TextStyle()

    // Game-specific
    override val scoreStyle = green
    override val mistakeStyle = magenta
    override val guessedWord = (strikethrough + dimGray)
    override val promptStyle = blue
}


