package kge.ui.toolkit

import imgui.ImFont
import imgui.ImGuiIO

object EditorFont {
    lateinit var regular: ImFont
    lateinit var medium: ImFont
    lateinit var bold: ImFont
    lateinit var semiBold: ImFont
    lateinit var italic: ImFont

    fun load(io: ImGuiIO) {
        regular = io.fonts.addFontFromMemoryTTF(assetBytes("std/fonts/JetBrainsMono-Regular.ttf"), 18f)
        medium = io.fonts.addFontFromMemoryTTF(assetBytes("std/fonts/JetBrainsMono-Medium.ttf"), 20f)
        italic = io.fonts.addFontFromMemoryTTF(assetBytes("std/fonts/JetBrainsMono-Italic.ttf"), 18f)
        bold = io.fonts.addFontFromMemoryTTF(assetBytes("std/fonts/JetBrainsMono-Bold.ttf"), 24f)
        semiBold = io.fonts.addFontFromMemoryTTF(assetBytes("std/fonts/JetBrainsMono-Bold.ttf"), 21f)
    }

    fun assetBytes(assetPath: String): ByteArray {
        return this.javaClass.classLoader.getResourceAsStream(assetPath).readAllBytes()
    }
}