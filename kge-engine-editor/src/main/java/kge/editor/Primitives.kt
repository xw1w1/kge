package kge.editor

object Primitives {
    private val loadedMeshes = mutableMapOf<String, GLMesh>()

    fun cube(): GLMesh = loadOrGet("std/meshes/CUBE.msh")
    fun plane(): GLMesh = loadOrGet("std/meshes/PLANE.msh")
    fun sphere(): GLMesh = loadOrGet("std/meshes/ICOSPHERE.msh")
    fun cylinder(): GLMesh = loadOrGet("std/meshes/CYLINDER.msh")
    fun octagon(): GLMesh = loadOrGet("std/meshes/OCTAGON.msh")
    fun pyramid(): GLMesh = loadOrGet("std/meshes/PYRAMID.msh")
    fun polygon(): GLMesh = loadOrGet("std/meshes/POLYGON.msh")
    fun torus(): GLMesh = loadOrGet("std/meshes/TORUS.msh")
    fun cone(): GLMesh = loadOrGet("std/meshes/CONE.msh")

    fun d_Axis(): GLMesh = loadOrGet("std/meshes/AXIS.msh")
    fun d_Grid(): GLMesh = loadOrGet("std/meshes/GRID.msh")

    private fun loadOrGet(path: String): GLMesh {
        return loadedMeshes.getOrPut(path) {
            ResourceLoader.loadMesh(path)
        }
    }

}
