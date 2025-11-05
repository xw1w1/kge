package kge.editor.component

import kge.editor.EditorTransformImpl
import kge.editor.ui.EditorText

class TransformComponent(
    var transform: EditorTransformImpl
) : Component("Transform") {
    override fun onInspectorUI() {
        EditorText.header("Transform")
    }
}