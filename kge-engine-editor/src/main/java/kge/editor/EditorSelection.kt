package kge.editor

import kge.editor.core.GameObject

class EditorSelection {
    private val selectedObjects: LinkedHashSet<GameObject> = LinkedHashSet()

    fun select(obj: GameObject) {
        selectedObjects.set(obj)
    }

    fun addSelection(vararg obj: GameObject) {
        selectedObjects.addAll(obj)
    }

    fun deselect(obj: GameObject) {
        selectedObjects.remove(obj)
    }

    fun clearSelection() {
        selectedObjects.clear()
    }

    fun getSelectedObjects(): MutableSet<GameObject> = selectedObjects.toMutableSet()

    private fun <T> MutableSet<T>.set(value: T) {
        this.clear()
        this.add(value)
    }
}

fun <T> MutableSet<T>.exclude(value: T): MutableSet<T> {
    val set = mutableSetOf<T>()
    set.addAll(this)
    set.remove(value)
    return set
}