package org.example


import dataForCollection.Coordinates
import dataForCollection.*

import outerLayer.InputManager
import outerLayer.OutputManager
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.example.dataForCollection.VehicleArgsPack

/**
 * Класс, отвечающий за запрос и валидацию данных для создания объектов [Vehicle].
 * Поддерживает как интерактивный, так и скриптовый режим ввода.
 *
 * @property outputManager Менеджер вывода.
 * @property inputManager Менеджер ввода.
 * @property reader Утилиты преобразования ввода.
 */
class VehicleAdder(
    val outputManager: OutputManager,
    private val inputManager: InputManager,
    private val reader: Reader
) {
    private val LOGGER: Logger = LogManager.getLogger(VehicleAdder::class.java)

    /**
     * Запрашивает и валидирует имя транспортного средства.
     * @return валидное имя
     * @throws IllegalArgumentException если имя пустое
     */
    fun validName(): String {
        if (inputManager.isScriptMode()) {
            val name = inputManager.read().trim()
            if (name.isBlank()) throw IllegalArgumentException("Имя не может быть пустым.")
            LOGGER.info("The name={$name} was read from the script")
            return name
        } else {
            while (true) {
                try {
                    outputManager.print("Введите имя: ")
                    val name = reader.readLineTrimmed(inputManager.read())

                    if (name.isBlank()) {
                        outputManager.println(
                            """Ошибка: имя введено не корректно.
                        |Имя не может быть пустой строкой.
                    """.trimMargin()
                        )
                        LOGGER.error("Error: the name was entered incorrectly blank.")
                        continue
                    }
                    LOGGER.info("The name={$name} was read")
                    return name
                } catch (exception: Exception) {
                    outputManager.println("Ошибка: ${exception.message ?: "Неизвестная ошибка"}")
                    LOGGER.error("Error:${exception.message ?: " Unknown error."}")
                }
            }
        }
    }

    /**
     * Запрашивает и валидирует координаты.
     * @return объект [Coordinates]
     * @throws NumberFormatException если координаты невалидны
     */
    fun validCoordinates(): Coordinates {
        fun x(): Float {
            (if (inputManager.isScriptMode()) {
                val coordinate = reader.readFloat(inputManager.read())
                LOGGER.info("The corX={$coordinate} was read from the script")
                return coordinate
            } else {
                while (true) {
                    try {
                        print("Введите координату Х : ")
                        val string = reader.readLineTrimmed(inputManager.read())
                        if (string.isBlank()) {
                            println("Ошибка: введена пустая строка")
                            LOGGER.error("Error: the corX was entered incorrectly blank.")

                            continue
                        }
                        if (string == null) {
                            outputManager.println("Ошибка: Координата не может быть null.")
                            LOGGER.error("Error: the corX was entered incorrectly null.")
                            continue
                        }
                        val corx = reader.readFloat(string)
                        LOGGER.info("The cX={$corx} was read")
                        return corx


                    } catch (exeption: NumberFormatException) {
                        println(
                            "Ошибка: значение введено не корректно.\n" +
                                    "Значение должно быть числом.\n" +
                                    "Попробуйте еще раз."
                        )
                        LOGGER.error("Error: the corX was entered incorrectly.")
                    } catch (exception: Exception) {
                        println("Неивестная ошибка:$exception")
                        LOGGER.error("Error:${exception.message ?: " Unknown error."}")
                    }
                }

            })
        }

        fun y(): Int {
            (if (inputManager.isScriptMode()) {
                val coordinate = reader.readInt(reader.readLineTrimmed(inputManager.read()))
                LOGGER.info("The cY={$coordinate} was read")
                return coordinate
            } else {
                while (true) {
                    try {
                        outputManager.print("Введите координату Y : ")
                        val string = reader.readLineTrimmed(inputManager.read())
                        if (string.isBlank()) {
                            outputManager.println("Ошибка: Введена пустая строка.")
                            LOGGER.error("Error: the corY was entered incorrectly blank.")
                            continue
                        }

                        if (string == null) {
                            outputManager.println("Ошибка: Координата не может быть null.")
                            LOGGER.error("Error: the corY was entered incorrectly null.")
                            continue
                        }
                        val cory = reader.readInt(string)
                        LOGGER.info("The cY={$cory} was read")
                        return cory

                    } catch (exeption: NumberFormatException) {
                        println(
                            "Ошибка: значение введено не корректно.\n" +
                                    "Значение должно быть числом.\n" +
                                    "Попробуйте еще раз."
                        )
                        LOGGER.error("Error: the corY was entered incorrectly.")
                    } catch (exception: Exception) {
                        outputManager.print("Ошибка: ${exception.message ?: "Неизвестная ошибка"}")
                        LOGGER.error("Error:${exception.message ?: " Unknown error."}")
                    }
                }

            })
        }
        return Coordinates((x()), (y()))
    }

    /**
     * Запрашивает и валидирует мощность двигателя.
     * @return значение мощности
     * @throws IllegalArgumentException если мощность недопустима
     */
    fun validEnginePower(): Long {
        (if (inputManager.isScriptMode()) {
            val engiePow = reader.readLong(reader.readLineTrimmed(inputManager.read()))
            if (engiePow <= 0) throw IllegalArgumentException("Mощность двигателя долженa быть > 0.")
            LOGGER.info("The engiePow={$engiePow} was read")
            return engiePow
        } else {
            while (true) {
                try {
                    outputManager.print("Введите мощность двигателя : ")
                    val string = reader.readLineTrimmed(inputManager.read())
                    if (string.isBlank()) {
                        outputManager.println("Ошибка: Введена пустая строка.")
                        continue
                    }
                    val enginePow = reader.readLong(string)

                    if (enginePow > 0) {
                        LOGGER.info("The enginePow={$enginePow} was read")
                        return enginePow

                    } else {
                        outputManager.println("Ошибка: Мощность двигателя должна быть больше нуля.")
                    }

                } catch (exeption: NumberFormatException) {
                    println(
                        "Ошибка: значение введено не корректно.\n" +
                                "Значение должно быть числом.\n" +
                                "Попробуйте еще раз."
                    )
                    LOGGER.error("Error: the EnginePower was entered incorrectly.")
                } catch (exception: Exception) {
                    outputManager.println("Ошибка: ${exception.message ?: "Неизвестная ошибка"}")
                    LOGGER.error("Error:${exception.message ?: " Unknown error."}")
                }
            }

        })
    }

    /**
     * Запрашивает и валидирует количество колёс.
     * @return значение > 0
     * @throws IllegalArgumentException если значение неверное
     */
    fun validNumberOfWheels(): Int {
        (if (inputManager.isScriptMode()) {
            val numberOfWeeels = reader.readInt(reader.readLineTrimmed(inputManager.read()))
            if (numberOfWeeels <= 0) throw IllegalArgumentException("количество колес должно быть > 0.")

            return numberOfWeeels
        } else {
            while (true) {
                try {
                    outputManager.print("Введите количество колес : ")
                    val string = reader.readLineTrimmed(inputManager.read())
                    if (string.isBlank()) {
                        outputManager.println("Ошибка: Введена пустая строка.")
                        continue
                    }
                    val numberOfWeeels = reader.readInt(string)

                    if (numberOfWeeels > 0) {
                        LOGGER.info("The wheels={$numberOfWeeels} was read")
                        return numberOfWeeels

                    } else {
                        outputManager.println("Ошибка: Количество колес должнo быть больше нуля.")
                    }

                } catch (exeption: NumberFormatException) {
                    println(
                        "Ошибка: значение введено не корректно.\n" +
                                "Значение должно быть числом.\n" +
                                "Попробуйте еще раз."
                    )
                    LOGGER.error("Error: the NumberOfWheels was entered incorrectly.")
                } catch (exception: Exception) {
                    outputManager.println("Ошибка: ${exception.message ?: "Неизвестная ошибка"}")
                    LOGGER.error("Error:${exception.message ?: " Unknown error."}")

                }
            }

        })
    }

    /**
     * Запрашивает и валидирует тип транспортного средства.
     * @return [VehicleType] или null, если тип не выбран
     */
    fun validVehicleType(): VehicleType? {
        fun wichType(string: String): VehicleType? {
            val type = string.lowercase()
            return when (type) {
                "helicopter", "heli" -> {
                    LOGGER.info("The vehicleType = HELICOPTER was read")
                    VehicleType.HELICOPTER
                }

                "drone" -> {
                    LOGGER.info("The vehicleType = DRONE was read")
                    VehicleType.DRONE
                }

                "boat" -> {
                    LOGGER.info("The vehicleType = BOAT was read")
                    VehicleType.BOAT
                }

                "ship" -> {
                    LOGGER.info("The vehicleType = SHIP was read")
                    VehicleType.SHIP
                }

                else -> {
                    LOGGER.info("The vehicleType = null was read")
                    null
                }
            }
        }
        if (inputManager.isScriptMode()) {
            val vehicleType = reader.readLineTrimmed(inputManager.read())

            return wichType(vehicleType)
        } else {
            while (true) {
                try {
                    outputManager.println(
                        """Выберите один из типов транспортного средства (необязательное значение, если не хотите указывать введите что угодно кроме предоставленных вариантов) 
                        HELICOPTER,
                        DRONE,
                        BOAT,
                        SHIP
                    """.trimIndent()
                    )
                    outputManager.print("Введите тип транспортного средства : ")
                    val string = reader.readLineTrimmed(inputManager.read())

                    return wichType(string)

                } catch (exception: Exception) {
                    outputManager.println("Ошибка: ${exception.message ?: "Неизвестная ошибка"}")
                    LOGGER.error("Error:${exception.message ?: " Unknown error."}")

                }

            }
        }

    }

    /**
     * Запрашивает и валидирует тип топлива.
     * @return [FuelType] или null, если тип не выбран
     */
    fun validFuelType(): FuelType? {
        fun wichType(string: String): FuelType? {
            val type = string.lowercase()
            return when (type) {
                "gasoline", "gas" -> FuelType.GASOLINE
                "diesel", "dis" -> FuelType.DIESEL
                "nuclear", "nuc" -> FuelType.NUCLEAR
                "antimatter", "anti" -> FuelType.ANTIMATTER
                else -> null
            }
        }
        if (inputManager.isScriptMode()) {
            val fuelType = reader.readLineTrimmed(inputManager.read())

            return wichType(fuelType)
        } else {
            while (true) {
                try {
                    outputManager.println(
                        """Выберите один из типов топлива (необязательное значение, если не хотите указывать введите что угодно кроме предоставленных вариантов) 
                        GASOLINE,
                        DIESEL,
                        NUCLEAR,
                        ANTIMATTER
                    """.trimIndent()
                    )
                    outputManager.print("Введите тип топлива : ")
                    val string = reader.readLineTrimmed(inputManager.read())
                    return wichType(string)
                } catch (exception: Exception) {
                    outputManager.println("Ошибка: ${exception.message ?: "Неизвестная ошибка"}")
                    LOGGER.error("Error:${exception.message ?: " Unknown error."}")

                }


            }
        }
    }

    fun vehiclePack(): VehicleArgsPack {

        val login = ClientApp.user.login as String
        val name = validName()
        val coordinates: Coordinates = validCoordinates()
        val engnePower = validEnginePower()
        val numberOfWheels = validNumberOfWheels()
        val vechicletype:VehicleType? = validVehicleType()
        val fuelType : FuelType?= validFuelType()
        val vechicle: VehicleArgsPack =
            VehicleArgsPack(login, name, coordinates, engnePower, numberOfWheels, vechicletype, fuelType)
        outputManager.println("Объект отправлен на сервер")
        return vechicle

    }

}