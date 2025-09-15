package dataForCollection
/**
 * Координаты, описывающие местоположение транспортного средства.
 *
 * @property x Координата по оси X.
 * @property y Координата по оси Y (не может быть null).
 * @throws IllegalArgumentException если `y` равен null.
 */
data class Coordinates(
    var x : Float,
    var y : Int, // Не может быть null

){
    init {

        require(y != null) { "Y - не должен быть null" }
    }
    /**
     * Возвращает координаты в строковом формате: \"x, y\".
     */
    override fun toString(): String {
        return "$x, $y"
    }
}
