package org.example.commands

import mechanicsOfCollection.CollectionManager

class CheckIdCommand(val collectionManager: CollectionManager): Command {
    override fun execute(args: Array<Any>?): String {
        if (args == null || args.size != 1) return "Ошибка: y=неверное кол-во аргументов"
        val inner = args[0]
        if (inner !is Array<*>) return "Ошибка: неверный формат аргументов."

        if (inner.size != 2 || inner[0] !is Long || inner[1] !is String) {
            return "Ошибка: ожидаются [Long id, String login]."
        }

        val id = inner[0] as Long
        val login = inner[1] as String
        if (collectionManager.HaveID(id, login) ) return "Найден id $id"
        return "Id $id не найден"
    }
    fun getId(arg: Array<Any>): Any? {
        return arg[0]
    }

}