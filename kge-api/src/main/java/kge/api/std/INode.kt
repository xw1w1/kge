package kge.api.std

@Deprecated("")
interface INode<N> {
    val nodeId: Int

    var name: String

    var isActive: Boolean

    var parent: N?
}