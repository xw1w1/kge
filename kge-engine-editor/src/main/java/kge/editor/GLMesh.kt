package kge.editor

import kge.api.std.IGLMesh
import org.joml.Vector3f
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*

class GLMesh(
    private val vertices: FloatArray,
    private val indices: IntArray? = null,
    override val drawMode: Int = GL_TRIANGLES,
    override val boundsMin: Vector3f = Vector3f(),
    override val boundsMax: Vector3f = Vector3f(),
    private val strideFloats: Int = 3
) : IGLMesh {

    override var vao: Int = 0
    override var vbo: Int = 0
    override var ebo: Int = 0

    override val vertexCount: Int
        get() = indices?.size ?: (vertices.size / strideFloats)

    var uploaded = false

    override fun upload() {
        if (uploaded) return
        uploaded = true

        computeBounds()

        vao = glGenVertexArrays()
        vbo = glGenBuffers()
        ebo = if (indices != null) glGenBuffers() else 0

        glBindVertexArray(vao)
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

        val strideBytes = strideFloats * Float.SIZE_BYTES

        // position at location 0 (3f)
        glEnableVertexAttribArray(0)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, strideBytes, 0L)

        // if stride >= 6 next 3 floats=normal attrib 1
        if (strideFloats >= 6) {
            glEnableVertexAttribArray(1)
            glVertexAttribPointer(1, 3, GL_FLOAT, false, strideBytes, (3 * Float.SIZE_BYTES).toLong())
        }

        // if stride >= 8 texCoord offset 6 (2 floats)
        if (strideFloats >= 8) {
            glEnableVertexAttribArray(2)
            glVertexAttribPointer(2, 2, GL_FLOAT, false, strideBytes, (6 * Float.SIZE_BYTES).toLong())
        }

        if (indices != null) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)
        }

        glBindVertexArray(0)
    }

    override fun bind() {
        if (!uploaded) upload()
        glBindVertexArray(vao)
    }

    override fun render() {
        bind()
        if (indices != null && indices.isNotEmpty()) {
            glDrawElements(drawMode, vertexCount, GL_UNSIGNED_INT, 0L)
        } else {
            glDrawArrays(drawMode, 0, vertexCount)
        }
        unbind()
    }

    override fun unbind() = glBindVertexArray(0)

    override fun destroy() {
        if (vao != 0) glDeleteVertexArrays(vao)
        if (vbo != 0) glDeleteBuffers(vbo)
        if (ebo != 0) glDeleteBuffers(ebo)
        vao = 0
        vbo = 0
        ebo = 0
        uploaded = false
    }

    private fun computeBounds() {
        if (vertices.isEmpty()) return
        var idx = 0
        val min = Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        val max = Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY)
        while (idx < vertices.size) {
            val x = vertices[idx]
            val y = vertices.getOrElse(idx + 1) { 0f }
            val z = vertices.getOrElse(idx + 2) { 0f }
            min.min(Vector3f(x, y, z))
            max.max(Vector3f(x, y, z))
            idx += strideFloats
        }
        boundsMin.set(min)
        boundsMax.set(max)
    }

    companion object {
        fun create(
            vertices: FloatArray,
            strideFloats: Int = 3,
            indices: IntArray? = null,
            drawMode: Int = GL_TRIANGLES
        ): GLMesh {
            if (vertices.isEmpty()) throw IllegalArgumentException("vertices array is empty")

            val mesh = GLMesh(vertices.copyOf(), indices?.copyOf(), drawMode, Vector3f(), Vector3f(), strideFloats)

            mesh.upload()
            return mesh
        }

        fun fromTriangles(vertices: FloatArray, indices: IntArray? = null): GLMesh {
            return create(vertices, strideFloats = 3, indices = indices, drawMode = GL_TRIANGLES)
        }
    }
}
