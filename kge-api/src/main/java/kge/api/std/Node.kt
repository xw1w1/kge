package kge.api.std

open class Node(
    override var name: String = "Node",
    override var isActive: Boolean = true
) : INodeParent {
    override val nodeId: Int = System.identityHashCode(this)

    override var parent: INodeParent? = null

    protected val _children: MutableList<INode> = mutableListOf()

    override val children: List<INode>
        get() = _children

    override val displayType: String
        get() = "${this::class.simpleName ?: "Node"}#$nodeId"

    override fun addChild(child: INode) {
        if (child === this)
            throw IllegalArgumentException("Cannot add node to itself: ${child.name}")

        if (_children.contains(child)) return

        child.parent?.removeChild(child)

        _children.add(child)
        child.parent = this
    }

    override fun removeChild(child: INode) {
        if (_children.remove(child)) {
            if (child.parent == this) {
                child.parent = null
            }
        }
    }

    override fun clearChildren() {
        _children.forEach { it.parent = null }
        _children.clear()
    }

    override fun hasChild(node: INode): Boolean = _children.contains(node)

    override fun findChildByName(name: String): INode? =
        _children.firstOrNull { it.name == name }

    override fun forEachChildRecursive(action: (INode) -> Unit) {
        _children.forEach {
            action(it)
            if (it is INodeParent) it.forEachChildRecursive(action)
        }
    }

    override fun toString(): String = "$displayType(name=$name, active=$isActive)"
}
