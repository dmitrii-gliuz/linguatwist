package org.dmitriigliuz.linguatwist.console.theming

import com.github.ajalt.mordant.rendering.BorderType
import org.dmitriigliuz.linguatwist.console.DefaultEmoji
import org.dmitriigliuz.linguatwist.console.EmojiSet
import org.dmitriigliuz.linguatwist.console.NoEmoji
import org.dmitriigliuz.linguatwist.console.prefixed
import org.dmitriigliuz.linguatwist.i18n.Messages

enum class LogoDecoration {
    PANEL, HORIZONTAL_LINE,
}

interface Theme {
    //    Styles
    val colorTheme: ColorTheme
    val logoDecoration: LogoDecoration
    val roundPanelBorderType: BorderType
    val feedbackPanelBorderType: BorderType

    //  Properties
    val showMenuInstructions: Boolean

    val emoji: EmojiSet

    // String values
    val logo: String
    fun welcomeMessage(): String
    fun startingGameMessage(): String
}

class RetroGame(noEmoji: Boolean = false): Theme {
    override val colorTheme = CyberpunkTheme
    override val logoDecoration = LogoDecoration.PANEL
    override val roundPanelBorderType = BorderType.ROUNDED
    override val feedbackPanelBorderType = BorderType.ASCII

    override val emoji = if(noEmoji) NoEmoji else DefaultEmoji
    override val logo = colorTheme.logo("""
█   █ █▄ █ █▀▀ █ █ █▀█ ▀█▀ █   █ █ ▄▀▀ ▀█▀
█   █ █ ▀█ █ ▀ █ █ █▀█  █  █ █ █ █  ▀▄  █ 
▀▀▀ ▀ ▀  ▀ ▀▀▀ ▀▀▀ ▀ ▀  ▀   ▀ ▀  ▀ ▀▀▀  ▀""".trimIndent())

    override val showMenuInstructions = true

    override fun welcomeMessage() =
        "${emoji.welcome.prefixed(Messages.game_greetings_welcome())} ${Messages.game_greetings_learnWords()}\n" +
                emoji.advice.prefixed(Messages.game_greetings_matchTranslations())

    override fun startingGameMessage() =
        colorTheme.coloredMessage (emoji.letsGo.prefixed(Messages.game_letsGo()))
}
