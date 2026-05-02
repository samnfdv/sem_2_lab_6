package org.example.dataForCollection

import dataForCollection.Coordinates
import dataForCollection.FuelType
import dataForCollection.VehicleType
import java.io.Serializable

class VehicleArgsPack(
    val userLogin : String,
    val name: String,
    val coordinates: Coordinates,
    val enginePower: Long,
    val numberOfWheels: Int,
    val type: VehicleType?,
    val fuelType: FuelType?
) : Serializable {

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}