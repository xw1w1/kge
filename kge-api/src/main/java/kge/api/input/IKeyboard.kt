package kge.api.input

interface IKeyboard : IInputDevice {
    fun isDown(key: Int): Boolean

    fun getKeys(): BooleanArray
}