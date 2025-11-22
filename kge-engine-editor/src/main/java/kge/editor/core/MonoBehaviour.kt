package kge.editor.core

import kge.editor.component.Component

abstract class MonoBehaviour {
    lateinit var gameObject: GameObject
        internal set

    val transform get() = gameObject.transform

    inline fun <reified T : Component> getComponent(): T? {
        return gameObject.getComponent<T>()
    }

    open fun onAwake() {}
    open fun onUpdate(delta: Float) {}
    open fun onDestroy() {}
}
