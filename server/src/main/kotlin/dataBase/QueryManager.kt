package org.example.dataBase

class QueryManager {
    val findUser = "SELECT * FROM users WHERE login = ?"
    val addUser = "INSERT INTO users(login, password) VALUES (?, ?);"
    val addVehicle =
        "INSERT INTO vehicles(name, corX, corY, enginePower, numberOfWheels, type, fuelType, user_login, creationdate) VALUES (?, ?, ?, ?, ? , ?, ?, ?, ?) RETURNING id;"
    val clearCollection = "DELETE FROM vehicles WHERE user_login = ? RETURNING id;"
    val deleteObject = "DELETE FROM vehicles WHERE user_login = ? AND id = ? RETURNING id;"
    val removeGreater = "DELETE FROM vehicles WHERE user_login = ? AND enginepower < ? RETURNING id;"
    val selectAllObjects = "SELECT * FROM vehicles"
    val selectObject = "SELECT id, user_login FROM vehicles WHERE user_login = ? AND id = ?;"
    val updateObject = """
            UPDATE vehicles
            SET name = ?, corX = ?, corY = ?, enginePower = ?, numberOfWheels = ?, type = ?, fuelType = ?
            WHERE user_login = ? AND id = ?
            RETURNING id;
            """
}