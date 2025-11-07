package kge.editor

import kge.api.editor.IEditorComponent
import kge.api.std.INodeParent
import kge.api.std.INode
import kge.editor.component.TransformComponent

open class GameObject(
    override var name: String = "GameObject"
) : INodeParent {
    private val _children = mutableListOf<INode>()
    private val _transform: EditorTransformImpl = EditorTransformImpl()
    val components = mutableListOf<IEditorComponent>()

    init {
        val transform = TransformComponent(_transform)
        this.addComponent(transform)
    }

    override var parent: INodeParent? = null

    override val children: List<INode>
        get() = _children.toList()

    override var isActive: Boolean = true

    override val displayType: String = "GameObject"

    var transform: EditorTransformImpl = requireComponent<TransformComponent>().transform!!

    override fun addChild(child: INode) {
        require(child !== this) { "Cannot add self as a child." }
        if (child.parent === this) return
        (child.parent as? GameObject)?.removeChild(child)
        child.parent = this
        _children += child
    }

    override fun removeChild(child: INode) {
        if (_children.remove(child)) {
            child.parent = null
        }
    }

    override fun clearChildren() {
        _children.forEach { it.parent = null }
        _children.clear()
    }

    override fun hasChild(node: INode): Boolean = node in _children

    override fun findChildByName(name: String): INode? {
        _children.forEach { child ->
            if (child.name == name) return child
            if (child is INodeParent) {
                val found = child.findChildByName(name)
                if (found != null) return found
            }
        }
        return null
    }

    override fun forEachChildRecursive(action: (INode) -> Unit) {
        _children.forEach { child ->
            action(child)
            if (child is INodeParent) {
                child.forEachChildRecursive(action)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : IEditorComponent> getComponent(type: Class<T>): T? {
        return components.find { type.isInstance(it) } as? T
    }

    inline fun <reified T : IEditorComponent> get(): T? = getComponent(T::class.java)

    inline fun <reified T : IEditorComponent> addComponent(vararg args: Any? = arrayOf(this)): T {
        val comp = T::class.java.getDeclaredConstructor().newInstance(args)
        components += comp
        return comp
    }

    inline fun <reified T : IEditorComponent> removeComponent() {
        components.removeIf { it is T }
    }

    fun addComponent(component: IEditorComponent) {
        components += component
    }

    private inline fun <reified T : IEditorComponent> requireComponent(): T {
        val existing = get<T>()
        if (existing != null) return existing
        val added = addComponent<T>()
        return added
    }

    override fun toString(): String = "$displayType('$name')"
}
