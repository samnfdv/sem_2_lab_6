package exceptions

class InvalidOutputException : Exception() {
    override val message: String = "Invalid output."
}