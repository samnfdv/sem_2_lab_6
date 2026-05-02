package org.example

import java.io.Serial
import java.io.Serializable


/**
 * Объект-ответ сервера клиенту.
 * Сериализуется и отправляется по сети через ObjectOutputStream.
 */
class Response : Serializable {
    /* ---------- Геттеры / Сеттеры ---------- */
    /** Флаг успеха выполнения команды  */
    var isSuccess: Boolean = false

    /** Читаемое сообщение (например, результат или ошибка)  */
    var message: String? = null

    /**
     * Дополнительные данные ответа.
     * Например, для команды SHOW здесь может лежать List<Person>.
    </Person> */
    var data: Any? = null

    /* ---------- Конструкторы ---------- */
    /** Пустой конструктор для десериализации  */
    constructor()

    /** Только сообщение и успех/неудача  */
    constructor(success: Boolean, message: String?) {
        this.isSuccess = success
        this.message = message
    }


    /** Сообщение, успех/неудача и дополнительные данные  */
    constructor(success: Boolean, message: String?, data: Any?) {
        this.isSuccess = success
        this.message = message
        this.data = data
    }

    override fun toString(): String {
        return "Response{" +
                "success=" + isSuccess +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}'
    }

    companion object {
        @Serial
        private const val serialVersionUID = 1L
    }
}