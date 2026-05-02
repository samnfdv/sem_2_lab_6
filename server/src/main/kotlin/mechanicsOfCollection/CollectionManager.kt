package mechanicsOfCollection

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.example.dataBase.DataBaseManager
import org.example.dataForCollection.Vehicle
import org.example.dataForCollection.VehicleArgsPack
import outerLayer.OutputManager
import java.time.LocalDateTime
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.math.log

/**
 * Класс для управления чтением данных из коллекции и записью данных в коллекцию.
 *
 */
class CollectionManager(
    private val dataBaseManager: DataBaseManager,
    private val outputManager: OutputManager
) {
    private val LOGGER: Logger = LogManager.getLogger(CollectionManager::class.java)
    var collection: ArrayList<Vehicle> = arrayListOf()
    private val initializationDate: LocalDateTime = LocalDateTime.now()
    private val lock = ReentrantReadWriteLock()

    /**
     * Получает коллекцию с данными из файла
     */
    fun getCollection() {
        lock.writeLock().lock()
        try {
            collection = dataBaseManager.loadCache(null, collection)

            LOGGER.info("Коллекция загружена для пользовател")

        } finally {
            lock.writeLock().unlock()
        }

    }

    fun loadCollection(login: String) {
        lock.writeLock().lock()
        try {
            collection.clear()
            collection = dataBaseManager.loadCache(login, collection)
            LOGGER.info("Коллекция загружена для пользователя: " + login);
        } finally {
            lock.writeLock().unlock()
        }
    }

    /**
     * Очищает коллекцию
     */
    fun clearCollection(login: String): String {
        lock.writeLock().lock()
        try {
            collection.removeIf { it.userLogin.equals(login) }
            LOGGER.info("The collection has been cleared")
            return "Коллекция очищена"
        } finally {
            lock.writeLock().unlock()
        }
    }

    /**
     * Вызывает [collectionFileManager] для записи коллекции в файл
     */
//    fun writeCollection() {
//        collectionFileManager.writeToFile(collection)
//        LOGGER.info("The collection is written from arraylist to a file")
//    }

    /**
     * Выводит информацию о коллекции
     */
    fun infoCollection(): String {
        lock.readLock().lock()
        try {
            LOGGER.info("Information about collection  shown")
            return "Информация о коллекции:\n" +
                    "Тип: ${collection.javaClass.simpleName}<${Vehicle::class.simpleName}>\n" +
                    "Дата инициализации: $initializationDate\n" +
                    "Кол-во элементов: ${collection.size}"
        } finally {
            lock.readLock().unlock()
        }

    }

    fun getMinVehicle(): Vehicle {
        lock.readLock().lock()
        try {
            val vehicle = collection.stream().min(Comparator.comparingLong { it.enginePower }).get()
            return vehicle as Vehicle
        } finally {
            lock.readLock().unlock()
        }

    }


    fun getVehicle(id: Long): Vehicle {
        lock.readLock().lock()
        try {
            return collection.stream().filter { id == it.id }.toList()[0]
        } finally {
            lock.readLock().unlock()
        }
    }

    fun isCollectionEmpty(): Boolean {
        lock.readLock().lock()
        try {
            return collection.isEmpty()
        } finally {
            lock.readLock().unlock()
        }
    }

    fun updateId(id: Long, vehicle: VehicleArgsPack, login: String): Boolean {
        lock.writeLock().lock();
        try {
            val vehicles = collection.stream().filter { login.equals(it.userLogin) }.filter { id == it.id }.toList()
            if (vehicles.isEmpty()) return false
            val veh = vehicles[0]
            veh.name = vehicle.name
            veh.numberOfWheels = vehicle.numberOfWheels
            veh.enginePower = vehicle.enginePower
            veh.coordinates = vehicle.coordinates
            veh.fuelType = vehicle.fuelType
            veh.type = vehicle.type
            return true
        } finally {
            lock.writeLock().unlock();
        }

    }


    /**
     * Выводит элементы коллекции
     */
    fun showCollection(): String {
        lock.readLock().lock()
        try {
            LOGGER.info("The collection is shown from the arraylist")
            return collection.joinToString("\n") { it.toString() }
        } finally {
            lock.readLock().unlock()
        }

    }

    /**
     * Добовляет элемент в коллекцию
     */
    fun addVehicle(vehicle: Vehicle) {
        lock.writeLock().lock();
        try {
            collection.add(vehicle)
            LOGGER.info("The vehicle has been added to the arraylist")
        } finally {
            lock.writeLock().unlock();
        }
    }

    fun filterCollectionByName(str: String?): String {
        lock.readLock().lock()
        try {
            if (str == null) return ""
            val g = collection.stream().filter { it.name.contains(str) }.toList().joinToString("\n") { it.toString() }
            return g
        } finally {
            lock.readLock().unlock();
        }
    }

    fun filterCollectionByWheel(wheels: Int): String {
        lock.readLock().lock()
        try {
            val g =
                collection.stream().filter { wheels > it.numberOfWheels }.toList().joinToString("\n") { it.toString() }
            if (g.isEmpty()) return "Нет подходящих элементов"
            return g
        } finally {
            lock.readLock().unlock();
        }
    }

    fun HaveID(id: Long, login: String): Boolean {
        lock.readLock().lock()
        try {
            if (collection.isEmpty()) return false
            if (collection.stream().filter { id == it.id }.filter { login.equals(it.userLogin) }.toList()
                    .isNotEmpty()
            ) return true
            return false
        } finally {
            lock.readLock().unlock()
        }
    }

    fun removeGreater(enginePower: Long, login: String): String {
        lock.writeLock().lock()
        try {
            val remove =
                collection.stream().filter { it.userLogin.equals(login) }.filter { it.enginePower > enginePower }
                    .toList()
            if (remove.isEmpty()) return "Нету элементов больше заданого "
            collection.removeAll(remove.toSet())
            return "Удалено ${remove.size} элементов"
        } finally {
            lock.writeLock().unlock()

        }
    }

    fun printDescending(): String {
        lock.readLock().lock()
        try {
            if (collection.isEmpty()) return "Коллекция пуста"
            return collection.sortedDescending().joinToString("\n") { it.toString() }
        } finally {
            lock.readLock().unlock()
        }
    }


    fun removeVehicle(id: Long, login: String): Boolean {
        lock.writeLock().lock()
        try {
            if (collection.isEmpty()) return false
            val p = collection.stream().filter { it.userLogin.equals(login) }.filter { it.id == id }.toList()
            if (p.isEmpty()) return false
            collection.remove(p[0])
            LOGGER.info("The vehicle $id has been removed from the arraylist")
            return true
        } finally {
            lock.writeLock().unlock();
        }
    }

}