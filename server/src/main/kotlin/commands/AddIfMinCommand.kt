package org.example.commands

import mechanicsOfCollection.CollectionManager
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.example.dataBase.DataBaseManager
import org.example.dataForCollection.Vehicle
import org.example.dataForCollection.VehicleArgsPack

/**
 * Команда для добавления нового элемента в коллекцию,
 * если его значение (enginePower) меньше минимального элемента в коллекции.
 */
class AddIfMinCommand(private val collectionManager: CollectionManager,
    val dataBaseManager: DataBaseManager) : Command {

    companion object {
        private val logger: Logger = LogManager.getLogger(AddIfMinCommand::class.java)
    }

    /**
     * Выполняет команду добавления элемента, если он минимален.
     *
     * @param args массив аргументов, ожидается ровно один элемент типа {@link VehicleArgsPack}.
     * @return результат выполнения команды (сообщение об успехе или причине отказа).
     */
    override fun execute(args: Array<Any>?): String {
        if (args == null || args.size != 1) {
            logger.error("Неверное количество аргументов: {}", args?.size ?: 0)
            return "Ошибка: ожидается один аргумент."
        }

        val arg = args[0]
        if (arg !is VehicleArgsPack) {
            logger.error("Неверный тип аргумента: {}", arg::class.java.name)
            return "Ошибка: неверный тип аргумента."
        }

        return try {
            val vehicle = Vehicle.createNew(
                arg.userLogin,
                arg.name,
                arg.coordinates,
                arg.enginePower,
                arg.numberOfWheels,
                arg.type,
                arg.fuelType
            )

            if (collectionManager.isCollectionEmpty() ||
                vehicle.enginePower < collectionManager.getMinVehicle().enginePower
            ) {
                dataBaseManager.addIfMin(vehicle, arg.userLogin, collectionManager ).message
//                collectionManager.addVehicle(vehicle)
                logger.info("Элемент добавлен (минимальный): {}", vehicle)
                "Элемент успешно добавлен: $vehicle"
            } else {
                logger.info("Элемент не добавлен: {} (не минимален)", vehicle)
                "Элемент не добавлен в коллекцию, так как не минимален."
            }
        } catch (e: IllegalStateException) {
            logger.error("Ошибка при добавлении элемента: {}", e.message)
            "Ошибка при добавлении элемента: ${e.message}"
        }
    }
}
