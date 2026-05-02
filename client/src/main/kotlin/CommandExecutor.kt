package commands


import AcountsManager
import ExecuteScriptManager
import org.example.*
import outerLayer.InputManager
import outerLayer.OutputManager

/**
 * Класс для выполнения и управления командами.
 *
 * Хранит набор доступных команд и обеспечивает их выполнение.
 * Поддерживает историю выполненных команд.
 *
 * @property vehicleAdder объект для добавления новых транспортных средств.
 * @property collectionManager менеджер коллекции объектов.
 * @property outputManager менеджер вывода сообщений пользователю.
 * @property inputManager менеджер ввода данных.
 * @property collectionFileManager менеджер работы с файлами коллекции.
 */
class CommandExecutor(
    private val vehicleAdder: VehicleAdder,
    private val outputManager: OutputManager,
    private val inputManager: InputManager,
    private val acountsManager: AcountsManager
) {
    /**
     * Сопоставление названий команд и их реализаций.
     */

    private val executeScriptManager: ExecuteScriptManager = ExecuteScriptManager(this, outputManager, inputManager)
    fun whatCommand(str: String): CommandType? {
        return when (str) {
            "add" -> CommandType.ADD
            "info" -> CommandType.INFO
            "show" -> CommandType.SHOW
            "remove_by_id" -> CommandType.REMOVE_BY_ID
            "update" -> CommandType.CHECK_ID
            "clear" -> CommandType.CLEAR
            "add_if_min" -> CommandType.ADD_IF_MIN
            "remove_greater" -> CommandType.REMOVE_GREATER
            "filter_contains_name" -> CommandType.FILTER_CONTAINS_NAME
            "filter_less_than_number_of_wheels" -> CommandType.FILTER_LESS_THAN_NUMBER_OF_WHEELS
            "print_descending" -> CommandType.PRINT_DESCENDING
            "execute_script" -> CommandType.EXECUTE_SCRIPT
            "login" -> CommandType.LOGIN
            "registration" -> CommandType.REGISTRATION
            else -> null
        }

    }

    fun getId(): Long {
        while (true) {
            try {
                outputManager.print("Введите id объекта, который хотите изменить : ")
                val string = inputManager.read().trim()
                if (string.isBlank()) {
                    outputManager.println("Ошибка: Введена пустая строка.")
                    continue
                }
                val id = string.toLong()
                if (id < 1) {
                    outputManager.println("Ошибка: id должно быть больше нуля.")
                    continue
                }
                return id
            } catch (e: NumberFormatException) {
                outputManager.println("Ошибка: id должно быть числом")
            } catch (e: Exception) {
                outputManager.println("Ошибка: ${e.message}")
            }
        }
    }

    fun filter_comtains_name(): String {
        while (true) {
            outputManager.print("Введите строку для фильтрации : ")
            val string = inputManager.read().trim()
            if (string.isBlank()) {
                outputManager.println("Ошибка: Введена пустая строка")
                continue
            }
            return string
        }
    }


    fun getArgs(command: CommandType): Any? {
        return when (command) {
            CommandType.ADD -> {
                println(ClientApp.user.login)
                vehicleAdder.vehiclePack()}

            CommandType.ADD_IF_MIN -> vehicleAdder.vehiclePack()

            CommandType.SHOW -> ClientApp.user.login

            CommandType.CHECK_ID -> arrayOf( getId(), ClientApp.user.login)

            CommandType.REMOVE_BY_ID -> arrayOf( getId(), ClientApp.user.login)

            CommandType.REMOVE_GREATER -> arrayOf( vehicleAdder.validEnginePower(), ClientApp.user.login)

            CommandType.FILTER_CONTAINS_NAME -> filter_comtains_name()

            CommandType.FILTER_LESS_THAN_NUMBER_OF_WHEELS -> vehicleAdder.validNumberOfWheels()

            CommandType.CLEAR -> ClientApp.user.login

            CommandType.LOGIN -> acountsManager.login()

            CommandType.REGISTRATION -> acountsManager.registration()

            else -> null
        }
    }

    fun build(line: String): Request? {
        if (ClientApp.getIWFI()) return null
        val trimmed = line.trim()
        if (trimmed.isEmpty()) return null
        val args = trimmed.split("\\s+".toRegex())
        val command = whatCommand(args[0]) ?: return null
        val args2 = getArgs(command)
        val arg = if (args2 != null) arrayOf(args2) else null
        println(arg)
        return Request(command, arg)
    }


    /**
     * Выводит список доступных команд.
     */
    fun help() {
        outputManager.surePrint("Доступные команды: ${(CommandType.entries.joinToString { it.name.lowercase() })}")

    }

    /**
     * История последних выполненных команд.
     */
    private val hystory = mutableListOf<String>()

    /**
     * Выводит историю выполненных команд.
     */
    fun getHistory() {
        outputManager.surePrint(hystory.joinToString(", "))
    }

    /**
     * Выполняет команду, заданную строкой [commandStr].
     *
     * Строка разбивается на имя команды и необязательный аргумент.
     * Если команда неизвестна, выводится сообщение об ошибке.
     * При выполнении команда добавляется в историю.
     *
     * @param commandStr строка команды с аргументом или без.
     */
    fun executeCommand(commandStr: String): Any? {


        val parts = commandStr.trim().split("\\s+".toRegex())
        val commandName = parts[0].lowercase()
        val args = if (parts.size > 1) parts[1] else null
        if (commandName == "history") {
            getHistory()
            return null
        }
        if (commandName == "help") {
            help()
            return null
        }
        val command = whatCommand(commandName)
        if (command == null) {
            outputManager.println("Неизвестная команда. Доступные команды: ${CommandType.entries.joinToString { it.name }}")
            return null
        }

        if (command == CommandType.EXECUTE_SCRIPT) {
            return executeScriptManager.execute(args)
        }
        if(command in arrayOf(CommandType.ADD, CommandType.ADD_IF_MIN, CommandType.CHECK_ID, CommandType.REMOVE_GREATER, CommandType.CLEAR) && ClientApp.user.login == null){
            outputManager.println("Данная команда не доступна до входа в аккаунт")
            return null
        }


        if (inputManager.isScriptMode()) {


            hystory.add(0, commandName)
            if (hystory.lastIndex == 9) {
                hystory.removeAt(9)
            }
            return build(commandName)

        } else {

            hystory.add(0, commandName)
            if (hystory.lastIndex == 9) {
                hystory.removeAt(9)
            }
            return build(commandName)

        }

    }

}
















