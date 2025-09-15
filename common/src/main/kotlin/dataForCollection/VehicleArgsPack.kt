package org.example

import dataForCollection.Coordinates
import dataForCollection.FuelType
import dataForCollection.VehicleType

class VehicleArgsPack(
    val name: String,
    val coordinates: Coordinates,
    val enginePower: Long,
    val numberOfWheels: Int,
    val type: VehicleType?,
    val fuelType: FuelType?
) {
    fun getName(): String {
        return name
    }
    fun getCoordinates(): Coordinates {
        return coordinates
    }
    fun getEnginePower(): Long {
        return enginePower
    }
    fun getNumberOfWheels(): Int {
        return numberOfWheels
    }
    fun getType(): VehicleType? {
        return type
    }
    fun getFuelType(): FuelType? {
        return fuelType
    }
}