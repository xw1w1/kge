package kge.ui.toolkit.delegates

import kge.ui.toolkit.FieldFactory
import kotlin.reflect.KProperty

class BoolFieldDelegate(
    default: Boolean,
    override val labelOverride: String? = null,
    val visibleWhen: (() -> Boolean)? = null
) : EditorFieldDelegate<Boolean> {

    override var value: Boolean = default

    override fun draw(label: String): Boolean {
        if (visibleWhen?.invoke() == false) return false
        val new = FieldFactory.bool(labelOverride ?: label, value)
        val changed = new != value
        value = new
        return changed
    }

    operator fun getValue(thisRef: Any, prop: KProperty<*>): Boolean {
        return value
    }

    operator fun setValue(thisRef: Any, prop: KProperty<*>, newValue: Boolean) {
        value = newValue
    }
}

fun boolField(default: Boolean, label: String? = null) =
    BoolFieldDelegate(default, label)
