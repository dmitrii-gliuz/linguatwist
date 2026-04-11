package org.dmitriigliuz.linguatwist.model

import de.comahe.i18n4k.messages.MessageBundleLocalizedString
import org.dmitriigliuz.linguatwist.i18n.Messages

enum class VerbalScore(val percentage: Int, val description: MessageBundleLocalizedString) {
    PERFECT(100, Messages.score_perfect),
    ALMOST_PERFECT(95, Messages.score_almostPerfect),
    EXCELLENT(90, Messages.score_excellent),
    VERY_GOOD(85, Messages.score_veryGood),
    GOOD(80, Messages.score_good),
    ABOVE_AVERAGE(75, Messages.score_aboveAverage),
    AVERAGE(70, Messages.score_average),
    BELOW_AVERAGE(65, Messages.score_belowAverage),
    POOR(60, Messages.score_poor),
    VERY_POOR(55, Messages.score_veryPoor),
    TRY_AGAIN(0, Messages.score_tryAgain);
}