package kge.editor.component

import kge.editor.core.GameObject

open class HierarchyNode(node: GameObject) : Component(node) {
    var parent: GameObject? = null

    val children: MutableList<GameObject> = mutableListOf()

    var activeSelf: Boolean = true
    val activeInHierarchy: Boolean
        get() {
            parent?.let { parent ->
                return if (parent.name == "##SceneHierarchyRoot") {
                    activeSelf
                } else {
                    parent.activeInHierarchy && activeSelf
                }
            }
            return activeSelf
        }

    override var shouldBeVisibleInInspector = false

    fun isChild(obj: GameObject): Boolean {
        return children.contains(obj)
    }

    inline fun <reified T> hasChildOfType(): Boolean {
        return children.any { it is T }
    }

    fun addChild(obj: GameObject) {
        val childNode = obj.hierarchy

        childNode.parent?.hierarchy?.children?.remove(obj)

        if (!children.contains(obj))
            children.add(obj)

        childNode.parent = this.gameObject
    }

    fun removeChild(obj: GameObject) {
        if (children.remove(obj)) {
            obj.hierarchy.parent = null
        }
    }

    fun forEachRecursive(action: (GameObject) -> Unit) {
        children.forEach { child ->
            action(child)
            child.hierarchy.forEachRecursive(action)
        }
    }

    fun forEachChildNode(action: (GameObject) -> Unit) {
        children.forEach { child -> action(child) }
    }
}
