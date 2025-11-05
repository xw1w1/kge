package kge.editor.component

import kge.api.editor.IEditorComponent
import kge.api.std.INodeParent

open class Component(
    override val displayTypeName: String
) : IEditorComponent {
    open var owner: INodeParent? = null

    open var enabled: Boolean = true

    open fun onAttach() {}

    open fun onDetach() {}

    open fun onUpdate(deltaTime: Float) {}

    override fun onInspectorUI() {}

    override fun toString(): String = displayTypeName
}
