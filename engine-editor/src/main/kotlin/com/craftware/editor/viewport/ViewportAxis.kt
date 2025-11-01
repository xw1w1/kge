package com.craftware.editor.viewport

import com.craftware.editor.ResourceLoader
import com.craftware.editor.ui.impl.ViewportSettings
import com.craftware.engine.render.GLMesh
import com.craftware.engine.render.ShaderProgram
import org.joml.Matrix4f
import org.lwjgl.opengl.GL11

object ViewportAxis {
    private var shader: ShaderProgram? = null
    private var mesh: GLMesh? = null
    private var initialized = false

    fun init() {
        if (initialized) return
        shader = ResourceLoader.loadShader(
            "standard/shaders/axis.vert",
            "standard/shaders/axis.frag"
        )
        mesh = ResourceLoader.loadMesh("standard/meshes/AXIS.msh")
        initialized = true
    }

    fun render(viewProj: Matrix4f) {
        if (!initialized || !ViewportSettings.showAxis) return
        val sh = shader ?: return
        val m = mesh ?: return

        sh.bind()
        sh.setUniformMat4("u_ViewProj", viewProj)
        GL11.glLineWidth(ViewportSettings.axisLineWidth)
        m.render()
        sh.unbind()
    }
}
