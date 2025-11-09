package kge.editor

import kge.api.editor.IEditorComponent
import kge.api.std.Node
import kge.editor.component.TransformComponent

open class GameObject(
    override var name: String = "GameObject"
) : Node(name) {
    private val _transform: EditorTransformImpl = EditorTransformImpl()

    val components = mutableListOf<IEditorComponent>()

    init {
        val transform = TransformComponent(_transform)
        this.addComponent(transform)
    }

    override val displayType: String = "GameObject"

    var transform: EditorTransformImpl = requireComponent<TransformComponent>().transform!!

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
}
