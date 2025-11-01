package com.craftware.editor

class Selection {
    var selected: Node? = null
        private set

    fun select(node: Node?) {
        selected = node
    }

    fun clear() {
        selected = null
    }

    fun selectEditorCamera(cameraObject: GameObject) {
        selected = cameraObject
    }
}
