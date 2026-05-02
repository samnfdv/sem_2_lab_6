package org.example.commands

import mechanicsOfCollection.CollectionManager
import org.example.dataBase.DataBaseManager

class LogIn(
    val dataBaseManager: DataBaseManager,
    val collectionManager: CollectionManager
): Command {
    override fun execute(args: Array<Any>?): String {
        if (args == null) return "Логин и пароль не могут быть null"
        if (args.size != 1) return "Недостаточно аргументов"
        val data = args[0] as Array<*>
        val login = data[0]as String
        val password = data[1] as String
        val response = dataBaseManager.authenticate(login, password)
        if (response.isSuccess) {
            collectionManager.loadCollection(login)

            return "Приветствую " + login
        } else {
            return "Неверный логин или пароль!"
        }
    }
}