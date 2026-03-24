package org.dmitriigliuz.linguatwist.game

import org.dmitriigliuz.linguatwist.model.GameState
import org.dmitriigliuz.linguatwist.model.Vocabulary
import org.dmitriigliuz.linguatwist.storage.VocabularyLoadResult
import org.dmitriigliuz.linguatwist.storage.VocabularyLoader
import org.dmitriigliuz.linguatwist.storage.VocabularyWriter
import kotlin.time.TimeSource
import kotlin.time.TimeSource.Monotonic.ValueTimeMark

class GameCoordinator(
    private val config: GameConfig,
    private val loader: VocabularyLoader,
    private val writer: VocabularyWriter
) {
    val maxRounds = config.maxRounds

    fun defaultNumberOfRounds(vocabulary: Vocabulary) =
        if (vocabulary.size < config.maxRounds * config.pairsPerRound) {
            val leftoverPairs = vocabulary.size % config.pairsPerRound
            vocabulary.size / config.pairsPerRound + if (leftoverPairs > 0) 1 else 0
        } else config.defaultRounds

    fun loadVocabulary(): Result<VocabularyLoadResult> = loader.load(config.spreadsheetPath)
            .mapCatching { loadResult ->
                val vocabulary = loadResult.vocabulary
                require(vocabulary.hasEnoughDistinctEntries(config.pairsPerRound)) {
                    "Vocabulary must contain at least ${config.pairsPerRound} distinct entries"
                }
                loadResult
            }

    fun saveVocabulary(vocabulary: Vocabulary) = writer.save(config.spreadsheetPath, vocabulary)

    fun startSession(totalRounds: Int): Pair<GameEngine, ValueTimeMark> = createEngine(totalRounds) to createTimeMark()

    fun finishSession(state: GameState, startTime: ValueTimeMark) = state
        .getResultOrNull()?.copy(duration = startTime.elapsedNow())

    private fun createEngine(totalRounds: Int) = GameEngine(totalRounds, config)

    private fun createTimeMark(): ValueTimeMark = TimeSource.Monotonic.markNow()
}