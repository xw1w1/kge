package kge.api.std

interface INode {
    var name: String

    var isActive: Boolean

    var parent: INodeParent?

    val displayType: String

    val isActiveInHierarchy: Boolean
        get() = isActive && (parent?.isActiveInHierarchy ?: true)
}