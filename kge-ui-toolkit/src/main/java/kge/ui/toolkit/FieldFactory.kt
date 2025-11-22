package kge.ui.toolkit

import imgui.ImGui
import imgui.flag.ImGuiTableColumnFlags
import imgui.flag.ImGuiTableFlags
import imgui.type.ImBoolean
import imgui.type.ImString
import kotlin.reflect.KClass
import org.joml.Vector3f

object FieldFactory {
    private const val DEFAULT_WIDTH = 215f

    private inline fun layout(
        label: String,
        layout: FieldLayout,
        width: Float,
        drawField: () -> Boolean
    ): Boolean {
        return when (layout) {
            FieldLayout.Horizontal -> {
                ImGui.beginTable("tbl_$label", 2, ImGuiTableFlags.SizingStretchSame or ImGuiTableFlags.NoPadInnerX)

                ImGui.tableSetupColumn("label_col", ImGuiTableColumnFlags.WidthStretch)
                ImGui.tableSetupColumn("field_col", ImGuiTableColumnFlags.WidthFixed, width)
                ImGui.tableNextRow()
                ImGui.tableSetColumnIndex(0)

                UIText.renderLabelClipped(label)

                ImGui.tableSetColumnIndex(1)
                ImGui.setNextItemWidth(width)

                val changed = drawField()

                ImGui.endTable()
                changed
            }

            FieldLayout.Vertical -> {
                ImGui.text(label)
                ImGui.setNextItemWidth(width)
                drawField()
            }
        }
    }

    fun <T : Any> objectField(
        label: String,
        value: T?,
        immutable: Boolean,
        type: KClass<T>,
        width: Float = DEFAULT_WIDTH,
        layout: FieldLayout = FieldLayout.Horizontal,
        onChange: (T?) -> Unit,
        onClickLMB: (T?) -> Unit = {},
        onClickRMB: (T?) -> Unit = {},
    ): T? {
        ImGui.pushID("ff_obj_$label")

        when (layout) {
            FieldLayout.Horizontal -> {
                ImGui.beginTable("tbl_obj_$label", 2,
                    ImGuiTableFlags.SizingStretchSame or ImGuiTableFlags.NoPadInnerX)

                ImGui.tableSetupColumn("label_col", ImGuiTableColumnFlags.WidthStretch)
                ImGui.tableSetupColumn("field_col", ImGuiTableColumnFlags.WidthFixed, width)

                ImGui.tableNextRow()
                ImGui.tableSetColumnIndex(0)
                UIText.renderLabelClipped(label)
                ImGui.tableSetColumnIndex(1)
                ImGui.setNextItemWidth(width)

                EditorDragDropField.draw(label, value, immutable, type, width, onChange = { newValue ->
                    if (!immutable) onChange(newValue)
                }, onClickLMB, onClickRMB)

                ImGui.endTable()
            }

            FieldLayout.Vertical -> {
                ImGui.text(label)
                EditorDragDropField.draw(label, value, immutable, type, width, onChange = { newValue ->
                    if (!immutable) onChange(newValue)
                }, onClickLMB, onClickRMB)
            }
        }

        ImGui.popID()
        return value
    }

    fun float(label: String, value: Float, step: Float = 0.1f, width: Float = DEFAULT_WIDTH, layout: FieldLayout = FieldLayout.Horizontal): Float {
        val v = floatArrayOf(value)
        layout(label, layout, width) { ImGui.dragFloat("##${label}_float", v, step) }
        return v[0]
    }

    fun int(label: String, value: Int, step: Int = 1, width: Float = DEFAULT_WIDTH, layout: FieldLayout = FieldLayout.Horizontal): Int {
        val arr = intArrayOf(value)
        layout(label, layout, width) { ImGui.dragInt("##${label}_int", arr, step.toFloat()) }
        return arr[0]
    }

    fun text(label: String, value: String, width: Float = DEFAULT_WIDTH, layout: FieldLayout = FieldLayout.Horizontal): String {
        val imStr = ImString(value, 256)
        layout(label, layout, width) { ImGui.inputText("##${label}_text", imStr) }
        return imStr.get().trimEnd('\u0000')
    }

    fun bool(label: String, value: Boolean, layout: FieldLayout = FieldLayout.Horizontal): Boolean {
        val v = ImBoolean(value)
        layout(label, layout, DEFAULT_WIDTH) { ImGui.checkbox("##${label}_bool", v) }
        return v.get()
    }

    fun floatArray(
        label: String,
        values: FloatArray,
        step: Float = 0.1f,
        width: Float = DEFAULT_WIDTH,
        layout: FieldLayout = FieldLayout.Horizontal
    ): FloatArray {
        val buffer = values.copyOf()
        layout(label, layout, width) {
            val itemWidth = width / buffer.size - 4f
            repeat(buffer.size) { i ->
                if (i > 0) ImGui.sameLine()
                ImGui.setNextItemWidth(itemWidth)
                val tmp = floatArrayOf(buffer[i])
                ImGui.dragFloat("##${label}_$i", tmp, step)
                buffer[i] = tmp[0]
            }
            true
        }
        return buffer
    }

    fun vec3(label: String, vec: Vector3f, step: Float = 0.1f, width: Float = DEFAULT_WIDTH, layout: FieldLayout = FieldLayout.Horizontal): Vector3f {
        val arr = floatArrayOf(vec.x, vec.y, vec.z)
        val newArr = floatArray(label, arr, step, width, layout)
        vec.set(newArr[0], newArr[1], newArr[2])
        return vec
    }

    fun <T : Enum<T>> enum(
        label: String,
        value: T,
        enumClass: KClass<T>,
        width: Float = DEFAULT_WIDTH,
        layout: FieldLayout = FieldLayout.Horizontal
    ): T {
        var selected = value
        val names = enumClass.java.enumConstants!!.map { it.name }.toTypedArray()

        layout(label, layout, width) {
            if (ImGui.beginCombo("##${label}_enum", selected.name)) {
                for (n in names) {
                    val isSelected = n == selected.name
                    if (ImGui.selectable(n, isSelected)) {
                        selected = enumClass.java.enumConstants!!.first { it.name == n }
                    }
                    if (isSelected) ImGui.setItemDefaultFocus()
                }

                ImGui.endCombo()
                true
            } else {
                false
            }
        }

        return selected
    }

}
