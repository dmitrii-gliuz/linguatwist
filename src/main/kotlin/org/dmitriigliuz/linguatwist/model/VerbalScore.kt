package org.dmitriigliuz.linguatwist.model

import org.dmitriigliuz.linguatwist.i18n.Messages

enum class VerbalScore(val percentage: Int, val description: String) {
    PERFECT(100, Messages.score_perfect.toString()),
    ALMOST_PERFECT(95, Messages.score_almostPerfect.toString()),
    EXCELLENT(90, Messages.score_excellent.toString()),
    VERY_GOOD(85, Messages.score_veryGood.toString()),
    GOOD(80, Messages.score_good.toString()),
    ABOVE_AVERAGE(75, Messages.score_aboveAverage.toString()),
    AVERAGE(70, Messages.score_average.toString()),
    BELOW_AVERAGE(65, Messages.score_belowAverage.toString()),
    POOR(60,Messages.score_poor.toString()),
    VERY_POOR(55, Messages.score_veryPoor.toString()),
    TRY_AGAIN(0, Messages.score_tryAgain.toString());
}