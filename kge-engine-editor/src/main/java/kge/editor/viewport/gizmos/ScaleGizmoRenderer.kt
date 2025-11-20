package kge.editor.viewport.gizmos

import kge.api.editor.EditorSelectionGizmoAxis
import kge.editor.EditorApplication
import kge.editor.GLMesh
import kge.editor.render.ShaderProgram
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.opengl.GL11

class ScaleGizmoRenderer {
    private lateinit var xLine: GLMesh
    private lateinit var yLine: GLMesh
    private lateinit var zLine: GLMesh
    private lateinit var xCube: GLMesh
    private lateinit var yCube: GLMesh
    private lateinit var zCube: GLMesh
    private lateinit var centerCube: GLMesh
    private lateinit var shader: ShaderProgram
    var initialized = false

    fun init(shaderProgram: ShaderProgram) {
        if (initialized) return
        shader = shaderProgram
        xLine = buildLineMesh()
        yLine = buildLineMesh()
        zLine = buildLineMesh()
        xCube = buildCubeMesh()
        yCube = buildCubeMesh()
        zCube = buildCubeMesh()
        centerCube = buildCubeMesh()
        initialized = true
    }

    private fun buildLineMesh(): GLMesh {
        val vertices = floatArrayOf(
            0f, 0f, 0f,
            0f, 1f, 0f
        )
        val indices = intArrayOf(0, 1)
        return GLMesh.fromLines(vertices, indices)
    }

    private fun buildCubeMesh(): GLMesh {
        val vertices = floatArrayOf(
            -0.5f, -0.5f,  0.5f,  0.5f, -0.5f,  0.5f,  0.5f,  0.5f,  0.5f, -0.5f,  0.5f,  0.5f,
            -0.5f, -0.5f, -0.5f, -0.5f,  0.5f, -0.5f,  0.5f,  0.5f, -0.5f,  0.5f, -0.5f, -0.5f,
            -0.5f,  0.5f, -0.5f, -0.5f,  0.5f,  0.5f,  0.5f,  0.5f,  0.5f,  0.5f,  0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,  0.5f, -0.5f, -0.5f,  0.5f, -0.5f,  0.5f, -0.5f, -0.5f,  0.5f,
            0.5f, -0.5f, -0.5f,  0.5f,  0.5f, -0.5f,  0.5f,  0.5f,  0.5f,  0.5f, -0.5f,  0.5f,
            -0.5f, -0.5f, -0.5f, -0.5f, -0.5f,  0.5f, -0.5f,  0.5f,  0.5f, -0.5f,  0.5f, -0.5f
        )

        val indices = intArrayOf(
            0, 1, 2, 0, 2, 3,
            4, 5, 6, 4, 6, 7,
            8, 9, 10, 8, 10, 11,
            12, 13, 14, 12, 14, 15,
            16, 17, 18, 16, 18, 19,
            20, 21, 22, 20, 22, 23
        )

        return GLMesh.fromTriangles(vertices, indices)
    }

    fun render(viewProj: Matrix4f, pos: Vector3f, activeAxis: EditorSelectionGizmoAxis?, cameraPos: Vector3f) {
        if (!initialized) return
        shader.bind()

        val camDist = pos.distance(cameraPos)
        val scale = (camDist * 0.25f).coerceIn(0.2f, 2.2F)
        val cubeScale = 0.1f * scale
        val lineLength = 0.8f * scale

        fun draw(mesh: GLMesh, color: Vector3f, rotation: Matrix4f = Matrix4f(), s: Float = 1f) {
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_CULL_FACE)
            EditorApplication.getInstance().getRenderPipeline().quickDraw(
                mesh, shader, color, pos, rotation, Vector3f(s), viewProj
            )
            GL11.glEnable(GL11.GL_CULL_FACE)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
        }

        val colX = if (activeAxis == EditorSelectionGizmoAxis.SCALE_X) Vector3f(1f, 0.65f, 0.65f) else Vector3f(1f, 0f, 0f)
        val colY = if (activeAxis == EditorSelectionGizmoAxis.SCALE_Y) Vector3f(0.65f, 1f, 0.65f) else Vector3f(0f, 1f, 0f)
        val colZ = if (activeAxis == EditorSelectionGizmoAxis.SCALE_Z) Vector3f(0.65f, 0.65f, 1f) else Vector3f(0f, 0f, 1f)
        val colCenter = if (activeAxis == EditorSelectionGizmoAxis.SCALE_CENTER) Vector3f(1f, 1f, 0.65f) else Vector3f(1f, 1f, 0f)

        draw(xLine, colX, Matrix4f().rotateZ(-Math.PI.toFloat() / 2f), lineLength)
        draw(yLine, colY, Matrix4f(), lineLength)
        draw(zLine, colZ, Matrix4f().rotateX(Math.PI.toFloat() / 2f), lineLength)

        draw(xCube, colX, Matrix4f().rotateZ(-Math.PI.toFloat() / 2f).translate(0f, lineLength, 0f), cubeScale)
        draw(yCube, colY, Matrix4f().translate(0f, lineLength, 0f), cubeScale)
        draw(zCube, colZ, Matrix4f().rotateX(Math.PI.toFloat() / 2f).translate(0f, lineLength, 0f), cubeScale)

        draw(centerCube, colCenter, Matrix4f(), cubeScale * 1.5f)

        shader.unbind()
    }
}