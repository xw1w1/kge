package kge.ui.toolkit.delegates

import kge.ui.toolkit.FieldFactory
import kotlin.reflect.KProperty

class FloatFieldDelegate(
    default: Float,
    val step: Float = 0.1f,
    val min: Float = Float.NaN,
    val max: Float = Float.NaN,
    override val labelOverride: String? = null,
    val visibleWhen: (() -> Boolean)? = null
) : EditorFieldDelegate<Float> {

    override var value: Float = default

    override fun draw(label: String): Boolean {
        if (visibleWhen?.invoke() == false) return false
        val new = FieldFactory.float(
            label = labelOverride ?: label,
            value = value,
            step = step
        )
        val constrained = when {
            !min.isNaN() && new < min -> min
            !max.isNaN() && new > max -> max
            else -> new
        }
        val changed = constrained != value
        value = constrained
        return changed
    }

    operator fun getValue(thisRef: Any, prop: KProperty<*>): Float {
        return value
    }

    operator fun setValue(thisRef: Any, prop: KProperty<*>, newValue: Float) {
        value = newValue
    }
}

fun floatField(
    default: Float,
    step: Float = 0.1f,
    min: Float = Float.NaN,
    max: Float = Float.NaN,
    label: String? = null,
    visibleWhen: (() -> Boolean)? = null
) = FloatFieldDelegate(default, step, min, max, label, visibleWhen)
