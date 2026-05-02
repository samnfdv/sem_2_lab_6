package org.example.dataBase

import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class PasswordManager {
    fun hashPassword(password: String): String {
        try {
            val md = MessageDigest.getInstance("SHA-256")
            val byteArray = md.digest(password.toByteArray(Charsets.UTF_8))
            val bigInteger = BigInteger(1, byteArray)
            return bigInteger.toString(16)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }
    }
}