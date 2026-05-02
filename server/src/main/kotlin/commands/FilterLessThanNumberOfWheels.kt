package org.example.commands

import mechanicsOfCollection.CollectionManager
/**
 * Команда для фильтрации элементов коллекции по количеству колёс,
 * выбираются элементы, у которых numberOfWheels меньше указанного значения.
 */
class FilterLessThanNumberOfWheels(val collectionManager: CollectionManager): Command {
    /**
     * Выполняет команду фильтрации элементов по количеству колёс.
     *
     * @param args массив аргументов, ожидается один элемент типа {@link Int} — пороговое количество колёс.
     * @return результат выполнения команды.
     */
    override fun execute(args: Array<Any>?): String {
        if (collectionManager.isCollectionEmpty()) return "Колекция пуста вывод не возможен"

        val wheels = args?.get(0) as Int?
        if (wheels == null) return "Ошибка wheels не могут быть null"
        return collectionManager.filterCollectionByWheel(wheels)
    }
}