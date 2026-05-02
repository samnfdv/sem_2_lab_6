package org.example.commands

import mechanicsOfCollection.CollectionManager
import org.example.dataBase.DataBaseManager

class ClearCollection(
    val collectionManager: CollectionManager,
    val dataBaseManager: DataBaseManager
) : Command {
    override fun execute(args: Array<Any>?): String {
        if (args == null || args.size != 1 || args[0] !is String) {
            return "Ошибка: ожидался login."
        }
        val login = args[0] as String
        collectionManager.loadCollection(login)
        return dataBaseManager.clear(login, collectionManager).message!!
    }

}