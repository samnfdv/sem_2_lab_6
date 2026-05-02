package org.example.commands


import dataForCollection.VehicleType
import mechanicsOfCollection.CollectionManager
import org.example.dataBase.DataBaseManager
import org.example.dataForCollection.Vehicle

import org.example.dataForCollection.VehicleArgsPack

/**
 * Команда для обновления элемента коллекции по его ID.
 * Требует два аргумента: ID элемента и новый объект VehicleArgsPack.
 */
class UpdateByIdCommand(
    val collectionManager: CollectionManager,
    val dataBaseManager: DataBaseManager
) : Command {
    /**
     * Выполняет обновление элемента по ID.
     *
     * @param args массив аргументов, ожидается два элемента:
     *             [0] — ID элемента типа {@link Long},
     *             [1] — новый объект {@link VehicleArgsPack}.
     * @return результат выполнения команды.
     */
    override fun execute(args: Array<Any>?): String {
        if (args == null || args.size != 2) {
            return "Ошибка ожидалось два аргумента id и vehicle"
        }


        val id = args[0] as Long
        val vehArg = args[1] as VehicleArgsPack
        try {
            if (collectionManager.getVehicle(id).userLogin.equals(vehArg.userLogin)) {

                dataBaseManager.updateObject(id, vehArg, vehArg.userLogin, collectionManager)
                val j = collectionManager.updateId(id, vehArg, vehArg.userLogin)
                if (j) return "Элемент с ID $id обновлен."
                else return "Ошибка объект с ID $id не обновлен."
            } else return "Ошибка объект вам не принадлежит"

        } catch (e: IllegalArgumentException) {
            return "Ошибка при обновлении ${e.message}"
        }
    }
}