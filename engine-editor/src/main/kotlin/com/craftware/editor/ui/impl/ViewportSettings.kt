package com.craftware.editor.ui.impl

import com.craftware.editor.ui.UIPanel
import com.craftware.engine.imgui.checkbox
import com.craftware.engine.imgui.dragFloat
import com.craftware.engine.imgui.separator
import com.craftware.engine.imgui.textDisabled

class ViewportSettings : UIPanel("ViewportSettings", "Viewport Settings"){
    fun render() = render {
        checkbox("Show grid", showGrid) {
            showGrid = !showGrid
        }
        checkbox("Show axis", showAxis) {
            showAxis = !showAxis
        }
        separator()

        textDisabled("Axis line width")
        dragFloat("##Axis line width", axisLineWidth, onChange = {
            axisLineWidth = it
        })

        separator()

        textDisabled("Gizmos settings")
        dragFloat("AxisHitboxSize", pickingAxisSize, onChange = {
            pickingAxisSize = it
        })

        dragFloat("CamDistModifier", camDistModifier, onChange = {
            camDistModifier = it
        })

        dragFloat("CoerceMinValue", coerceMinValue, onChange = {
            coerceMinValue = it
        })

        dragFloat("CoerceMaxValue", coerceMaxValue, onChange = {
            coerceMaxValue = it
        })
    }

    companion object {
        var showAxis: Boolean = true
        var showGrid: Boolean = true
        var axisLineWidth: Float = 2f

        var pickingAxisSize: Float = 0.02f
        var camDistModifier: Float = 0.25f
        var coerceMinValue: Float = 0.2f
        var coerceMaxValue: Float = 2.2f
    }
}