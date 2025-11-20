package kge.editor.viewport.gizmos

import kge.api.editor.EditorSelectionGizmoAxis
import kge.editor.EditorApplication
import kge.editor.GLMesh
import kge.editor.render.ShaderProgram
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import kotlin.math.cos
import kotlin.math.sin

class TranslationGizmoRenderer {
    private lateinit var xArrow: GLMesh
    private lateinit var yArrow: GLMesh
    private lateinit var zArrow: GLMesh
    private lateinit var centerSphere: GLMesh
    private lateinit var planeTriangle: GLMesh
    private lateinit var shader: ShaderProgram
    var initialized = false

    fun init(shaderProgram: ShaderProgram) {
        if (initialized) return
        shader = shaderProgram
        xArrow = buildArrowMesh()
        yArrow = buildArrowMesh()
        zArrow = buildArrowMesh()
        centerSphere = buildSphereMesh(3, 3)
        planeTriangle = buildPlaneAxis()
        initialized = true
    }

    private fun buildArrowMesh(): GLMesh {
        val vertices = ArrayList<Float>()
        val idx = ArrayList<Int>()
        val segments = 16
        val shaftLen = 0.8f
        val shaftRad = 0.005f
        val headLen = 0.18f
        val headRad = 0.06f

        for (i in 0..segments) {
            val a = (i.toFloat() / segments) * (Math.PI * 2.0).toFloat()
            val cx = cos(a); val cz = sin(a)
            vertices.add(cx * shaftRad); vertices.add(0f); vertices.add(cz * shaftRad)
            vertices.add(cx * shaftRad); vertices.add(shaftLen); vertices.add(cz * shaftRad)
        }

        for (i in 0 until segments) {
            val b = i * 2
            idx.add(b); idx.add(b + 3); idx.add(b + 1)
            idx.add(b); idx.add(b + 2); idx.add(b + 3)
        }

        val baseIndex = vertices.size / 3
        for (i in 0..segments) {
            val a = (i.toFloat() / segments) * (Math.PI * 2.0).toFloat()
            val cx = cos(a); val cz = sin(a)
            vertices.add(cx * headRad); vertices.add(shaftLen); vertices.add(cz * headRad)
        }

        val tipIndex = vertices.size / 3
        vertices.add(0f); vertices.add(shaftLen + headLen); vertices.add(0f)

        for (i in 0 until segments) {
            idx.add(baseIndex + i); idx.add(tipIndex); idx.add(baseIndex + i + 1)
        }

        return GLMesh.fromTriangles(vertices.toFloatArray(), idx.toIntArray())
    }

    private fun buildSphereMesh(stacks: Int, slices: Int): GLMesh {
        val vertices = ArrayList<Float>()
        val idx = ArrayList<Int>()
        val radius = 0.1f
        val st = stacks.coerceAtLeast(6)
        val sl = slices.coerceAtLeast(8)
        for (i in 0..st) {
            val phi = Math.PI * i / st
            for (j in 0..sl) {
                val theta = 2.0 * Math.PI * j / sl
                val x = (sin(phi) * cos(theta)).toFloat()
                val y = cos(phi).toFloat()
                val z = (sin(phi) * sin(theta)).toFloat()
                vertices.add(x * radius); vertices.add(y * radius); vertices.add(z * radius)
            }
        }
        for (i in 0 until st) {
            for (j in 0 until sl) {
                val first = i * (sl + 1) + j
                val second = first + sl + 1
                idx.add(first); idx.add(second); idx.add(first + 1)
                idx.add(second); idx.add(second + 1); idx.add(first + 1)
            }
        }
        return GLMesh.fromTriangles(vertices.toFloatArray(), idx.toIntArray())
    }

    private fun buildPlaneAxis(): GLMesh {
        val segments = 16
        val radius = 1f

        val vertices = mutableListOf<Float>()
        val indices = mutableListOf<Int>()

        vertices.add(0f); vertices.add(0f); vertices.add(0f)

        for (i in 0..segments) {
            val angle = (Math.PI / 2.0 * i / segments).toFloat()
            val x = radius * cos(angle.toDouble()).toFloat()
            val y = radius * sin(angle.toDouble()).toFloat()
            vertices.add(x)
            vertices.add(y)
            vertices.add(0f)
        }

        val baseIndex = vertices.size / 3
        vertices.add(0f); vertices.add(0f); vertices.add(-0.001f)

        for (i in 0..segments) {
            val angle = (Math.PI / 2.0 * i / segments).toFloat()
            val x = radius * cos(angle.toDouble()).toFloat()
            val y = radius * sin(angle.toDouble()).toFloat()
            vertices.add(x)
            vertices.add(y)
            vertices.add(-0.001f)
        }

        for (i in 1..segments) {
            indices.add(0)
            indices.add(i)
            indices.add(i + 1)
        }

        for (i in 1..segments) {
            indices.add(baseIndex)
            indices.add(baseIndex + i + 1)
            indices.add(baseIndex + i)
        }

        return GLMesh.fromTriangles(vertices.toFloatArray(), indices.toIntArray())
    }

    fun render(viewProj: Matrix4f, pos: Vector3f, activeAxis: EditorSelectionGizmoAxis?, cameraPos: Vector3f) {
        if (!initialized) return
        shader.bind()

        val camDist = pos.distance(cameraPos)
        val scale = (camDist * 0.25f)
            .coerceIn(0.2f, 2.2F)
        val axisLength = 1.0f * scale
        val sphereScale = 1.0f * scale
        val planeScale = 0.35f * axisLength

        fun draw(mesh: GLMesh, color: Vector3f, rotation: Matrix4f = Matrix4f(), s: Float = 1f) {
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_CULL_FACE)
            EditorApplication.getInstance().getRenderPipeline().quickDraw(
                mesh, shader, color, pos, rotation, Vector3f(s), viewProj
            )
            GL11.glEnable(GL11.GL_CULL_FACE)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
        }

        val colX = if (activeAxis == EditorSelectionGizmoAxis.TRANSLATE_X) Vector3f(1f, 0.65f, 0.65f) else Vector3f(
            1f,
            0f,
            0f
        )
        val colY = if (activeAxis == EditorSelectionGizmoAxis.TRANSLATE_Y) Vector3f(0.65f, 1f, 0.65f) else Vector3f(
            0f,
            1f,
            0f
        )
        val colZ = if (activeAxis == EditorSelectionGizmoAxis.TRANSLATE_Z) Vector3f(0.65f, 0.65f, 1f) else Vector3f(
            0f,
            0f,
            1f
        )
        val colC = if (activeAxis == EditorSelectionGizmoAxis.TRANSLATE_CENTER) Vector3f(1f, 1f, 0.5f) else Vector3f(
            1f,
            1f,
            0f
        )

        val colPlaneXY = if (activeAxis == EditorSelectionGizmoAxis.TRANSLATE_PLANE_XY) Vector3f(
            0.65f,
            0.65f,
            0.90f
        ) else Vector3f(0f, 0f, 0.6f)
        val colPlaneXZ = if (activeAxis == EditorSelectionGizmoAxis.TRANSLATE_PLANE_XZ) Vector3f(
            0.65f,
            0.90f,
            0.65f
        ) else Vector3f(0f, 0.6f, 0f)
        val colPlaneYZ = if (activeAxis == EditorSelectionGizmoAxis.TRANSLATE_PLANE_ZY) Vector3f(
            0.90f,
            0.65f,
            0.65f
        ) else Vector3f(0.6f, 0f, 0f)

        val centerOffset: Float = 0.6f * planeScale
        // XY
        draw(planeTriangle, colPlaneXY, Matrix4f().translate(centerOffset, centerOffset, 0f), planeScale)
        // XZ
        draw(planeTriangle, colPlaneXZ, Matrix4f().rotateX((Math.PI.toFloat()) / 2f).translate(centerOffset, centerOffset, 0f), planeScale)
        // YZ
        draw(planeTriangle, colPlaneYZ, Matrix4f().rotateZ((Math.PI.toFloat()) / 2f).rotateX(Math.PI.toFloat() / 2f).translate(centerOffset, centerOffset, 0f), planeScale)

        // X
        draw(xArrow, colX, Matrix4f().rotateZ(-Math.PI.toFloat() / 2f), scale)
        // Y
        draw(yArrow, colY, Matrix4f(), scale)
        // Z
        draw(zArrow, colZ, Matrix4f().rotateX(Math.PI.toFloat() / 2f), scale)
        // center
        draw(centerSphere, colC, Matrix4f(), sphereScale)

        shader.unbind()
    }
}