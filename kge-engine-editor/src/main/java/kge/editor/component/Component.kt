package kge.editor.component

import kge.editor.core.GameObject

open class Component(
    val gameObject: GameObject
) {
    var active: Boolean = true
    open var shouldBeVisibleInInspector: Boolean = true

    open val typeName: String = "Component"

    open fun onAwake() {}

    open fun onUpdate() {}

    inline fun <reified T : Component> addComponent() {
        this.gameObject.addComponent<T>()
    }

    inline fun <reified T : Component> getComponent(): T? {
        return this.gameObject.getComponent<T>()
    }

    inline fun <reified T : Component> getComponents(): MutableList<T> {
        return this.gameObject.getComponentsOfType()
    }

    inline fun <reified T : Component> removeComponent() {
        this.gameObject.removeComponent<T>()
    }
}
