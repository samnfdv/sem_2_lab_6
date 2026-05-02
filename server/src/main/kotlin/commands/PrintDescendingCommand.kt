package org.example.commands


import mechanicsOfCollection.CollectionManager
/**
 * Команда для вывода элементов коллекции в порядке убывания.
 * Не принимает аргументов.
 */
class PrintDescendingCommand(val collectioneManager: CollectionManager) : Command {
    /**
     * Выполняет команду печати элементов коллекции в порядке убывания.
     *
     * @param args массив аргументов, должен быть пустым.
     * @return результат выполнения команды или сообщение об ошибке.
     */
    override fun execute(args: Array<Any>?): String {
        if (args != null) {
            if (args.size > 0) return "PrintDescending не принимает аргументы"
            return collectioneManager.printDescending()
        }
        return "Ошибка "
    }
}