package kge.editor.render

import kge.api.render.ICamera
import kge.api.render.IRenderPipeline
import kge.api.std.IScene
import kge.editor.GLMesh
import org.joml.Matrix4f
import org.joml.Vector3f

class DefaultEditorRenderPipeline : IRenderPipeline {
    override fun initialize() {
        TODO("Not yet implemented")
    }

    override fun render(scene: IScene, camera: ICamera) {
        TODO("Not yet implemented")
    }

    fun quickDraw(
        mesh: GLMesh,
        shader: ShaderProgram,
        color: Vector3f,
        position: Vector3f,
        rotation: Matrix4f = Matrix4f(),
        scale: Vector3f = Vector3f(1f),
        viewProjection: Matrix4f
    ) {
        val model = Matrix4f().translate(position).mul(rotation).scale(scale)
        shader.setUniformMat4("u_Model", model)
        shader.setUniformMat4("u_ViewProj", viewProjection)
        shader.setUniform3f("u_Color", color)
        mesh.render()
    }

    override fun resize(width: Int, height: Int) {
        TODO("Not yet implemented")
    }

    override fun beginFrame() {
        TODO("Not yet implemented")
    }

    override fun endFrame() {
        TODO("Not yet implemented")
    }

    override fun destroy() {
        TODO("Not yet implemented")
    }
}