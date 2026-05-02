package org.example.commands

import mechanicsOfCollection.CollectionManager
import org.example.dataBase.DataBaseManager

/**
 * Команда для удаления всех элементов коллекции,
 * у которых значение enginePower больше указанного.
 */
class RemoveGreaterCommand(
    val collectionManager: CollectionManager,
    val dataBaseManager: DataBaseManager
) : Command {
    /**
     * Выполняет команду удаления элементов с enginePower больше указанного.
     *
     * @param args массив аргументов, ожидается один элемент типа {@link Long} — пороговое значение enginePower.
     * @return результат выполнения команды.
     */
    override fun execute(args: Array<Any>?): String {
        if (collectionManager.isCollectionEmpty()) return "Коллекция пуста, удаление не возможно"
        if (args == null || args.size != 1) return "Ошибка: неверное кол-во аргументов"
        val inner = args[0]
        if (inner !is Array<*>) return "Ошибка: неверный формат аргументов."

        if (inner.size != 2 || inner[0] !is Long || inner[1] !is String) {
            return "Ошибка: ожидаются [Long id, String login]."
        }

        val enginePower = inner[0] as Long
        val login = inner[1] as String
        val response = dataBaseManager.removeGreater(login, enginePower, collectionManager);
        return response.message!!
    }
}