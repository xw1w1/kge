package kge.api.std

@Deprecated("")
interface INodeParent<N> : INode<N> {
    val children: List<N>

    fun addChild(child: N)

    fun removeChild(child: N)

    fun clearChildren()

    fun hasChild(node: N): Boolean

    fun findChildByName(name: String): N?

    fun forEachChildRecursive(action: (N) -> Unit)
}