package org.dmitriigliuz.linguatwist.model

enum class VerbalScore(val percentage: Int, val emoji: String, val description: String) {
    PERFECT(100, "🎉", "Perfect"),
    ALMOST_PERFECT(95, "👏", "Almost perfect"),
    EXCELLENT(90, "✨", "Excellent"),
    VERY_GOOD(85, "👍", "Very good"),
    GOOD(80, "😊", "Good"),
    ABOVE_AVERAGE(75, "🙂", "Above average"),
    AVERAGE(70, "😐", "Average"),
    BELOW_AVERAGE(65, "😕", "Below average"),
    POOR(60, "😟", "Poor"),
    VERY_POOR(55, "😢", "Very poor"),
    TRY_AGAIN(0, "😭", "Try again");
}