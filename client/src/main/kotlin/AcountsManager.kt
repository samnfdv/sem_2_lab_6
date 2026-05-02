import org.example.Reader
import outerLayer.InputManager
import outerLayer.OutputManager
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.example.CommandType
import org.example.Request

class AcountsManager(
    val inputManager: InputManager,
    val outputManager: OutputManager,
    private val reader: Reader
) {
    private val LOGGER: Logger = LogManager.getLogger(AcountsManager::class.java)
    fun getLogInData(data : String): String {
        while (true) {
            try {
                outputManager.print("Введите $data: ")

                val str = reader.readLineTrimmed(inputManager.read())

                if (str.isBlank()) {
                    outputManager.println(
                        """Ошибка: $data введено не корректно.
                        |$data не может быть пустой строкой.
                    """.trimMargin()
                    )
                    LOGGER.error("Error: the $data was entered incorrectly blank.")
                    continue
                }
                LOGGER.info("The $data={$str} was read")
                return str
            } catch (exception: Exception) {
                outputManager.println("Ошибка: ${exception.message ?: "Неизвестная ошибка"}")
                LOGGER.error("Error:${exception.message ?: " Unknown error."}")
            }
        }
    }

    fun login(): Array<String> {
        outputManager.println("Вход в аккаунт:")
        val login = getLogInData("login")
        val password = getLogInData("password")
        return arrayOf(login, password)
    }

    fun registration():Array<Any>{
        outputManager.println("Регистрация:")
        val login = getLogInData("login")
        val password = getLogInData("password")
        return  arrayOf(login, password)
    }

}