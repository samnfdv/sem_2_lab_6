package org.example

import java.io.Serializable

/**
 * Класс запроса для передачи команды и аргументов.
 * @property type тип команды
 * @property args аргументы команды (может быть null)
 */
class Request(
    var type: CommandType,
    val args: Array<Any>? = null
) : Serializable {
    val serialVersionUID = 1L


    constructor(type: CommandType) : this(type, null)

    override fun toString(): String {
        return "Request(type=$type, args=${args?.contentToString()})"
    }

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
