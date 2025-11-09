package kge.editor

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

    fun getSelectedObjects(): Set<GameObject> = selectedObjects.toSet()

    private fun <T> MutableSet<T>.set(value: T) {
        this.clear()
        this.add(value)
    }
}