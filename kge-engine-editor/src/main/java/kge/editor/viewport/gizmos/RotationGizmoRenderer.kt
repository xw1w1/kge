package kge.editor.viewport.gizmos

import kge.api.editor.EditorSelectionGizmoAxis
import kge.editor.EditorApplication
import kge.editor.GLMesh
import kge.editor.render.ShaderProgram
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class RotationGizmoRenderer {
    private lateinit var xTorus: GLMesh
    private lateinit var yTorus: GLMesh
    private lateinit var zTorus: GLMesh
    private lateinit var screenCircle: GLMesh
    private lateinit var shader: ShaderProgram
    var initialized = false

    fun init(shaderProgram: ShaderProgram) {
        if (initialized) return
        shader = shaderProgram
        xTorus = buildTorusMesh()
        yTorus = buildTorusMesh()
        zTorus = buildTorusMesh()
        screenCircle = buildScreenCircleMesh()
        initialized = true
    }

    private fun buildTorusMesh(): GLMesh {
        val vertices = ArrayList<Float>()
        val indices = ArrayList<Int>()

        val radius = 1.0f
        val thickness = 0.008f
        val segments = 32
        val tubeSegments = 12

        for (i in 0..segments) {
            val u = (i.toFloat() / segments) * (Math.PI * 2.0).toFloat()
            val cosU = cos(u)
            val sinU = sin(u)

            for (j in 0..tubeSegments) {
                val v = (j.toFloat() / tubeSegments) * (Math.PI * 2.0).toFloat()
                val cosV = cos(v)
                val sinV = sin(v)

                val x = (radius + thickness * cosV) * cosU
                val y = (radius + thickness * cosV) * sinU
                val z = thickness * sinV

                vertices.add(x)
                vertices.add(y)
                vertices.add(z)
            }
        }

        for (i in 0 until segments) {
            for (j in 0 until tubeSegments) {
                val nextI = (i + 1) % (segments + 1)
                val nextJ = (j + 1) % (tubeSegments + 1)

                val i0 = i * (tubeSegments + 1) + j
                val i1 = nextI * (tubeSegments + 1) + j
                val i2 = nextI * (tubeSegments + 1) + nextJ
                val i3 = i * (tubeSegments + 1) + nextJ

                indices.add(i0); indices.add(i1); indices.add(i2)
                indices.add(i0); indices.add(i2); indices.add(i3)
            }
        }

        return GLMesh.fromTriangles(vertices.toFloatArray(), indices.toIntArray())
    }

    private fun buildScreenCircleMesh(): GLMesh {
        val vertices = ArrayList<Float>()
        val indices = ArrayList<Int>()

        val radius = 1.6f
        val thickness = 0.005f
        val segments = 32

        for (i in 0..segments) {
            val angle = (i.toFloat() / segments) * (Math.PI * 2.0).toFloat()
            val x = radius * cos(angle)
            val y = radius * sin(angle)
            vertices.add(x); vertices.add(y); vertices.add(0f)

            val innerX = (radius - thickness) * cos(angle)
            val innerY = (radius - thickness) * sin(angle)
            vertices.add(innerX); vertices.add(innerY); vertices.add(0f)
        }

        for (i in 0 until segments) {
            val base = i * 2
            indices.add(base); indices.add(base + 1); indices.add(base + 3)
            indices.add(base); indices.add(base + 3); indices.add(base + 2)
        }

        return GLMesh.fromTriangles(vertices.toFloatArray(), indices.toIntArray())
    }

    fun render(viewProj: Matrix4f, pos: Vector3f, activeAxis: EditorSelectionGizmoAxis?, cameraPos: Vector3f) {
        if (!initialized) return
        shader.bind()

        val camDist = pos.distance(cameraPos)
        val scale = (camDist * 0.25f).coerceIn(0.2f, 2.2F)

        fun draw(mesh: GLMesh, color: Vector3f, rotation: Matrix4f = Matrix4f(), s: Float = 1f) {
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_CULL_FACE)
            EditorApplication.getInstance().getRenderPipeline().quickDraw(
                mesh, shader, color, pos, rotation, Vector3f(s), viewProj
            )
            GL11.glEnable(GL11.GL_CULL_FACE)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
        }

        val colX = if (activeAxis == EditorSelectionGizmoAxis.ROTATE_X) Vector3f(1f, 0.65f, 0.65f) else Vector3f(1f, 0f, 0f)
        val colY = if (activeAxis == EditorSelectionGizmoAxis.ROTATE_Y) Vector3f(0.65f, 1f, 0.65f) else Vector3f(0f, 1f, 0f)
        val colZ = if (activeAxis == EditorSelectionGizmoAxis.ROTATE_Z) Vector3f(0.65f, 0.65f, 1f) else Vector3f(0f, 0f, 1f)
        val colScreen = if (activeAxis == EditorSelectionGizmoAxis.ROTATE_CENTER) Vector3f(1f, 1f, 0.65f) else Vector3f(1f, 1f, 0f)

        draw(xTorus, colX, Matrix4f().rotateY(Math.PI.toFloat() / 2f), scale)
        draw(yTorus, colY, Matrix4f().rotateX(Math.PI.toFloat() / 2f), scale)
        draw(zTorus, colZ, Matrix4f(), scale)

        val screenRotation = calculateBillboardRotation(cameraPos, pos)
        draw(screenCircle, colScreen, screenRotation, scale)

        shader.unbind()
    }

    private fun calculateBillboardRotation(cameraPos: Vector3f, gizmoPos: Vector3f): Matrix4f {
        val toCamera = Vector3f(cameraPos).sub(gizmoPos).normalize()

        val up = Vector3f(0f, 1f, 0f)
        val right: Vector3f
        val actualUp: Vector3f

        if (abs(toCamera.dot(up)) > 0.9999f) {
            right = Vector3f(1f, 0f, 0f)
            actualUp = toCamera.cross(right, Vector3f()).normalize()
        } else {
            right = toCamera.cross(up, Vector3f()).normalize()
            actualUp = right.cross(toCamera, Vector3f()).normalize()
        }

        return Matrix4f().set(
            right.x, right.y, right.z, 0f,
            actualUp.x, actualUp.y, actualUp.z, 0f,
            -toCamera.x, -toCamera.y, -toCamera.z, 0f,
            0f, 0f, 0f, 1f
        )
    }
}