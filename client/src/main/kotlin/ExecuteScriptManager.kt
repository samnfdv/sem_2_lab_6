import commands.CommandExecutor
import outerLayer.InputManager
import outerLayer.OutputManager
import java.io.File
import java.lang.Exception

class ExecuteScriptManager(
    private val commandExecutor: CommandExecutor,
    private val outputManager: OutputManager,
    private val inputManager: InputManager
) {
    companion object {
        /**
         * Множество канонических путей к файлам, которые в данный момент выполняются.
         */
        private val executingScr = mutableSetOf<String>()
    }

    fun execute(fileName: String?): Any? {

        val srcPath = fileName ?: run {
            outputManager.print("Введите путь к файлу: ")
            inputManager.read()
        }

        val file = File(srcPath)
        if (!file.exists()) {
            outputManager.surePrint("Файл не найден.")
            return null
        }

        val canonicalPath = file.canonicalPath
        if (executingScr.contains(canonicalPath)) {
            outputManager.surePrint("Скрипт уже выполняется.")
            return null
        }

        executingScr.add(canonicalPath)

        if (!file.canRead()) {
            outputManager.surePrint("Нет прав на чтение данного файла.")
            return null
        }

        try {
            inputManager.startScriptRead(srcPath)

            var lineNumber = 0
            while (inputManager.isScriptMode()) {
                val line = inputManager.read()
                lineNumber++
                if (line.isEmpty()) {
                    if (!inputManager.isScriptMode()) break // Прерываем, если файл закончился
                    outputManager.println("Пустая строка пропущена (строка $lineNumber).")
                    return null
                }

                outputManager.println("Выполняется команда из скрипта (строка $lineNumber): $line")
                try {
                    return commandExecutor.executeCommand(line)
                } catch (e: Exception) {
                    outputManager.println("${e.message}")
                }
            }
        } catch (e: Exception) {
            outputManager.surePrint("Ошибка при чтении файла: ${e.message}")
            inputManager.finishScriptRead()
            return null
        } finally {

            executingScr.remove(canonicalPath)

        }
        return null
    }

}