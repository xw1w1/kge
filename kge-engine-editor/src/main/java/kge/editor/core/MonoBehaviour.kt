package kge.editor.core

abstract class MonoBehaviour {
    lateinit var gameObject: GameObject
        internal set

    val transform get() = gameObject.transform

    open fun start() {}
    open fun update(dt: Float) {}
    open fun onDestroy() {}
}
