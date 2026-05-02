package dataForCollection

import java.io.Serializable

/**
 * Перечисление возможных типов транспортных средств.
 */
enum class VehicleType : Serializable {
    /** Вертолёт */
    HELICOPTER,
    /** Беспилотник (дрон) */
    DRONE,
    /** Лодка */
    BOAT,
    /** Корабль */
    SHIP;

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}