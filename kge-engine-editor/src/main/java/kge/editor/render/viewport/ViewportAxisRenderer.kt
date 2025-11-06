package kge.editor.render.viewport

import kge.editor.GLMesh
import kge.editor.ResourceLoader
import kge.editor.render.ShaderProgram
import org.joml.Matrix4f
import org.lwjgl.opengl.GL11

class ViewportAxisRenderer {
    private var shader: ShaderProgram? = null
    private var mesh: GLMesh? = null
    private var initialized = false

    fun init() {
        if (initialized) return
        shader = ResourceLoader.loadShader(
            "std/shaders/axis.vert",
            "std/shaders/axis.frag"
        )
        mesh = ResourceLoader.loadMesh("std/meshes/AXIS.msh")
        initialized = true
    }

    fun render(viewProj: Matrix4f) {
        if (!initialized) return
        val sh = shader ?: return
        val m = mesh ?: return

        sh.bind()
        sh.setUniformMat4("u_ViewProj", viewProj)
        GL11.glLineWidth(0.2f)
        m.render()
        sh.unbind()
    }
}