package kge.editor.ui

import imgui.ImGui
import imgui.ImVec2
import imgui.ImVec4
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiDir

class KgeEditorStyle {
    init {
        val style = ImGui.getStyle()

        style.alpha = 1.0f
        style.disabledAlpha = 1.0f

        style.windowPadding = ImVec2(12f, 12f)
        style.windowRounding = 11.5f
        style.windowBorderSize = 0f //TODO
        style.windowMinSize = ImVec2(20f, 20f)
        style.windowTitleAlign = ImVec2(0.5f, 0.5f)
        style.windowMenuButtonPosition = ImGuiDir.Right

        style.childRounding = 0f
        style.childBorderSize = 1f

        style.popupRounding = 0f
        style.popupBorderSize = 1f

        style.framePadding = ImVec2(8.0f, 3.4f)
        style.frameRounding = 5f
        style.frameBorderSize = 0f

        style.itemSpacing = ImVec2(4.3f, 5.5f)
        style.itemInnerSpacing = ImVec2(7f, 1.8f)

        style.cellPadding = ImVec2(9f, 9f)
        style.indentSpacing = 0f

        style.columnsMinSpacing = 4.9f

        style.scrollbarSize = 11.6f
        style.scrollbarRounding = 15.9f

        style.grabMinSize = 3.7f
        style.grabRounding = 20f

        style.tabRounding = 0f
        style.tabBorderSize = 0f
        style.tabMinWidthForCloseButton = 0f

        style.colorButtonPosition = ImGuiDir.Right
        style.buttonTextAlign = ImVec2(0.5f, 0.5f)
        style.selectableTextAlign = ImVec2(0.5f, 0.5f)

        style.colors[ImGuiCol.Text]              = ImVec4(1.00f, 1.00f, 1.00f, 1.00f)
        style.colors[ImGuiCol.TextDisabled]      = ImVec4(0.27f, 0.32f, 0.45f, 1.00f)
        style.colors[ImGuiCol.WindowBg]          = ImVec4(0.08f, 0.09f, 0.10f, 1.00f)
        style.colors[ImGuiCol.ChildBg]           = ImVec4(0.09f, 0.10f, 0.12f, 1.00f)
        style.colors[ImGuiCol.PopupBg]           = ImVec4(0.08f, 0.09f, 0.10f, 1.00f)
        style.colors[ImGuiCol.Border]            = ImVec4(0.16f, 0.17f, 0.19f, 1.00f)
        style.colors[ImGuiCol.BorderShadow]      = ImVec4(0.08f, 0.09f, 0.10f, 1.00f)
        style.colors[ImGuiCol.FrameBg]           = ImVec4(0.11f, 0.13f, 0.15f, 1.00f)
        style.colors[ImGuiCol.FrameBgHovered]    = ImVec4(0.16f, 0.17f, 0.19f, 1.00f)
        style.colors[ImGuiCol.FrameBgActive]     = ImVec4(0.16f, 0.17f, 0.19f, 1.00f)
        style.colors[ImGuiCol.TitleBg]           = ImVec4(0.05f, 0.06f, 0.07f, 1.00f)
        style.colors[ImGuiCol.TitleBgActive]     = ImVec4(0.05f, 0.06f, 0.07f, 1.00f)
        style.colors[ImGuiCol.TitleBgCollapsed]  = ImVec4(0.08f, 0.09f, 0.10f, 1.00f)
        style.colors[ImGuiCol.MenuBarBg]         = ImVec4(0.10f, 0.11f, 0.12f, 1.00f)
        style.colors[ImGuiCol.ScrollbarBg]       = ImVec4(0.05f, 0.06f, 0.07f, 1.00f)
        style.colors[ImGuiCol.ScrollbarGrab]     = ImVec4(0.12f, 0.13f, 0.15f, 1.00f)
        style.colors[ImGuiCol.ScrollbarGrabHovered]= ImVec4(0.16f, 0.17f, 0.19f, 1.00f)
        style.colors[ImGuiCol.ScrollbarGrabActive] = ImVec4(0.12f, 0.13f, 0.15f, 1.00f)
        style.colors[ImGuiCol.CheckMark]         = ImVec4(0.97f, 1.00f, 0.50f, 1.00f)
        style.colors[ImGuiCol.SliderGrab]        = ImVec4(0.97f, 1.00f, 0.50f, 1.00f)
        style.colors[ImGuiCol.SliderGrabActive]  = ImVec4(1.00f, 0.80f, 0.50f, 1.00f)
        style.colors[ImGuiCol.Button]            = ImVec4(0.12f, 0.13f, 0.15f, 1.00f)
        style.colors[ImGuiCol.ButtonHovered]     = ImVec4(0.18f, 0.19f, 0.20f, 1.00f)
        style.colors[ImGuiCol.ButtonActive]      = ImVec4(0.15f, 0.15f, 0.15f, 1.00f)
        style.colors[ImGuiCol.Header]            = ImVec4(0.14f, 0.16f, 0.21f, 1.00f)
        style.colors[ImGuiCol.HeaderHovered]     = ImVec4(0.11f, 0.11f, 0.11f, 1.00f)
        style.colors[ImGuiCol.HeaderActive]      = ImVec4(0.08f, 0.09f, 0.10f, 1.00f)
        style.colors[ImGuiCol.Separator]         = ImVec4(0.13f, 0.15f, 0.19f, 1.00f)
        style.colors[ImGuiCol.SeparatorHovered]  = ImVec4(0.16f, 0.18f, 0.25f, 1.00f)
        style.colors[ImGuiCol.SeparatorActive]   = ImVec4(0.16f, 0.18f, 0.25f, 1.00f)
        style.colors[ImGuiCol.ResizeGrip]        = ImVec4(0.15f, 0.15f, 0.15f, 1.00f)
        style.colors[ImGuiCol.ResizeGripHovered] = ImVec4(0.97f, 1.00f, 0.50f, 1.00f)
        style.colors[ImGuiCol.ResizeGripActive]  = ImVec4(1.00f, 1.00f, 1.00f, 1.00f)
        style.colors[ImGuiCol.Tab]               = ImVec4(0.08f, 0.09f, 0.10f, 1.00f)
        style.colors[ImGuiCol.TabHovered]        = ImVec4(0.12f, 0.13f, 0.15f, 1.00f)
        style.colors[ImGuiCol.TabActive]         = ImVec4(0.12f, 0.13f, 0.15f, 1.00f)
        style.colors[ImGuiCol.TabUnfocused]      = ImVec4(0.08f, 0.09f, 0.10f, 1.00f)
        style.colors[ImGuiCol.TabUnfocusedActive] = ImVec4(0.12f, 0.27f, 0.57f, 1.00f)
        style.colors[ImGuiCol.PlotLines]         = ImVec4(0.52f, 0.60f, 0.70f, 1.00f)
        style.colors[ImGuiCol.PlotLinesHovered]  = ImVec4(0.04f, 0.98f, 0.98f, 1.00f)
        style.colors[ImGuiCol.PlotHistogram]     = ImVec4(0.88f, 0.79f, 0.56f, 1.00f)
        style.colors[ImGuiCol.PlotHistogramHovered] = ImVec4(0.96f, 0.96f, 0.96f, 1.00f)
        style.colors[ImGuiCol.TableHeaderBg]     = ImVec4(0.05f, 0.06f, 0.07f, 1.00f)
        style.colors[ImGuiCol.TableBorderStrong] = ImVec4(0.05f, 0.06f, 0.07f, 1.00f)
        style.colors[ImGuiCol.TableBorderLight]  = ImVec4(0.00f, 0.00f, 0.00f, 1.00f)
        style.colors[ImGuiCol.TableRowBg]       = ImVec4(0.12f, 0.13f, 0.15f, 1.00f)
        style.colors[ImGuiCol.TableRowBgAlt]    = ImVec4(0.10f, 0.11f, 0.12f, 1.00f)
        style.colors[ImGuiCol.TextSelectedBg]    = ImVec4(0.94f, 0.94f, 0.94f, 1.00f)
        style.colors[ImGuiCol.DragDropTarget]    = ImVec4(0.50f, 0.51f, 1.00f, 1.00f)
        style.colors[ImGuiCol.NavHighlight]      = ImVec4(0.27f, 0.29f, 1.00f, 1.00f)
        style.colors[ImGuiCol.NavWindowingHighlight] = ImVec4(0.50f, 0.51f, 1.00f, 1.00f)
        style.colors[ImGuiCol.NavWindowingDimBg] = ImVec4(0.20f, 0.18f, 0.55f, 0.50f)
        style.colors[ImGuiCol.ModalWindowDimBg]  = ImVec4(0.20f, 0.18f, 0.55f, 0.50f)
    }
}