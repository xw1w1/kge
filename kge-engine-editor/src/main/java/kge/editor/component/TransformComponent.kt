package kge.editor.component

import kge.api.std.INode
import kge.editor.EditorTransformImpl
import kge.editor.ui.EditorText

class TransformComponent(
    override var owningNode: INode,
    var transform: EditorTransformImpl
) : Component(owningNode, "Transform") {
    override fun onInspectorUI() {
        EditorText.header("Transform")
    }
}