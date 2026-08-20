package org.dmitriigliuz.linguatwist.game

import io.github.oshai.kotlinlogging.KotlinLogging
import org.dmitriigliuz.linguatwist.i18n.Messages
import org.dmitriigliuz.linguatwist.model.GameResult
import org.dmitriigliuz.linguatwist.model.GameState
import org.dmitriigliuz.linguatwist.model.Vocabulary
import org.dmitriigliuz.linguatwist.storage.VocabularyLoadResult
import org.dmitriigliuz.linguatwist.storage.VocabularyLoader
import org.dmitriigliuz.linguatwist.storage.VocabularyWriter
import org.dmitriigliuz.linguatwist.storage.createBackup
import kotlin.time.TimeSource
import kotlin.time.TimeSource.Monotonic.ValueTimeMark

private val logger = KotlinLogging.logger { }

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

    fun loadVocabulary(path: String? = null): Result<VocabularyLoadResult> = loader.load(path ?: config.spreadsheetPath)
        .mapCatching { loadResult ->
            val vocabulary = loadResult.vocabulary
            require(vocabulary.hasEnoughDistinctEntries(config.pairsPerRound)) {
                Messages.vocabulary_error_notEnoughDistinct(config.pairsPerRound)
            }
            loadResult
        }

    fun saveVocabulary(path: String, vocabulary: Vocabulary) = writer.save(path, vocabulary)

    fun createVocabularyBackup(vocabularyPath: String) = createBackup(vocabularyPath)

    fun startSession(totalRounds: Int) = Pair(createEngine(totalRounds), createTimeMark()).also {
        logger.info { "Game session started: rounds=$totalRounds, pairsPerRound=${config.pairsPerRound}" }
    }

    fun finishSession(state: GameState, startTime: ValueTimeMark): GameResult? {
        val result = state.getResultOrNull()?.copy(duration = startTime.elapsedNow())
        if (result != null) {
            logger.info {
                "Game session finished: failed=${result.isFailed}, guessed=${result.totalGuessedAmount}/${result.totalPairsAmount}, mistakes=${result.mistakesCount}/${result.allowedMistakes}, duration=${result.duration}"
            }
        } else logger.warn { "Game session finished without final result. state=${state::class.simpleName}" }
        return result
    }

    private fun createEngine(totalRounds: Int) = GameEngine(totalRounds, config)

    private fun createTimeMark(): ValueTimeMark = TimeSource.Monotonic.markNow()
}
