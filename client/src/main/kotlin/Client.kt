package org.example

import outerLayer.InputManager
import outerLayer.OutputManager


fun main() {
    val outputManager = OutputManager()
    val inputManager = InputManager(outputManager)
    val reader = Reader()
    val client = ClientApp(outputManager,inputManager, reader)
    client.run()
}