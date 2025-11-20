package kge.editor

import kge.api.std.IGLMesh
import org.joml.Vector3f
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*

class GLMesh(
    val vertices: FloatArray,
    val indices: IntArray? = null,
    override val drawMode: Int = GL_TRIANGLES,
    override val boundsMin: Vector3f = Vector3f(),
    override val boundsMax: Vector3f = Vector3f(),
    val vertexAttributes: VertexAttributes = VertexAttributes.POSITION
) : IGLMesh {

    enum class VertexAttributes(val stride: Int) {
        POSITION(3),           // position 1 2 3
        POSITION_NORMAL(6),    // position + normal 1 2 3 4 5 6
        POSITION_NORMAL_UV(8), // position + normal + UV 1 2 3 4 5 6 7 8
        POSITION_UV(5),        // position + UV 1 2 3 4 5
        POSITION_COLOR(6)      // position + r g b 1 2 3 4 5 6
    }

    override var vao: Int = 0
    override var vbo: Int = 0
    override var ebo: Int = 0

    override val vertexCount: Int
        get() = indices?.size ?: (vertices.size / vertexAttributes.stride)

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

        val strideBytes = vertexAttributes.stride * Float.SIZE_BYTES

        glEnableVertexAttribArray(0)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, strideBytes, 0L)

        if (vertexAttributes == VertexAttributes.POSITION_NORMAL ||
            vertexAttributes == VertexAttributes.POSITION_NORMAL_UV) {
            glEnableVertexAttribArray(1)
            glVertexAttribPointer(1, 3, GL_FLOAT, false, strideBytes, (3 * Float.SIZE_BYTES).toLong())
        }

        if (vertexAttributes == VertexAttributes.POSITION_NORMAL_UV) {
            glEnableVertexAttribArray(2)
            glVertexAttribPointer(2, 2, GL_FLOAT, false, strideBytes, (6 * Float.SIZE_BYTES).toLong())
        } else if (vertexAttributes == VertexAttributes.POSITION_UV) {
            glEnableVertexAttribArray(2)
            glVertexAttribPointer(2, 2, GL_FLOAT, false, strideBytes, (3 * Float.SIZE_BYTES).toLong())
        }

        if (vertexAttributes == VertexAttributes.POSITION_COLOR) {
            glEnableVertexAttribArray(3)
            glVertexAttribPointer(3, 3, GL_FLOAT, false, strideBytes, (3 * Float.SIZE_BYTES).toLong())
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
            idx += vertexAttributes.stride
        }
        boundsMin.set(min)
        boundsMax.set(max)
    }

    companion object {
        fun create(
            vertices: FloatArray,
            vertexAttributes: VertexAttributes = VertexAttributes.POSITION,
            indices: IntArray? = null,
            drawMode: Int = GL_TRIANGLES
        ): GLMesh {
            if (vertices.isEmpty()) throw IllegalArgumentException("vertices array is empty")

            val mesh = GLMesh(vertices.copyOf(), indices?.copyOf(), drawMode, Vector3f(), Vector3f(), vertexAttributes)
            mesh.upload()
            return mesh
        }

        fun fromLines(vertices: FloatArray, indices: IntArray? = null): GLMesh {
            return create(vertices, VertexAttributes.POSITION, indices, GL_LINES)
        }

        fun fromTriangles(vertices: FloatArray, indices: IntArray? = null): GLMesh {
            return create(vertices, VertexAttributes.POSITION, indices, GL_TRIANGLES)
        }

        fun combine(meshes: List<GLMesh>, transform: (Int, Float) -> Float = { _, value -> value }): GLMesh {
            if (meshes.isEmpty()) throw IllegalArgumentException("Cannot combine empty mesh list")

            val firstMesh = meshes.first()

            for (mesh in meshes) {
                if (mesh.vertexAttributes != firstMesh.vertexAttributes) {
                    throw IllegalArgumentException("All meshes must have same vertex attributes")
                }
                if (mesh.drawMode != firstMesh.drawMode) {
                    throw IllegalArgumentException("All meshes must have same draw mode")
                }
            }

            return if (firstMesh.indices != null) {
                combineIndexed(meshes, transform)
            } else {
                combineNonIndexed(meshes, transform)
            }
        }

        private fun combineIndexed(meshes: List<GLMesh>, transform: (Int, Float) -> Float): GLMesh {
            val vertexAttributes = meshes.first().vertexAttributes
            val stride = vertexAttributes.stride

            var totalVertices = 0
            var totalIndices = 0

            for (mesh in meshes) {
                totalVertices += mesh.vertices.size / stride
                totalIndices += mesh.indices!!.size
            }

            val combinedVertices = FloatArray(totalVertices * stride)
            val combinedIndices = IntArray(totalIndices)

            var vertexOffset = 0
            var indexOffset = 0
            var vertexIndex = 0

            for (mesh in meshes) {
                val vertices = mesh.vertices
                val indices = mesh.indices!!

                for (i in vertices.indices) {
                    combinedVertices[vertexOffset + i] = transform(vertexIndex++, vertices[i])
                }
                vertexOffset += vertices.size

                val indexBase = vertexOffset / stride - vertices.size / stride
                for (i in indices.indices) {
                    combinedIndices[indexOffset + i] = indices[i] + indexBase
                }
                indexOffset += indices.size
            }

            return create(combinedVertices, vertexAttributes, combinedIndices, meshes.first().drawMode)
        }

        private fun combineNonIndexed(meshes: List<GLMesh>, transform: (Int, Float) -> Float): GLMesh {
            val vertexAttributes = meshes.first().vertexAttributes

            val totalVertices = meshes.sumOf { it.vertices.size }
            val combinedVertices = FloatArray(totalVertices)

            var offset = 0
            var vertexIndex = 0

            for (mesh in meshes) {
                val vertices = mesh.vertices
                for (i in vertices.indices) {
                    combinedVertices[offset + i] = transform(vertexIndex++, vertices[i])
                }
                offset += vertices.size
            }

            return create(combinedVertices, vertexAttributes, drawMode = meshes.first().drawMode)
        }
    }
}