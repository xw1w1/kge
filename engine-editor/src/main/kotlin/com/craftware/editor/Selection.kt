package com.craftware.editor

import com.craftware.editor.standard.GameObject

class Selection {
    private var selectedObjects: MutableList<Node> = mutableListOf()

    fun select(node: Node) {
        selectedObjects.clear()
        selectedObjects.add(node)
    }

    fun add(node: Node) {
        selectedObjects.add(node)
    }

    fun selectMultiple(objects: List<GameObject>) {
        selectedObjects.clear()
        selectedObjects.addAll(objects)
    }

    fun getSelectedObjects(): List<Node> {
        return selectedObjects
    }

    fun clear() {
        selectedObjects.clear()
    }

    fun selectEditorCamera(cameraObject: GameObject) {
        selectedObjects.add(cameraObject)
    }
}
