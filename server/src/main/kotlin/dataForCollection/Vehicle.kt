package org.example.dataForCollection

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import dataForCollection.Coordinates
import dataForCollection.FuelType
import dataForCollection.VehicleType
import java.io.Serializable
import java.time.ZonedDateTime

/**
 * Класс, представляющий транспортное средство.
 * Используется в коллекции и сериализуется через Jackson.
 *
 * @property id Уникальный идентификатор, генерируется автоматически, > 0.
 * @property creationDate Дата создания объекта, устанавливается автоматически.
 * @property name Название транспортного средства (не может быть пустым).
 * @property coordinates Координаты транспортного средства (не null).
 * @property enginePower Мощность двигателя (> 0).
 * @property numberOfWheels Количество колёс (> 0).
 * @property type Тип транспортного средства (может быть null).
 * @property fuelType Тип топлива (может быть null).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Vehicle @JsonCreator constructor(
    @JsonProperty("id")
    var id: Long,//Поле не может быть null, Значение поля должно быть больше 0, Значение этого поля должно быть
    // уникальным, Значение этого поля должно генерироваться автоматически


    @JsonProperty("creationDate")
    val creationDate: ZonedDateTime,//Поле не может быть null, Значение этого поля должно генерироваться автоматически

    @JsonProperty("user_login")
    var userLogin: String,

    @JsonProperty("name")
    var name: String,  //Поле не может быть null, Строка не может быть пустой

    @JsonProperty("coordinates")
    var coordinates: Coordinates,//Поле не может быть null

    @JsonProperty("enginePower")
    var enginePower: Long, // Значение поля должно быть больше 0

    @JsonProperty("numberOfWheels")
    var numberOfWheels: Int, //Значение поля должно быть больше 0

    @JsonProperty("type")
    var type: VehicleType?,//Поле может быть null

    @JsonProperty("fuelType")
    var fuelType: FuelType?//Поле может быть null
) : Comparable<Vehicle>, Serializable {


    companion object {
        @JsonIgnore
        private const val serialVersionUID: Long = 1L

        /**
         * Генератор уникальных ID на основе Snowflake.
         */
        @JsonIgnore
        private val IDGENERATOR = SnowflakeIdGenerator(1)

        /**
         * Создаёт новый объект [Vehicle] с автогенерацией id и даты создания.
         */
        fun createNew(
            userLogin: String,
            name: String,
            coordinates: Coordinates,
            enginePower: Long,
            numberOfWheels: Int,
            type: VehicleType?,
            fuelType: FuelType?
        ): Vehicle {
            return Vehicle(
                id = IDGENERATOR.nextId(),
                creationDate = ZonedDateTime.now(),
                userLogin = userLogin,
                name = name,
                coordinates = coordinates,
                enginePower = enginePower,
                numberOfWheels = numberOfWheels,
                type = type,
                fuelType = fuelType
            )
        }
    }

    override fun compareTo(other: Vehicle): Int {
        return this.enginePower.compareTo(other.enginePower)
    }
}