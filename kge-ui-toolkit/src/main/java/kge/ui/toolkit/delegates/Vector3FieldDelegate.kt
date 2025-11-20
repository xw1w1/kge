package kge.ui.toolkit.delegates

import kge.ui.toolkit.FieldFactory
import org.joml.Vector3f
import kotlin.reflect.KProperty

class Vector3FieldDelegate(
    default: Vector3f,
    override val labelOverride: String? = null,
    val visibleWhen: (() -> Boolean)? = null
) : EditorFieldDelegate<Vector3f> {

    override var value = default

    override fun draw(label: String): Boolean {
        if (visibleWhen?.invoke() == false) return false
        val newArr = FieldFactory.vec3(labelOverride ?: label, value)
        val changed = !(newArr[0] == value.x && newArr[1] == value.y && newArr[2] == value.z)
        if (changed) value.set(newArr)
        return changed
    }

    operator fun getValue(thisRef: Any, prop: KProperty<*>): Vector3f {
        return value
    }

    operator fun setValue(thisRef: Any, prop: KProperty<*>, newValue: Vector3f) {
        value = newValue
    }
}

fun vec3Field(default: Vector3f, label: String? = null, visibleWhen: (() -> Boolean)? = null) =
    Vector3FieldDelegate(default, label, visibleWhen)
