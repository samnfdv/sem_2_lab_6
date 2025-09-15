package mechanicsOfCollection
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import dataForCollection.Vehicle
import outerLayer.OutputManager
import java.time.LocalDateTime
import java.util.LinkedHashSet
/**
 * Класс для управления чтением данных из коллекции и записью данных в коллекцию.
 *
 */
class CollectionManager(
    private val collectionFileManager : CollectionFileManager,
    private val outputManager :OutputManager
    ) {
    private val LOGGER: Logger = LogManager.getLogger(CollectionManager::class.java)
    var collection: ArrayList<Vehicle> = collectionFileManager.readFromFile()
    private val initializationDate: LocalDateTime = LocalDateTime.now()
    /**
     * Получает коллекцию с данными из файла
     */
    fun getCollection() {
        collection = collectionFileManager.readFromFile()
        LOGGER.info("The collection is saved in an ArrayList from a file")
    }
    /**
     * Очищает коллекцию
     */
    fun clearCollection() {
        collection.clear()
        LOGGER.info("The collection has been cleared")
    }
    /**
     * Вызывает [collectionFileManager] для записи коллекции в файл
     */
    fun writeCollection(){
        collectionFileManager.writeToFile(collection)
        LOGGER.info("The collection is written from arraylist to a file")
    }
    /**
     * Выводит информацию о коллекции
     */
    fun infoCollection(){
        outputManager.surePrint("Информация о коллекции:\n" +
                "Тип: ${collection.javaClass.simpleName}<${Vehicle::class.simpleName}>\n" +
                "Дата инициализации: $initializationDate\n" +
                "Кол-во элементов: ${collection.size}")
        LOGGER.info("Information about collection  shown")
    }

    /**
     * Выводит элементы коллекции
     */
    fun showCollection() {
        outputManager.surePrint(collection.joinToString("\n") { it.toString() })
        LOGGER.info("The collection is shown from the arraylist")
    }
    /**
     * Добовляет элемент в коллекцию
     */
    fun addVehicle(vehicle: Vehicle) {
        collection.add(vehicle)
        LOGGER.info("The vehicle has been added to the arraylist")
    }

    fun removeVehicle(vehicle: Vehicle) {
        collection.remove(vehicle)
        LOGGER.info("The vehicle $vehicle has been removed from the arraylist")
    }

}