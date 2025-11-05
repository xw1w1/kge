package kge.api.std

import kge.api.editor.imgui.IRenderCallback

interface IRenderable {
    fun render(delta: Float)
    fun pushRenderCallback(cb: IRenderCallback)
}