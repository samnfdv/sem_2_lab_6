package org.example.commands

import mechanicsOfCollection.CollectionManager
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.example.Response
import org.example.dataBase.DataBaseManager
import org.example.dataForCollection.Vehicle
import org.example.dataForCollection.VehicleArgsPack

/**
 * Команда для добавления нового элемента в коллекцию.
 * Принимает {@link VehicleArgsPack}, создает {@link Vehicle} и сохраняет его
 * через {@link CollectionManager}.
 */
class AddCommand(
    private val collectionManager: CollectionManager,
    val dataBaseManager: DataBaseManager
) : Command {

    companion object {
        private val logger: Logger = LogManager.getLogger(AddCommand::class.java)
    }

    /**
     * Выполняет команду добавления элемента.
     *
     * @param args массив аргументов, ожидается ровно один элемент типа {@link VehicleArgsPack}.
     * @return результат выполнения команды (сообщение об успехе или ошибке).
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

        try {
            val vehicle = Vehicle.createNew(
                arg.userLogin,
                arg.name,
                arg.coordinates,
                arg.enginePower,
                arg.numberOfWheels,
                arg.type,
                arg.fuelType
            )
            val login = arg.userLogin
            logger.info("Элемент добавлен: {}", vehicle)
            val db= dataBaseManager.addObject(vehicle, login, collectionManager)
            if (db.isSuccess) return db.message!!
//            collectionManager.addVehicle(vehicle)
            return "Ошибка при добавлении элемента: ${db.message}"
//            "Элемент успешно добавлен: $vehicle"
        } catch (e: IllegalStateException) {
            logger.error("Ошибка при добавлении элемента: {}", e.message)
            return "Ошибка при добавлении элемента: ${e.message}"
        }
    }
}
