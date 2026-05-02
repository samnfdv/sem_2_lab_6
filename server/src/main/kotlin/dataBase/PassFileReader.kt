package org.example.dataBase

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import outerLayer.OutputManager
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

class PassFileReader(
    val outputManager: OutputManager
) {
    private val LOGGER: Logger = LogManager.getLogger(PassFileReader::class.java)
    lateinit var user1: String
    lateinit var password: String

    fun readF(): Array<String>? {
        val filePath = System.getenv("PASS_PATH")
        if (filePath == null || filePath.isBlank()) {
            LOGGER.warn("Переменная окружения не найдена, подключение к базе данных невозможно!")
            outputManager.surePrint("Переменная окружения не найдена, подключение к базе данных невозможно!")
            System.exit(0)
        }

        try {
            val path = Paths.get(filePath)
            if (!Files.exists(path)) {
                LOGGER.warn("Файл не найден:  $filePath  \n подключение к базе данных невозможно!")
                outputManager.surePrint("Файл не найден:  $filePath  \n подключение к базе данных невозможно!")
                System.exit(0)
            }
            val tmp = Files.readString(path, StandardCharsets.UTF_8).split(" ")
            if (tmp.size != 2) {
                LOGGER.warn("Неверный формат файла с паролем")
                System.exit(0);
            }

            user1 = stripBom(tmp[0].trim())!!
            password = stripBom(tmp[1].trim())!!
            return arrayOf(user1, password)
        } catch (e: IOException) {
            LOGGER.warn("Ошибка чтения файла")
            System.exit(0)
            return null
        }


    }

    private fun stripBom(s: String?): String? {
        return s?.takeIf { it.isNotEmpty() && it[0] == '\uFEFF' }?.substring(1) ?: s
    }


}