package org.dmitriigliuz.linguatwist.domain.model

enum class VerbalScore(val percentage: Int, val emoji: String, val description: String) {
    PERFECT(100, "🎉", "Perfect"),
    ALMOST_PERFECT(95, "👏", "AlmostPerfect"),
    EXCELLENT(90, "✨", "Excellent"),
    VERY_GOOD(85, "👍", "VeryGood"),
    GOOD(80, "😊", "Good"),
    ABOVE_AVERAGE(75, "🙂", "AboveAverage"),
    AVERAGE(70, "😐", "Average"),
    BELOW_AVERAGE(65, "😕", "BelowAverage"),
    POOR(60, "😟", "Poor"),
    VERY_POOR(55, "😢", "VeryPoor"),
    TRY_AGAIN(0, "😭", "TryAgain");
}