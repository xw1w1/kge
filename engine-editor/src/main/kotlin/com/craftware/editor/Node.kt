package com.craftware.editor

open class Node(var name: String = "Node") : NodeParent() {
    var isActive: Boolean = true
        get() = if (parent != null && parent is Node && !(parent as Node).isActive) false else field
    var parent: NodeParent? = null

    open val displayType: String = "Node"
}