package kge.api.std

interface INodeParent : INode {
    val children: List<INode>

    fun addChild(child: INode)

    fun removeChild(child: INode)

    fun clearChildren()

    fun hasChild(node: INode): Boolean

    fun findChildByName(name: String): INode?

    fun forEachChildRecursive(action: (INode) -> Unit)
}