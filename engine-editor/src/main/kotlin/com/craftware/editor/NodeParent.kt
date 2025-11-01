package com.craftware.editor

open class NodeParent {
    private val children = mutableListOf<Node>()

    fun addChild(child: Node) {
        child.parent = this
        children += child
    }

    fun removeChild(child: Node) {
        children -= child
        if (child.parent != this) throw IllegalArgumentException("Child node does not belong to this parent")
        child.parent = null
    }

    fun getChildren(): List<Node> = this.children.toList()
}