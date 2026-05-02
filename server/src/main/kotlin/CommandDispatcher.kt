package org.example

import mechanicsOfCollection.CollectionManager
import org.example.commands.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.example.dataBase.DataBaseManager

/**
 * Dispatcher команд: читает Request, выполняет соответствующую команду и формирует Response.
 * Ведет историю последних 10 выполненных команд и логирует ключевые события.
 * @property collectionManager Менеджер коллекции, используемый для выполнения команд.
 */
class CommandDispatcher(
    private val collectionManager: CollectionManager,
    val dataBaseManager: DataBaseManager
) {
    private val LOGGER: Logger = LogManager.getLogger(CommandDispatcher::class.java)

    /** История последних выполненных команд (максимум 10). */
    private val history = ArrayList<String>()

    /**
     * Получить историю последних выполненных команд.
     * @return Строка с перечислением последних команд через запятую.
     */
    fun getHystory(): String {
        return history.joinToString(", ")
    }

    /**
     * Выполняет команду из Request и возвращает результат в Response.
     * Логирует успешное выполнение команд и ошибки.
     * @param request Запрос с типом команды и аргументами.
     * @return Response с результатом выполнения команды, сообщением и дополнительными данными.
     */
    fun dispatch(request: Request): Response {
        val type = request.type
        val args = (request.args as? Array<Any>) ?: emptyArray()

        var message: String?
        var data: Any? = null
        var success = true

        try {
            when (type) {

                CommandType.ADD -> {
                    message = AddCommand(collectionManager,dataBaseManager).execute(args)
                    history.add(type.toString())
                }

                CommandType.ADD_IF_MIN -> {
                    message = AddIfMinCommand(collectionManager,dataBaseManager).execute(args)
                    history.add(type.toString())
                }

                CommandType.REMOVE_BY_ID -> {
                    message = RemoveByIdCommand(collectionManager,dataBaseManager).execute(args)
                    history.add(type.toString())
                }

                CommandType.UPDATE -> {
                    message = UpdateByIdCommand(collectionManager, dataBaseManager).execute(args)
                    history.add(type.toString())
                    }

                CommandType.CLEAR -> {
                    message = ClearCollection(collectionManager, dataBaseManager).execute(args)
                    history.add(type.toString())
                }

                CommandType.SHOW -> {
                    val out: String = ShowCommand(collectionManager).execute(args)
                    message = if (out.isEmpty()) "Коллекция пуста." else out
                    history.add(type.toString())
                }

                CommandType.INFO -> {
                    message = collectionManager.infoCollection()
                    history.add(type.toString())
                }

                CommandType.FILTER_CONTAINS_NAME -> {
                    message = FilterContainsNameCommand(collectionManager).execute(args)
                    history.add(type.toString())
                }

                CommandType.FILTER_LESS_THAN_NUMBER_OF_WHEELS -> {
                    message = FilterLessThanNumberOfWheels(collectionManager).execute(args)
                    history.add(type.toString())
                }

                CommandType.PRINT_DESCENDING -> {
                    message = PrintDescendingCommand(collectionManager).execute(args)
                    history.add(type.toString())
                }

                CommandType.REMOVE_GREATER -> {
                    message = RemoveGreaterCommand(collectionManager,dataBaseManager).execute(args)
                    history.add(type.toString())
                }

                CommandType.HISTORY -> {
                    message = getHystory()
                    history.add(type.toString())
                }

                CommandType.CHECK_ID -> {
                    val ci = CheckIdCommand(collectionManager)
                    message = ci.execute(args)
                    data = ci.getId(args)
                }

                CommandType.LOGIN -> {
                    message = LogIn(dataBaseManager, collectionManager).execute(args)
                    data = message.split(" ")[1]
                }

                CommandType.REGISTRATION -> {
                    message = Registration(collectionManager, dataBaseManager).execute(args)
                }


                else -> {
                    success = false
                    message = "Неизвестная команда: $type"
                    LOGGER.warn("Попытка выполнить неизвестную команду: {}", type)
                }
            }


            while (history.size > 8) {
                history.removeAt(0)
            }

            LOGGER.info("Команда выполнена: {}, успех: {}", type, success)
        } catch (e: Exception) {
            LOGGER.error("Ошибка при выполнении команды {}: {}", type, e.message, e)
            success = false
            message = "Ошибка: " + e.message
        }

        return Response(success, message + "\n", data)
    }
}
