package kge.ui.toolkit.delegates

import kge.ui.toolkit.FieldFactory

class IntFieldDelegate(
    default: Int,
    val min: Int = Int.MIN_VALUE,
    val max: Int = Int.MAX_VALUE,
    override val labelOverride: String? = null,
    val visibleWhen: (() -> Boolean)? = null
) : EditorFieldDelegate<Int> {

    override var value: Int = default

    override fun draw(label: String): Boolean {
        if (visibleWhen?.invoke() == false) return false
        val new = FieldFactory.int(
            label = labelOverride ?: label,
            value = value
        )
        val clamped = new.coerceIn(min, max)
        val changed = clamped != value
        value = clamped
        return changed
    }
}

fun intField(default: Int, min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE, label: String? = null, visibleWhen: (() -> Boolean)? = null) =
    IntFieldDelegate(default, min, max, label, visibleWhen)
