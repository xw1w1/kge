package kge.api.input

interface IKeyboard {
    fun isDown(key: Int): Boolean

    fun getKeys(): BooleanArray
}