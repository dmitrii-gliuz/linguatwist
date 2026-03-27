package org.dmitriigliuz.linguatwist.console

import com.github.ajalt.mordant.rendering.TextColors.Companion.rgb
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.TextStyles.*


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
    val logo: TextStyle

    // Game-specific
    val scoreStyle: TextStyle
    val mistakeStyle: TextStyle
    val guessedWord: TextStyle
    val promptStyle: TextStyle
}


object Cyberpunk : ColorTheme {
    // Cyberpunk palette
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
    override val success = (bold + neonGreen)
    override val error = (bold + neonRed)
    override val warning = (bold + neonYellow)
    override val logo = (bold + warmYellow)

    // Game-specific
    override val scoreStyle = neonGreen
    override val mistakeStyle = neonMagenta
    override val guessedWord = (strikethrough + dimGray)
    override val promptStyle = neonCyan

}