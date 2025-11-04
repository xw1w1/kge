package kge.editor.ui

import imgui.ImFont
import imgui.ImGuiIO

object EditorFont {
    lateinit var regular: ImFont
    lateinit var medium: ImFont
    lateinit var bold: ImFont
    lateinit var italic: ImFont

    fun load(io: ImGuiIO) {
        regular = io.fonts.addFontFromFileTTF("std/fonts/JetBrainsMono-Regular.ttf", 16f)
        medium = io.fonts.addFontFromFileTTF("std/fonts/JetBrainsMono-Medium.ttf", 18f)
        italic = io.fonts.addFontFromFileTTF("std/fonts/JetBrainsMono-Italic.ttf", 16f)
        bold = io.fonts.addFontFromFileTTF("std/fonts/JetBrainsMono-Bold.ttf", 20f)
    }
}