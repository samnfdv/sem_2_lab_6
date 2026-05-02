package org.example.commands

import mechanicsOfCollection.CollectionManager
import sun.security.jgss.GSSUtil.login


class ShowCommand(val collectionManager: CollectionManager) : Command {
    override fun execute(args: Array<Any>?): String {
        if (args == null || args.isEmpty()) {
            collectionManager.getCollection()
            return collectionManager.showCollection()
        }

        val login = args[0] as? String
            ?: return "Ошибка: ожидался логин"
        collectionManager.loadCollection(login)
        return collectionManager.showCollection()
    }
}
