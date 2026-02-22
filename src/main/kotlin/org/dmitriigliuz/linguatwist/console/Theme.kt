package org.dmitriigliuz.linguatwist.console

import com.github.ajalt.mordant.rendering.TextColors.Companion.rgb
import com.github.ajalt.mordant.rendering.TextStyles.*


object Theme {
    // Cyberpunk palette
    val neonCyan = rgb("#00ffff")
    val neonMagenta = rgb("#ff00ff")
    val neonGreen = rgb("#00ff41")
    val neonYellow = rgb("#ffff00")
    val dimGray = rgb("#666699")
    val darkPurple = rgb("#1a1a2e")

    // Semantic styles
    val title = (bold + neonCyan)
    val success = (bold + neonGreen)
    val error = (bold + neonMagenta)
    val warning = (bold + neonYellow)
    val muted = dimGray
    val accent = neonCyan

    // Game-specific
    val scoreStyle = neonGreen
    val mistakeStyle = neonMagenta
    val roundTitle = (bold + neonCyan)
    val guessedWord = (strikethrough + dimGray)
    val promptStyle = neonCyan
}