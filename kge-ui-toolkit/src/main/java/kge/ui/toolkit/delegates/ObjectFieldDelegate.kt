package kge.ui.toolkit.delegates

import imgui.ImGui
import kge.ui.toolkit.FieldFactory
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class ObjectFieldDelegate<T : Any>(
    default: T?,
    private val type: KClass<T>,
    override val labelOverride: String? = null,
    val immutable: Boolean = false,
    val onClickLMB: (T?) -> Unit = {},
    val onClickRMB: (T?) -> Unit = {},
    val visibleWhen: (() -> Boolean)? = null
) : EditorFieldDelegate<T?> {

    override var value: T? = default

    override fun draw(label: String): Boolean {
        if (visibleWhen?.invoke() == false) return false
        val prev = value

        val newValue = FieldFactory.objectField(
            label = labelOverride ?: label,
            value = prev,
            immutable = immutable,
            type = type,
            onChange = { v -> value = v },
            onClickLMB = onClickLMB,
            onClickRMB = onClickRMB
        )

        var changed = newValue != prev

        ImGui.sameLine()
        if (ImGui.smallButton("Reset##${label.hashCode()}")) {
            value = null
            changed = true
        }

        return changed
    }

    operator fun getValue(thisRef: Any, prop: KProperty<*>): T? = value
    operator fun setValue(thisRef: Any, prop: KProperty<*>, newValue: T?) {
        value = newValue
    }
}

inline fun <reified T : Any> objectField(
    default: T?,
    label: String? = null,
    immutable: Boolean = false,
    noinline onClickLMB: (T?) -> Unit = {},
    noinline onClickRMB: (T?) -> Unit = {},
    noinline visibleWhen: (() -> Boolean)? = null
) = ObjectFieldDelegate(default, T::class, label, immutable, onClickLMB, onClickRMB, visibleWhen)

