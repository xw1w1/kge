package kge.api.editor

import kge.api.editor.imgui.UIRenderable

interface IEditorComponent : UIRenderable {
    val displayTypeName: String
}