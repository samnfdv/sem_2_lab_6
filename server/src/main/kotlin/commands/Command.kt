package org.example.commands

interface Command {
    fun execute(args: Array<Any>?): String
}