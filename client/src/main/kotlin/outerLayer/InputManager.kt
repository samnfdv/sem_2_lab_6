package outerLayer


import exceptions.InvalidInputException
import java.io.File
import java.io.FileReader
import java.io.InputStream
import java.util.*
/**
 * Менеджер ввода, отвечающий за обработку данных из консоли или скрипта.
 * Поддерживает переключение между интерактивным и скриптовым режимами,
 * предотвращает рекурсивное выполнение скриптов.
 *
 * @property outputManager Менеджер вывода, используемый для отображения информации.
 */
class InputManager(private val outputManager: OutputManager) {
    private var scanners:Stack<Scanner> = Stack()
    private var inputStream = System.`in`
    private var scriptMode = false
    private var files:Stack<File> = Stack()
    private var pausedScriptScanner: Scanner? = null
    /**
     * Конструктор с заданным входным потоком.
     *
     * @param inputStream Входной поток, например, System.`in` или файл.
     * @param outputManager Менеджер вывода.
     */
    constructor(
        inputStream: InputStream,
        outputManager: OutputManager
    ): this(outputManager){
        this.inputStream = inputStream
    }

    init{
        scanners.push(Scanner(inputStream))
    }
    /**
     * Считывает строку из текущего источника (консоль или скрипт).
     *
     * @return Следующая строка ввода.
     * @throws InvalidInputException Если ввод завершён и не в режиме скрипта.
     */
    fun read(): String{
        return if (scanners.peek().hasNextLine()) {
            scanners.peek().nextLine()
        } else {
            if (scriptMode) {
                finishScriptRead()
                ""
            }else{
                throw InvalidInputException()
            }
        }
    }
    /**
     * Запускает выполнение скрипта из файла.
     *
     * @param filePath Путь к скриптовому файлу.
     * @throws IllegalStateException При попытке рекурсивного вызова скрипта.
     */

    fun startScriptRead(filePath: String){
        val file = File(filePath)
        try {

            if (file in files){
                outputManager.surePrint("Файл уже выполняется.")
                throw IllegalStateException("Рекурсия обнаружена: файл ${file.name} уже выполняется.")
            }else{
                outputManager.println("Запущено выполнение скрипта из файла ${file.name}")
                scanners.push(Scanner(FileReader(file)))
                files.push(file)
                outputManager.disableOutput()
                scriptMode = true

            }
        }catch (e: Exception){outputManager.surePrint(e.message.toString())}
    }
    /**
     * Завершает выполнение текущего скрипта, возвращая предыдущий режим.
     */
    fun finishScriptRead(){
        scriptMode = false
        scanners.pop()
        outputManager.enableOutput()
        outputManager.println("Скрипт из файла был выполнен")
        files.pop()
    }
    /**
     * Проверяет, активен ли режим скрипта.
     *
     * @return `true` если сейчас исполняется скрипт.
     */
    fun isScriptMode(): Boolean = scriptMode


}