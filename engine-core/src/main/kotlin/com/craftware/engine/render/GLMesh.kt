package com.craftware.engine.render

import kge.api.std.IGLMesh
import org.joml.Vector3f
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer
import java.nio.IntBuffer

/**
 * A modern GPU mesh implementation that wraps OpenGL VAO/VBO/EBO.
 *
 * Provides utility factory methods [create] and [fromTriangles].
 * Automatically computes bounds, uploads data to GPU, and allows clean rendering.
 */
class GLMesh(
    private val vertices: FloatArray,
    private val indices: IntArray? = null,
    override val drawMode: Int = GL_TRIANGLES
) : IGLMesh {

    override var vao: Int = 0
    override var vbo: Int = 0
    override var ebo: Int = 0

    override val vertexCount: Int
        get() = indices?.size ?: (vertices.size / 3)

    override val boundsMin = Vector3f()
    override val boundsMax = Vector3f()

    private var uploaded = false

    // === Lifecycle ===

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

        glEnableVertexAttribArray(0)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.SIZE_BYTES, 0L)

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
        if (hasIndices)
            glDrawElements(drawMode, vertexCount, GL_UNSIGNED_INT, 0L)
        else
            glDrawArrays(drawMode, 0, vertexCount)
        unbind()
    }

    override fun unbind() = glBindVertexArray(0)

    override fun destroy() {
        glDeleteVertexArrays(vao)
        glDeleteBuffers(vbo)
        if (ebo != 0) glDeleteBuffers(ebo)
        vao = 0
        vbo = 0
        ebo = 0
        uploaded = false
    }


    private fun computeBounds() {
        if (vertices.isEmpty()) return
        boundsMin.set(vertices[0], vertices[1], vertices[2])
        boundsMax.set(boundsMin)
        for (i in vertices.indices step 3) {
            val x = vertices[i]
            val y = vertices[i + 1]
            val z = vertices[i + 2]
            boundsMin.min(Vector3f(x, y, z))
            boundsMax.max(Vector3f(x, y, z))
        }
    }

    companion object {
        fun create(
            vertices: FloatArray,
            strideFloats: Int = 0,
            indices: IntArray? = null,
            drawMode: Int = GL_TRIANGLES
        ): GLMesh {
            if (vertices.isEmpty()) throw IllegalArgumentException("vertices array is empty")

            val stride = when {
                strideFloats > 0 -> strideFloats
                vertices.size % 6 == 0 -> 6
                vertices.size % 3 == 0 -> 3
                else -> 3
            }

            val boundsMin = Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            val boundsMax = Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY)

            var i = 0
            while (i < vertices.size) {
                val x = vertices[i]
                val y = vertices.getOrElse(i + 1) { 0f }
                val z = vertices.getOrElse(i + 2) { 0f }

                boundsMin.x = minOf(boundsMin.x, x)
                boundsMin.y = minOf(boundsMin.y, y)
                boundsMin.z = minOf(boundsMin.z, z)

                boundsMax.x = maxOf(boundsMax.x, x)
                boundsMax.y = maxOf(boundsMax.y, y)
                boundsMax.z = maxOf(boundsMax.z, z)

                i += stride
            }

            val vao = glGenVertexArrays()
            glBindVertexArray(vao)

            val vbo = glGenBuffers()
            val vBuf: FloatBuffer = MemoryUtil.memAllocFloat(vertices.size)
            vBuf.put(vertices).flip()
            glBindBuffer(GL_ARRAY_BUFFER, vbo)
            glBufferData(GL_ARRAY_BUFFER, vBuf, GL_STATIC_DRAW)

            val strideBytes = stride * Float.SIZE_BYTES
            glEnableVertexAttribArray(0)
            glVertexAttribPointer(0, 3, GL_FLOAT, false, strideBytes, 0L)

            if (stride >= 6) {
                glEnableVertexAttribArray(1)
                glVertexAttribPointer(1, 3, GL_FLOAT, false, strideBytes, (3 * Float.SIZE_BYTES).toLong())
            }

            var ebo = 0

            if (indices != null && indices.isNotEmpty()) {
                ebo = glGenBuffers()
                val iBuf: IntBuffer = MemoryUtil.memAllocInt(indices.size)
                iBuf.put(indices).flip()
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
                glBufferData(GL_ELEMENT_ARRAY_BUFFER, iBuf, GL_STATIC_DRAW)
                MemoryUtil.memFree(iBuf)
            }

            glBindVertexArray(0)
            MemoryUtil.memFree(vBuf)

            return GLMesh(vertices, indices, drawMode).apply {
                this.vao = vao
                this.vbo = vbo
                this.ebo = ebo
                this.boundsMin.set(boundsMin)
                this.boundsMax.set(boundsMax)
                this.uploaded = true
            }
        }

        fun fromTriangles(vertices: FloatArray, indices: IntArray? = null): GLMesh {
            return create(vertices, strideFloats = 3, indices = indices, drawMode = GL_TRIANGLES)
        }
    }
}
