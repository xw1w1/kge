package kge.api.editor

import kge.api.std.INode

interface IEditorComponent {
    val displayTypeName: String
    var owningNode: INode

    fun onInspectorUI()
}