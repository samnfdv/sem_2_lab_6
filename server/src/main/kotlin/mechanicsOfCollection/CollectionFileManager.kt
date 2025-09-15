package mechanicsOfCollection





import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dataForCollection.Vehicle
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.*
import java.util.ArrayList
    /**
     * Класс для управления чтением и записью коллекции транспортных средств в файл.
     * @property FILEPATH Путь к файлу, используемому для чтения и записи данных.
     * Загружает данные из файла, указанного в переменной окружения CONFIG_FILE, или использует путь по умолчанию.
     */
class CollectionFileManager {
    private val MAPPER = jacksonObjectMapper().registerModule(JavaTimeModule())
    private val LOGGER: Logger = LogManager.getLogger(CollectionFileManager::class.java)
    /**
     * Путь к файлу для чтения и записи данных.
     * По умолчанию берётся из переменной окружения FILEPATH, иначе используется "objects.json".
     */

    private val FILEPATH: String = System.getenv("CONFIG_FILE") ?: run {
        println("Переменная окружения FILENAME не установлена. Используется значение по умолчанию 'objects.json'")
        "objects.json"
    }

        /**
         * Записывает данные в указанный файл
         * @param list коллекция из которой записываются данные в файл.
          */
    fun writeToFile(list: ArrayList<Vehicle>) {
        try {
            val json = MAPPER.writeValueAsString(list)
            FileWriter(FILEPATH, false).use { writer ->
                writer.write(json)
            }
            LOGGER.info("Collection was written to a file: {}", FILEPATH)
        } catch (e: IOException) {
            LOGGER.error("Failed to write collection to file: {}", e.message)
        }
    }
        /**
         * Записывает данные из указанного файла в коллекцию
         *@return Заполненая коллекция.
         */
    fun readFromFile(): ArrayList<Vehicle> {
        val file = File(FILEPATH)
        if (!file.exists()) {
            LOGGER.error("File does not exist: {}", FILEPATH)
            return ArrayList()
        }

        try {
            BufferedReader(FileReader(file)).use { reader ->
                val content = reader.readText()
                if (content.isBlank()) {
                    LOGGER.error("Collection was not read from the file, because the file is empty: {}", FILEPATH)
                    return ArrayList()
                }
                LOGGER.info("Collection was read from the file: {}", FILEPATH)
                return MAPPER.readValue(content)
            }
        } catch (e: IOException) {
            LOGGER.error("Failed to read collection from file: {}", e.message)
            return ArrayList()
        }
    }
}

