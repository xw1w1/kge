package kge.editor.core

import kge.editor.EditorApplication
import kge.editor.component.Component
import kge.editor.component.HierarchyNode
import kge.editor.component.Transform
import kotlin.collections.forEach
import kotlin.reflect.KClass

open class GameObject(
    open var name: String = "GameObject"
) {
    val components: MutableList<Component> = ArrayList()

    val transform: Transform = requireComponent(Transform::class)
    val hierarchy: HierarchyNode = requireComponent(HierarchyNode::class)

    var activeSelf: Boolean by hierarchy::activeSelf
    val activeInHierarchy: Boolean by hierarchy::activeInHierarchy

    var parent: GameObject? by hierarchy::parent
    val children: MutableList<GameObject> by hierarchy::children

    open fun onUpdate() {
        this.components.forEach(Component::onUpdate)
    }

    fun addChild(obj: GameObject) {
        hierarchy.addChild(obj)
    }

    fun removeChild(obj: GameObject) {
        hierarchy.removeChild(obj)
    }

    fun isChild(obj: GameObject): Boolean {
        return hierarchy.isChild(obj)
    }

    inline fun <reified T> hasChildOfType(): Boolean {
        return hierarchy.hasChildOfType<T>()
    }

    fun forEachChildRecursive(action: (GameObject) -> Unit) {
        hierarchy.children.forEach { child ->
            action(child)
            child.forEachChildRecursive(action)
        }
    }

    fun forEachChild(action: (GameObject) -> Unit) {
        hierarchy.forEachChildNode(action)
    }

    inline fun <reified T : Component> addComponent(vararg consArgs: Any?) {
        val constr = T::class.java.constructors.firstOrNull { c ->
            val params = c.parameterTypes
            params.isNotEmpty() &&
                    GameObject::class.java.isAssignableFrom(params[0]) &&
                    params.size == consArgs.size + 1
        } ?: throw IllegalArgumentException(
            "No matching constructor found for ${T::class.java.simpleName}"
        )

        val args = arrayOf(this, *consArgs)
        val comp = constr.newInstance(*args) as T
        comp.onAwake()
        components.add(comp)
    }


    inline fun <reified T : Component> hasComponent(): Boolean {
        return this.components.any { it is T }
    }

    inline fun <reified T : Component> getComponent(): T? {
        return this.components.firstOrNull() { T::class.isInstance(it) } as? T?
    }

    inline fun <reified T : Component> getComponentsOfType(): MutableList<T> {
        return this.components.filter { T::class.isInstance(it) }.map { it as T }.toMutableList()
    }

    inline fun <reified T : Component> removeComponent() {
        this.components.removeIf { T::class.isInstance(it) }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Component> requireComponent(type: KClass<T>): T {
        val existing = components.firstOrNull { type.isInstance(it) } as? T
        if (existing != null) return existing
        val ctor = type.java.getDeclaredConstructor(GameObject::class.java)
        val comp = ctor.newInstance(this)
        comp.onAwake()
        components += comp
        return comp
    }

    override fun toString(): String {
        return "${this.name} (${this::class.simpleName})"
    }

    companion object {
        fun findByName(name: String): GameObject? {
            val scene = EditorApplication.getInstance().getProjectManager().getCurrentScene()
                ?: // TODO: EditorConsoleHost.throwWarning
                return null

            return scene.getAllObjects().firstOrNull { it.name == name }
        }

        fun destroy(obj: GameObject) {
            val scene = EditorApplication.getInstance().getProjectManager().getCurrentScene()
                ?: // TODO: EditorConsoleHost.throwWarning
                return

            scene.getSelection().deselect(obj)
            obj.children.toList().forEach {
                destroy(it)
            }

            obj.parent?.hierarchy?.removeChild(obj)

            //obj.components.forEach { it.onDestroy() }

            obj.components.clear()
            obj.hierarchy.children.clear()
        }
    }
}