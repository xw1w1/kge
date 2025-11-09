package kge.editor.render

import kge.editor.GLMesh
import kge.editor.ResourceLoader
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.opengl.GL11

class ViewportGridRenderer {
    private var mesh: GLMesh? = null
    private var shader: ShaderProgram? = null
    private var initialized = false

    fun init() {
        if (initialized) return

        shader = ResourceLoader.loadShader(
            "std/shaders/grid.vert",
            "std/shaders/grid.frag"
        )

        val size = 200
        val step = 1
        val color = floatArrayOf(0.4f, 0.4f, 0.4f)

        val vertices = mutableListOf<Float>()
        for (i in -size..size step step) {
            val z = i.toFloat()
            vertices.addAll(listOf(-size.toFloat(), 0f, z, *color.toTypedArray()))
            vertices.addAll(listOf(size.toFloat(), 0f, z, *color.toTypedArray()))
            val x = i.toFloat()
            vertices.addAll(listOf(x, 0f, -size.toFloat(), *color.toTypedArray()))
            vertices.addAll(listOf(x, 0f, size.toFloat(), *color.toTypedArray()))
        }

        mesh = GLMesh.create(vertices.toFloatArray(), strideFloats = 6, drawMode = GL11.GL_LINES)
        initialized = true
    }

    fun render(viewProj: Matrix4f) {
        if (!initialized) return

        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        shader!!.bind()
        shader!!.setUniformMat4("u_ViewProj", viewProj)
        shader!!.setUniform3f("u_GridColor", Vector3f(0.35f, 0.35f, 0.35f))
        shader!!.setUniform3f("u_HorizonColor", Vector3f(0.08f, 0.09f, 0.10f))

        GL11.glLineWidth(1f)
        mesh!!.render()
        shader!!.unbind()

        GL11.glDisable(GL11.GL_BLEND)
    }
}
