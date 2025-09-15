package dataForCollection
/**
 * Утилитарный класс для безопасного преобразования строковых значений
 * в числовые типы данных и удаления пробелов.
 */
class Reader {
    /**
     * Преобразует строку в целое число.
     * @param string строка для преобразования
     * @return значение Int
     */
    fun readInt(string: String): Int{
        return string.toInt()
    }
    /**
     * Преобразует строку в число с плавающей точкой.
     * @param string строка для преобразования
     * @return значение Float
     */
    fun readFloat(string: String): Float{
        return string.toFloat()
    }
    /**
     * Преобразует строку в Double.
     * @param string строка для преобразования
     * @return значение Double
     */
    fun readDouble(string: String): Double {
        return string.toDouble()
    }

    /**
     * Преобразует строку в Long.
     * @param string строка для преобразования
     * @return значение Long
     */
    fun readLong(string: String): Long{
        return string.toLong()
    }
    /**
    * Удаляет пробелы с начала и конца строки.
    * @param string исходная строка
    * @return обрезанная строка
    */
    fun readLineTrimmed(string: String): String {
        return string.trim()
    }

}