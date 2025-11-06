package kge.api.input

interface IMouse {
    val x: Double
    val y: Double
    val dx: Double
    val dy: Double

    val scroll: Double

    var cursorDisabled: Boolean

    fun isDown(button: Int): Boolean
}