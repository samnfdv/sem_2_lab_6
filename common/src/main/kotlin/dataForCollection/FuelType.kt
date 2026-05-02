package dataForCollection

import java.io.Serializable

/**
 * Перечисление возможных типов топлива для транспортных средств.
 */
enum class FuelType : Serializable {
    /** Бензин */
    GASOLINE,
    /** Дизель */
    DIESEL,
    /** Ядерное топливо */
    NUCLEAR,
    /** Антивещество */
    ANTIMATTER;

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}