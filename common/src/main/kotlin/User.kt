package org.example

import java.io.Serial
import java.io.Serializable
import java.util.*

class User : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }

    var login: String? = null
    var password: String? = null
    var isExists: Boolean = false

    constructor(login: String, password: String, status: Boolean) {
        this.login = login
        this.password = password
        this.isExists = status
    }

    constructor()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val user = other as User
        return login == user.login && password == user.password
    }

    override fun hashCode(): Int {
        return Objects.hash(login, password)
    }
}