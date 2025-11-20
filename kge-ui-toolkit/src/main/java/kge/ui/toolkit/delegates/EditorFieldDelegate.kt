package kge.ui.toolkit.delegates

interface EditorFieldDelegate<T : Any?> {
    var value: T
    val labelOverride: String?
    fun draw(label: String): Boolean
}