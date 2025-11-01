package com.craftware.editor

import com.craftware.editor.component.MeshRenderer
import com.craftware.editor.component.Transform

class Scene(val name: String = "Untitled") : NodeParent() {
    fun createEmpty(name: String = "GameObject"): GameObject {
        val obj = GameObject(name)
        addChild(obj)
        return obj
    }

    fun createCube(name: String = "Cube"): GameObject {
        val cube = GameObject(name)
        cube.add<MeshRenderer>()
        addChild(cube)
        return cube
    }

    fun create(name: String = "GameObject", parent: NodeParent, action: ((GameObject) -> Unit)? = null): GameObject {
        val obj = GameObject(name)
        action?.invoke(obj)
        parent.addChild(obj)
        return obj
    }

    fun getAllObjects(): List<GameObject> {
        val list = mutableListOf<GameObject>()
        collectChildren(this, list)
        return list
    }

    private fun collectChildren(parent: NodeParent, result: MutableList<GameObject>) {
        for (child in parent.getChildren()) {
            if (child is GameObject) result += child
            collectChildren(child, result)
        }
    }
}
