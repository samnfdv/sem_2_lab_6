package outerLayer


import exceptions.InvalidOutputException
import java.io.IOException
import java.io.OutputStream
/**
 * Менеджер вывода, управляющий направлением вывода и его режимами.
 */
class OutputManager() {
    private var outputStream: OutputStream = System.out
    private var outputMode = OutputMode.ACTIVE
    /**
     * Режимы вывода:
     * - [ACTIVE]: активный вывод
     * - [SILENT]: вывод отключён
     */
    private enum class OutputMode(){
        SILENT, ACTIVE
    }
    /**
     * Создаёт менеджер вывода с заданным потоком.
     *
     * @param outputStream Поток, в который производится вывод.
     */
    constructor(outputStream: OutputStream): this(){
        this.outputStream = outputStream
    }
    /**
     * Печатает строку в текущий поток, если вывод включён.
     *
     * @param string Строка для вывода.
     */
    fun print(string: String){
        try{
            if (outputMode == OutputMode.ACTIVE){
                outputStream.write(string.toByteArray())
            }
        }catch(e: IOException){
            println(e.message)
        }
    }
    /**
     * Печатает строку с переходом на новую строку, если вывод включён.
     *
     * @param string Строка для вывода.
     */
    fun println(string: String){
        try{
            if(outputMode == OutputMode.ACTIVE){
                outputStream.write(string.toByteArray())
                outputStream.write("\n".toByteArray())
            }
        } catch (e: IOException){
            println(e.message)
        }
    }
    /**
     * Печатает строку независимо от текущего режима вывода.
     * Используется для критически важных сообщений.
     *
     * @param string Строка для вывода.
     */
    fun surePrint(string: String){
        try{
            outputStream.write(string.toByteArray())
            outputStream.write("\n".toByteArray())
        }catch (e: IOException){
            println(e.message)
        }
    }
    /**
     * Отключает вывод (переход в режим SILENT).
     */
    fun disableOutput(){
        outputMode = OutputMode.SILENT

    }
    /**
     * Включает вывод (переход в режим ACTIVE).
     */
    fun enableOutput(){
        outputMode = OutputMode.ACTIVE

    }
}