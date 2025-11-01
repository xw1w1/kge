package com.craftware.editor

import com.craftware.engine.render.GLMesh

object Primitives {
    private val loadedMeshes = mutableMapOf<String, GLMesh>()

    fun cube(): GLMesh = loadOrGet("standard/meshes/CUBE.msh")
    fun plane(): GLMesh = loadOrGet("standard/meshes/PLANE.msh")
    fun sphere(): GLMesh = loadOrGet("standard/meshes/ICOSPHERE.msh")
    fun cylinder(): GLMesh = loadOrGet("standard/meshes/CYLINDER.msh")
    fun octagon(): GLMesh = loadOrGet("standard/meshes/OCTAGON.msh")
    fun pyramid(): GLMesh = loadOrGet("standard/meshes/PYRAMID.msh")

    fun d_Axis(): GLMesh = loadOrGet("standard/meshes/AXIS.msh")
    fun d_Grid(): GLMesh = loadOrGet("standard/meshes/GRID.msh")

    private fun loadOrGet(path: String): GLMesh {
        return loadedMeshes.getOrPut(path) {
            ResourceLoader.loadMesh(path)
        }
    }

}
