package kge.editor

import org.joml.Matrix4f
import org.joml.Vector3f

// EditorRenderPipeline
class MeshBatcher {
    private val meshes = mutableListOf<GLMesh>()
    private val transforms = mutableListOf<Matrix4f>()

    fun addMesh(mesh: GLMesh, transform: Matrix4f = Matrix4f()) {
        meshes.add(mesh)
        transforms.add(transform)
    }

    fun createBatch(): GLMesh {
        val meshGroups = meshes.groupBy { it.vertexAttributes to it.drawMode }

        val batchedMeshes = mutableListOf<GLMesh>()

        for ((_, group) in meshGroups) {
            if (group.size == 1) {
                batchedMeshes.add(group.first())
            } else {
                val transformedMeshes = group.mapIndexed { _, mesh ->
                    applyTransformToMesh(mesh, transforms[meshes.indexOf(mesh)])
                }
                val combined = GLMesh.combine(transformedMeshes)
                batchedMeshes.add(combined)
            }
        }

        return if (batchedMeshes.size == 1) {
            batchedMeshes.first()
        } else {
            GLMesh.combine(batchedMeshes)
        }
    }

    private fun applyTransformToMesh(mesh: GLMesh, transform: Matrix4f): GLMesh {
        val originalVertices = mesh.vertices
        val stride = mesh.vertexAttributes.stride
        val transformedVertices = FloatArray(originalVertices.size)

        for (i in originalVertices.indices step stride) {
            if (i + 2 < originalVertices.size) {
                val position = Vector3f(originalVertices[i], originalVertices[i + 1], originalVertices[i + 2])
                transform.transformPosition(position)

                transformedVertices[i] = position.x
                transformedVertices[i + 1] = position.y
                transformedVertices[i + 2] = position.z

                for (j in 3 until stride) {
                    if (i + j < originalVertices.size) {
                        transformedVertices[i + j] = originalVertices[i + j]
                    }
                }
            }
        }

        return GLMesh.create(transformedVertices, mesh.vertexAttributes, mesh.indices?.copyOf(), mesh.drawMode)
    }

    fun clear() {
        meshes.clear()
        transforms.clear()
    }
}