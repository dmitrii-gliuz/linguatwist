package org.dmitriigliuz.linguatwist.console

import com.github.ajalt.mordant.rendering.TextColors.Companion.rgb
import com.github.ajalt.mordant.rendering.TextStyles.*


internal object Theme {
    // Cyberpunk palette
    val neonCyan = rgb("#00ffff")
    val neonMagenta = rgb("#ff00ff")
    val neonIndigo = rgb("#7B68EE")
    val neonGreen = rgb("#00ff41")
    val neonRed = rgb("#ff0000")
    val neonYellow = rgb("#ffff00")
    val dimGray = rgb("#666699")
    val darkPurple = rgb("#1a1a2e")

    // Semantic styles
    val title = (bold + neonCyan)
    val success = (bold + neonGreen)
    val error = (bold + neonRed)
    val warning = (bold + neonYellow)

    // Game-specific
    val scoreStyle = neonGreen
    val mistakeStyle = neonMagenta
    val guessedWord = (strikethrough + dimGray)
    val promptStyle = neonCyan
}