package org.example.dataBase

import dataForCollection.Coordinates
import dataForCollection.FuelType
import dataForCollection.VehicleType
import mechanicsOfCollection.CollectionManager
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.example.Response
import org.example.User
import org.example.dataForCollection.Vehicle
import org.example.dataForCollection.VehicleArgsPack
import outerLayer.OutputManager
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.time.ZonedDateTime
import java.util.concurrent.locks.ReentrantLock


class DataBaseManager(
    val outputManager: OutputManager
) {
    private val queryManager = QueryManager()
    private val passwordManager = PasswordManager()
    private val lock = ReentrantLock()
    private val LOGGER: Logger = LogManager.getLogger(DataBaseManager::class.java)
    private var passFileReader = PassFileReader(outputManager)


    fun connect(): Connection? {

        try {
            Class.forName("org.postgresql.Driver")
            val dt = passFileReader.readF() ?: return null
            val j = DriverManager.getConnection(
                "jdbc:postgresql://localhost:25432/studs",
                dt[0],
                dt[1]
            )
            LOGGER.info("Подключен к базе данных: $j")
            println("Подключился к базе данных")
            return j
        } catch (e: SQLException) {
            LOGGER.warn("Ошибка при подключении к базе данных: " + e.message)
            return null
        } catch (e: ClassNotFoundException) {
            LOGGER.warn("Ошибка при подключении к базе данных: " + e.message)
            return null
        }
    }

    fun loadCache(user_login: String?, collection: ArrayList<Vehicle>): ArrayList<Vehicle> {
        lock.lock()
        try {
            connect()?.use { connection ->
                val query =
                    if (user_login == null) queryManager.selectAllObjects
                    else "${queryManager.selectAllObjects} WHERE user_login = ?"

                connection.prepareStatement(query).use { selectAll ->
                    if (user_login != null) {
                        selectAll.setString(1, user_login)
                    }

                    selectAll.executeQuery().use { resultSet ->
                        while (resultSet.next()) {
                            val ts = resultSet.getTimestamp("creationdate")
                            val creationDate = ts?.toInstant()?.atZone(java.time.ZoneId.systemDefault())

                            val vehicle = Vehicle(
                                resultSet.getLong("id"),
                                creationDate!!,
                                resultSet.getString("user_login"),
                                resultSet.getString("name"),
                                Coordinates(resultSet.getFloat("corX"), resultSet.getInt("corY")),
                                resultSet.getLong("enginePower"),
                                resultSet.getInt("numberOfWheels"),
                                resultSet.getString("type")?.takeIf { it.isNotBlank() }
                                    ?.let { VehicleType.valueOf(it) },
                                resultSet.getString("fuelType")?.takeIf { it.isNotBlank() }
                                    ?.let { FuelType.valueOf(it) }
                            )
                            collection.add(vehicle)
                        }
                    }
                }

                LOGGER.info("Загружено ${collection.size} объектов из базы данных${if (user_login != null) " для пользователя $user_login" else ""}")
                outputManager.surePrint("Загружено ${collection.size} объектов из базы данных${if (user_login != null) " для пользователя $user_login" else ""}")
                return collection
            }

            LOGGER.warn("Не удалось подключиться к базе данных для загрузки кэша")
            outputManager.surePrint("Не удалось подключиться к базе данных для загрузки кэша")
            return collection
        } catch (e: SQLException) {
            LOGGER.error("Ошибка при загрузке кэша: ${e.message}")
            outputManager.surePrint("Ошибка при загрузке кэша: ${e.message}")
            return collection
        } finally {
            lock.unlock()
        }
    }

    fun registration(user: User): Response {
        lock.lock()
        try {
            connect()?.use { connection ->
                connection.prepareStatement(queryManager.findUser).use { findUser ->
                    findUser.setString(1, user.login)
                    findUser.executeQuery().use { resultSet ->
                        if (!resultSet.next()) {
                            connection.prepareStatement(queryManager.addUser).use { addUser ->
                                addUser.setString(1, user.login)
                                addUser.setString(2, passwordManager.hashPassword(user.password!!))
                                addUser.execute()
                                return Response(true, "Регистрация прошла успешно.")
                            }
                        } else {
                            return Response(false, "Пользователь с таким логином уже существует.")
                        }
                    }
                }
            }
            return Response(false, "Ошибка подключения к базе данных.")
        } catch (e: SQLException) {
            LOGGER.warn("Ошибка при регистрации: ${e.message}")
            return Response(false, "Ошибка подключения к базе данных.")
        } finally {
            lock.unlock()
        }
    }

    fun authenticate(login: String, password: String): Response {
        lock.lock()
        try {
            connect()?.use { connection ->
                connection.prepareStatement(queryManager.findUser).use { findUser ->
                    findUser.setString(1, login)
                    findUser.executeQuery().use { resultSet ->
                        if (resultSet.next()) {
                            val storedHash = resultSet.getString("password")
                            val inputHash = passwordManager.hashPassword(password)
                            if (storedHash != null && storedHash == inputHash) {
                                return Response(true, "Авторизация успешна.", login)
                            } else {
                                return Response(false, "Неверный пароль.")
                            }
                        } else {
                            return Response(false, "Пользователь не найден.")
                        }
                    }
                }
            }
            return Response(false, "Ошибка подключения к базе данных.")
        } catch (e: SQLException) {
            LOGGER.warn("Ошибка при авторизации: ${e.message}")
            return Response(false, "Ошибка подключения к базе данных.")
        } finally {
            lock.unlock()
        }
    }

    fun addObject(vehicle: Vehicle, login: String, collectionManager: CollectionManager): Response {
        lock.lock()
        try {
            vehicle.userLogin = login
            connect()?.use { connection ->
                connection.prepareStatement(queryManager.addVehicle).use { add ->
                    add.setString(1, vehicle.name)
                    add.setFloat(2, vehicle.coordinates.x)
                    add.setInt(3, vehicle.coordinates.y)
                    add.setLong(4, vehicle.enginePower)
                    add.setInt(5, vehicle.numberOfWheels)
                    add.setString(6, vehicle.type?.name ?: "")
                    add.setString(7, vehicle.fuelType?.name ?: "")
                    // 8 — user_login, 9 — creationdate (вставляем в правильном порядке)
                    add.setString(8, vehicle.userLogin)
                    // Если у вас колонка TIMESTAMP WITH TIME ZONE и драйвер поддерживает ZonedDateTime:
                    val creationTimestamp = java.sql.Timestamp.from(vehicle.creationDate.toInstant())
                    add.setTimestamp(9, creationTimestamp)
                    add.executeQuery().use { resultSet ->
                        if (resultSet.next()) {
                            vehicle.id = resultSet.getLong("id")
                            collectionManager.addVehicle(vehicle)
                            return Response(true, "Элемент успешно добавлен: $vehicle")
                        }
                    }
                }
            }
            return Response(false, "Не удалось добавить объект.")
        } catch (e: SQLException) {
            LOGGER.warn("Ошибка при добавлении объекта: ${e.message}")
            return Response(false, "Ошибка при добавлении объекта. ${e.message}")
        } finally {
            lock.unlock()
        }
    }


    fun updateObject(
        id: Long,
        newVehicle: VehicleArgsPack,
        login: String,
        collectionManager: CollectionManager
    ): Response {
        lock.lock()
        try {
            connect()?.use { connection ->
                connection.prepareStatement(queryManager.selectObject).use { checkOwner ->
                    checkOwner.setString(1, login)
                    checkOwner.setLong(2, id)
                    checkOwner.executeQuery().use { resultSet ->
                        if (!resultSet.next()) {
                            return Response(false, "Объект не найден или не принадлежит пользователю.")
                        }
                    }
                }
                connection.prepareStatement(queryManager.updateObject).use { update ->
                    update.setString(1, newVehicle.name)
                    update.setFloat(2, newVehicle.coordinates.x)
                    update.setInt(2, newVehicle.coordinates.y)
                    update.setLong(3, newVehicle.enginePower)
                    update.setInt(4, newVehicle.numberOfWheels)
                    update.setString(5, newVehicle.type?.name ?: "")
                    update.setString(6, newVehicle.fuelType?.name ?: "")
                    update.setString(7, login)
                    update.setLong(8, id)
                    update.executeQuery().use { resultSet ->
                        if (resultSet.next()) {
                            collectionManager.updateId(id, newVehicle, login)
                            return Response(true, "Элемент с ID $id обновлён.")
                        }
                    }
                }
            } ?: return Response(false, "Ошибка подключения к базе данных.")
            return Response(false, "Не удалось обновить объект.")
        } catch (e: SQLException) {
            LOGGER.warn("Ошибка при обновлении объекта: ${e.message}")
            return Response(false, "Ошибка при обновлении объекта.${e.message}")
        } finally {
            lock.unlock()
        }

    }

    fun removeObject(id: Long, login: String): Response {
        lock.lock()
        try {
            connect()?.use { connection ->
                connection.prepareStatement(queryManager.deleteObject).use { remove ->
                    remove.setString(1, login)
                    remove.setLong(2, id)
                    remove.executeQuery().use { resultSet ->
                        if (resultSet.next()) {
                            return Response(true, "Элемент с ID $id удалён.")
                        }
                    }
                }
            } ?: return Response(false, "Ошибка подключения к базе данных.")
            return Response(false, "Объект не найден или не принадлежит пользователю.")
        } catch (e: SQLException) {
            LOGGER.warn("Ошибка при удалении объекта: ${e.message}")
            return Response(false, "Ошибка при удалении объекта.")
        } finally {
            lock.unlock()
        }
    }

    fun clear(login: String, collectionManager: CollectionManager): Response {
        lock.lock()
        try {
            connect()?.use { connection ->
                connection.prepareStatement(queryManager.clearCollection).use { clear ->
                    clear.setString(1, login)
                    clear.executeQuery().use { resultSet ->
                        var count = 0
                        while (resultSet.next()) {
                            count++
                        }
                        collectionManager.clearCollection(login)
                        return Response(true, "Удалено объектов: $count")
                    }
                }
            } ?: return Response(false, "Ошибка подключения к базе данных.")
        } catch (e: SQLException) {
            LOGGER.warn("Ошибка при очистке коллекции: ${e.message}")
            return Response(false, "Ошибка при очистке коллекции.")
        } finally {
            lock.unlock()
        }
    }

        fun removeGreater(login: String, enginePower : Long, collectionManager : CollectionManager ): Response {
        lock.lock()
        try {
            connect()?.use { connection ->
                connection.prepareStatement(queryManager.removeGreater).use { removeGreater ->
                    removeGreater.setString(1, login)
                    removeGreater.setLong(2, enginePower)
                    removeGreater.executeQuery().use { resultSet ->
                        if (resultSet.next()) {
                            val id = resultSet.getLong("id")
                            val s = collectionManager.removeGreater(enginePower, login)
                            return Response(true, s)
                        }
                    }
                }
            } ?: return Response(false, "Ошибка подключения к базе данных.")
            return Response(false, "Коллекция пуста.")
        } catch (e: SQLException) {
            LOGGER.warn("Ошибка при удалении элементов: ${e.message}")
            return Response(false, "Ошибка при удалении первого элемента.")
        } finally {
            lock.unlock()
        }
    }


    fun addIfMin(vehicle: Vehicle, login: String, collectionManager: CollectionManager): Response {
        lock.lock()
        try {
            if (collectionManager.getMinVehicle().enginePower >= vehicle.enginePower) {
                return addObject(vehicle, login, collectionManager)
            }
            return Response(false, "Мощность двигателя объекта не минимальная.")
        } finally {
            lock.unlock()
        }
    }
}
