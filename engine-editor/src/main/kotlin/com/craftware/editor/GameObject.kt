package com.craftware.editor

import com.craftware.editor.component.Component
import com.craftware.editor.component.Transform

class GameObject(name: String = "GameObject") : Node(name) {
    val transform: Transform
    val components = mutableListOf<Component>()

    override val displayType: String = "GameObject"

    init {
        transform = requireComponent<Transform>()
    }

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

    @Suppress("UNCHECKED_CAST")
    fun <T> addComponent(component: Component): T {
        components += component
        return component as T
    }

    private inline fun <reified T : Component> requireComponent(): T {
        if (get<T>() == null) add<T>()
        val comp = get<T>() ?: throw IllegalStateException("previously added component was not found for some reason wtf")
        return comp
    }

    override fun toString(): String = name
}
