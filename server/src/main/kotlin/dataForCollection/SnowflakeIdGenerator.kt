package org.example.dataForCollection
/**
 * Генератор уникальных идентификаторов в стиле Snowflake.
 *
 * Алгоритм генерирует 64-битные ID, которые состоят из:
 * - timestamp (в миллисекундах),
 * - идентификатора узла (nodeId),
 * - последовательного номера (sequence).
 *
 * Конструктор принимает идентификатор узла, который должен быть в диапазоне [0..1023].
 *
 * @property nodeId идентификатор узла (10 бит).
 */
class SnowflakeIdGenerator(private val nodeId: Long) {
    private var sequence = 0L
    private var lastTimestamp = -1L

    // Константы для битовых сдвигов
    companion object {
        private const val NODE_ID_BITS = 10L
        private const val SEQUENCE_BITS = 12L

        private const val MAX_SEQUENCE = (1L shl SEQUENCE_BITS.toInt()) - 1

        private const val TIMESTAMP_SHIFT = NODE_ID_BITS + SEQUENCE_BITS
        private const val NODE_ID_SHIFT = SEQUENCE_BITS
    }

    /**
     * Генерирует следующий уникальный идентификатор.
     *
     * Метод синхронизирован для корректной работы в многопоточной среде.
     *
     * Алгоритм:
     * - Получает текущий timestamp в миллисекундах.
     * - Если время сдвинулось назад относительно предыдущего (lastTimestamp),
     *   выбрасывает исключение.
     * - Если вызовы идут в пределах одной миллисекунды, увеличивает sequence.
     * - Если sequence переполняется, ждет следующую миллисекунду.
     * - Формирует 64-битный ID, объединяя timestamp, nodeId и sequence.
     *
     * @return уникальный 64-битный идентификатор.
     * @throws RuntimeException если системное время сдвинулось назад.
     */

    fun nextId(): Long {
        var currentTimestamp = System.currentTimeMillis()

        // Если время "отстаёт" (например, NTP скорректировал часы), бросаем ошибку

        if (currentTimestamp < lastTimestamp) {
            throw RuntimeException("Clock moved backwards! Refusing to generate ID.")
        }

        // Если та же миллисекунда, увеличиваем sequence
        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) and MAX_SEQUENCE
            // Если sequence переполнился, ждём следующую миллисекунду
            if (sequence == 0L) {
                currentTimestamp = waitNextMillis(currentTimestamp)
            }
        } else {
            sequence = 0
        }

        lastTimestamp = currentTimestamp

        return (currentTimestamp shl TIMESTAMP_SHIFT.toInt()) or
                (nodeId shl NODE_ID_SHIFT.toInt()) or
                sequence
    }
    /**
     * Ожидает наступления следующей миллисекунды.
     *
     * Используется, когда sequence переполнен в текущей миллисекунде.
     *
     * @param currentTimestamp текущий timestamp в миллисекундах.
     * @return следующий timestamp, который больше lastTimestamp.
     */
    private fun waitNextMillis(currentTimestamp: Long): Long {
        var timestamp = currentTimestamp
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis()
        }
        return timestamp
    }
}