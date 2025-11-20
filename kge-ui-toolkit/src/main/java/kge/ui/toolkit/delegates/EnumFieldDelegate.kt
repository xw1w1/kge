package kge.ui.toolkit.delegates

import kge.ui.toolkit.FieldFactory
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class EnumFieldDelegate<T : Enum<T>>(
    default: T,
    val enumClass: KClass<T>,
    override val labelOverride: String? = null,
    val visibleWhen: (() -> Boolean)? = null
) : EditorFieldDelegate<T> {

    override var value: T = default

    override fun draw(label: String): Boolean {
        if (visibleWhen?.invoke() == false) return false
        val new = FieldFactory.enum(labelOverride ?: label, value, enumClass)
        val changed = new != value
        value = new
        return changed
    }

    operator fun getValue(thisRef: Any, prop: KProperty<*>): T {
        return value
    }

    operator fun setValue(thisRef: Any, prop: KProperty<*>, newValue: T) {
        value = newValue
    }
}

fun <T : Enum<T>> enumField(default: T, enumClass: KClass<T>, label: String? = null, visibleWhen: (() -> Boolean)? = null) =
    EnumFieldDelegate(default, enumClass, label, visibleWhen)
