package kge.api.std

import kge.api.editor.imgui.IRenderCallback

interface IRenderable {
    fun render()
    fun pushRenderCallback(cb: IRenderCallback)
}