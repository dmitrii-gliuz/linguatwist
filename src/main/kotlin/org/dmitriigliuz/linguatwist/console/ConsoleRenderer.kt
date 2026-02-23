package org.dmitriigliuz.linguatwist.console

import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.Padding
import com.github.ajalt.mordant.widgets.Panel
import com.github.ajalt.mordant.widgets.Text
import org.dmitriigliuz.linguatwist.i18n.Messages

class ConsoleRenderer(
    private val terminal: Terminal,
    private val messages: Messages
) {
    fun printLogo() {
        val art = """
    ▖ ▘        ▄▖   ▘  ▗
    ▌ ▌▛▌▛▌▌▌▀▌▐ ▌▌▌▌▛▘▜
    ▙▖▌▌▌▙▌▙▌█▌▐ ▚▚▘▌▄▌▐▖
         ▄▌         ── v0.2 ──
⟐  L E A R N  · S U R V I V E  ⟐""".trimMargin()
        val colors = listOf(Theme.neonCyan, Theme.neonMagenta, Theme.neonGreen)
        val coloredArt = buildString {
            art.lines().forEachIndexed { i, line ->
                if (i < art.lines().size - 1) appendLine(colors[i % colors.size](line))
                else append(colors[i % colors.size](line))
            }
        }
        terminal.println(Panel(Text(coloredArt), padding = Padding(top = 0, right = 1, bottom = 0, left = 1)))
    }

}