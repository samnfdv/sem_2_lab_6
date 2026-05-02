package org.example.commands

import mechanicsOfCollection.CollectionManager
import org.example.dataBase.DataBaseManager

/**
 * Команда для удаления элемента коллекции по его ID.
 */
class RemoveByIdCommand(val collectionManager: CollectionManager,
    val dataBaseManager: DataBaseManager) : Command {
    /**
     * Выполняет команду удаления элемента по ID.
     *
     * @param args массив аргументов, ожидается один элемент типа {@link Long} — ID элемента.
     * @return результат выполнения команды (успех или ошибка).
     */
    override fun execute(args: Array<Any>?): String {
        if (args == null || args.size != 1) return "Ошибка: неверное кол-во аргументов"
        val inner = args[0]
        if (inner !is Array<*>) return "Ошибка: неверный формат аргументов."

        if (inner.size != 2 || inner[0] !is Long || inner[1] !is String) {
            return "Ошибка: ожидаются [Long id, String login]."
        }

        val id = inner[0] as Long
        val login = inner[1] as String
        collectionManager.loadCollection(login)
        val removed: Boolean = collectionManager.removeVehicle(id, login)
        if (removed) {
            dataBaseManager.removeObject(id, login)
        }
        return if (removed)
            "Элемент с ID $id удалён."
        else
            "Элемент с ID $id не найден."

    }
}
