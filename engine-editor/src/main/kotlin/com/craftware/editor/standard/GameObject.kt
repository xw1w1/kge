package com.craftware.editor.standard

import com.craftware.editor.Node
import com.craftware.editor.component.Component
import com.craftware.editor.component.Transform

open class GameObject(name: String = "GameObject") : Node(name) {
    val components = mutableListOf<Component>() // needs to be declared first

    var transform: Transform = requireComponent<Transform>()
        set(value) {
            remove<Transform>()
            addComponent(value)
        }

    override val displayType: String = "GameObject"

    @Suppress("UNCHECKED_CAST")
    fun <T : Component> getComponent(type: Class<T>): T? {
        return components.find { type.isInstance(it) } as? T
    }

    inline fun <reified T : Component> get(): T? = getComponent(T::class.java)
    inline fun <reified T : Component> add(): T {
        val comp = T::class.java.getDeclaredConstructor().newInstance()
        components += comp
        return comp
    }
    inline fun <reified T : Component> remove() {
        components.removeIf { it is T }
    }

    @Suppress("UNCHECKED_CAST")
    fun addComponent(component: Component) {
        components += component
    }

    private inline fun <reified T : Component> requireComponent(): T {
        if (get<T>() == null) add<T>()
        val comp = get<T>() ?: throw IllegalStateException("previously added component was not found for some reason wtf")
        return comp
    }

    override fun toString(): String = name
}