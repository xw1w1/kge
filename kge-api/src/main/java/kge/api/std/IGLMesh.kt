package kge.api.std

import org.joml.Vector3f

/**
 * Represents a GPU-resident OpenGL mesh.
 *
 * A mesh is a collection of vertex and (optionally) index data
 * uploaded to the GPU for rendering.
 *
 * The mesh stores references to its OpenGL buffer objects (VAO, VBO, and EBO)
 * and provides simple lifecycle management methods such as [bind], [render], and [destroy].
 *
 * Meshes can represent triangles, lines, or points, depending on the [drawMode].
 * **/
interface IGLMesh {
    val vao: Int
    val vbo: Int
    val ebo: Int

    val vertexCount: Int

    val drawMode: Int

    val boundsMin: Vector3f

    val boundsMax: Vector3f

    val hasIndices: Boolean
        get() = ebo != 0

    fun upload()

    fun bind()

    fun render()

    fun unbind()

    fun destroy()
}
