package org.example

import dataForCollection.Reader

import mechanicsOfCollection.CollectionManager
import org.example.dataBase.DataBaseManager
import outerLayer.InputManager
import outerLayer.OutputManager

fun main() {
    val outputManager = OutputManager()
    val dataBaseManager = DataBaseManager(outputManager)
    val collectionManager = CollectionManager(dataBaseManager, outputManager)
    val inputManager = InputManager(outputManager)
    val reader = Reader()
    val vehicleAdder = VehicleAdder(outputManager,inputManager, reader )
    val dispatcher = CommandDispatcher(collectionManager, dataBaseManager)
    val server = ServerApp(collectionManager, dispatcher)
    server.run()
}