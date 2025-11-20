package kge.editor.ui

import imgui.ImGui
import imgui.ImVec2
import imgui.ImVec4
import imgui.flag.ImGuiCol

class KgeEditorStyle {
    init {
        val style = ImGui.getStyle()

        style.windowPadding = ImVec2(6f, 6f)
        style.framePadding = ImVec2(6f, 3f)
        style.itemSpacing = ImVec2(6f, 4f)
        style.indentSpacing = 14f

        style.scrollbarSize = 12f
        style.grabMinSize = 8f

        style.windowRounding = 2f
        style.frameRounding = 2f
        style.popupRounding = 2f
        style.childRounding = 2f
        style.grabRounding = 2f
        style.scrollbarRounding = 3f

        style.windowBorderSize = 1f
        style.frameBorderSize = 1f
        style.popupBorderSize = 1f
        style.childBorderSize = 1f

        fun vCol(r: Float, g: Float, b: Float, a: Float = 1f) = ImVec4(r/255f, g/255f, b/255f, a)

        style.colors[ImGuiCol.Text]                  = vCol(204f, 204f, 204f)
        style.colors[ImGuiCol.TextDisabled]          = vCol(107f, 107f, 107f)

        style.colors[ImGuiCol.WindowBg]              = vCol(43f, 43f, 43f)
        style.colors[ImGuiCol.ChildBg]               = vCol(42f, 42f, 42f)
        style.colors[ImGuiCol.PopupBg]               = vCol(44f, 44f, 44f)

        style.colors[ImGuiCol.Border]                = vCol(31f, 31f, 31f)
        style.colors[ImGuiCol.BorderShadow]          = vCol(0f, 0f, 0f, 0f)

        style.colors[ImGuiCol.FrameBg]               = vCol(58f, 58f, 58f)
        style.colors[ImGuiCol.FrameBgHovered]        = vCol(71f, 71f, 71f)
        style.colors[ImGuiCol.FrameBgActive]         = vCol(81f, 81f, 81f)

        style.colors[ImGuiCol.Button]                = vCol(60f, 60f, 60f)
        style.colors[ImGuiCol.ButtonHovered]         = vCol(75f, 75f, 75f)
        style.colors[ImGuiCol.ButtonActive]          = vCol(50f, 50f, 50f)

        style.colors[ImGuiCol.Header]                = vCol(70f, 70f, 70f)
        style.colors[ImGuiCol.HeaderHovered]         = vCol(85f, 85f, 85f)
        style.colors[ImGuiCol.HeaderActive]          = vCol(95f, 95f, 95f)

        style.colors[ImGuiCol.TitleBg]               = vCol(36f, 36f, 36f)
        style.colors[ImGuiCol.TitleBgActive]         = vCol(44f, 44f, 44f)
        style.colors[ImGuiCol.TitleBgCollapsed]      = vCol(30f, 30f, 30f)

        style.colors[ImGuiCol.CheckMark]             = vCol(74f, 163f, 255f)
        style.colors[ImGuiCol.SliderGrab]            = vCol(74f, 163f, 255f)
        style.colors[ImGuiCol.SliderGrabActive]      = vCol(74f, 163f, 255f)

        style.colors[ImGuiCol.ResizeGrip]            = vCol(50f, 50f, 50f)
        style.colors[ImGuiCol.ResizeGripHovered]     = vCol(65f, 65f, 65f)
        style.colors[ImGuiCol.ResizeGripActive]      = vCol(80f, 80f, 80f)

        style.colors[ImGuiCol.TableHeaderBg]         = vCol(43f, 43f, 43f)
        style.colors[ImGuiCol.TableRowBg]            = vCol(46f, 46f, 46f)
        style.colors[ImGuiCol.TableRowBgAlt]         = vCol(50f, 50f, 50f)
        style.colors[ImGuiCol.TextSelectedBg]        = vCol(61f, 79f, 102f)

        style.colors[ImGuiCol.DragDropTarget]        = vCol(74f, 163f, 255f)

        style.colors[ImGuiCol.NavHighlight]          = vCol(74f, 163f, 255f)
    }
}

