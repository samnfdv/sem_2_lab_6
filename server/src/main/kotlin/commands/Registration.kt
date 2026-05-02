package org.example.commands

import mechanicsOfCollection.CollectionManager
import org.example.User
import org.example.dataBase.DataBaseManager
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
class Registration(
    val collectionManager: CollectionManager,
    val dataBaseManager: DataBaseManager
): Command {
    private val LOGGER: Logger = LogManager.getLogger(Registration::class.java)
    override fun execute(args: Array<Any>?): String {
        if (args == null) return "Логин и пароль не могут быть null"
        if (args.size != 2)return "Неверное кол-во аргументов"
//        val data = args[0] as Array<*>
        val login = args[0]as String
        val password = args[1] as String
        val r = dataBaseManager.registration(User(login, password, true)).message!!
        LOGGER.info("User registered: $login, Password: $password")
        return r
    }
}