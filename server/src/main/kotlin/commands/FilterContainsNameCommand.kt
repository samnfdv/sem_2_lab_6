package org.example.commands

import mechanicsOfCollection.CollectionManager
/**
 * Выполняет команду фильтрации элементов по имени.
 *
 * @param args массив аргументов, ожидается один элемент типа {@link String} — подстрока для поиска.
 * @return результат выполнения команды.
 */
class FilterContainsNameCommand(val collectionManager: CollectionManager) : Command {
    override fun execute(args: Array<Any>?): String {
        if (collectionManager.isCollectionEmpty()) {
            return "Ошибка: Коллекция пуста."
        }

        return "Отфильтрованые элементы " + collectionManager.filterCollectionByName(args?.get(0) as String)
    }
}