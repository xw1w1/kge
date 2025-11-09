package kge.editor

import kge.api.std.INodeParent
import kge.api.std.ITransform
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import org.joml.Vector4f

class EditorTransformImpl : ITransform {
    override var position: Vector3f = Vector3f(0f, 0f, 0f)
    override var rotation: Quaternionf = Quaternionf()
    override var scale: Vector3f = Vector3f(1f, 1f, 1f)

    override fun getLocalMatrix(): Matrix4f {
        return Matrix4f()
            .translate(position)
            .rotate(rotation)
            .scale(scale)
    }

    fun getWorldMatrix(parent: INodeParent?): Matrix4f {
        return if (parent is GameObject && parent != this)
            Matrix4f(parent.transform.getWorldMatrix(parent.parent)).mul(getLocalMatrix())
        else
            getLocalMatrix()
    }


    fun getWorldPosition(parent: INodeParent?): Vector3f {
        val result = Vector3f()
        getWorldMatrix(parent).getTranslation(result)
        return result
    }

    fun setWorldPosition(parent: INodeParent?, worldPos: Vector3f) {
        if (parent != null && parent is GameObject) {
            val parentMatrix = Matrix4f(parent.transform.getWorldMatrix(parent.parent))
            parentMatrix.invert()
            val local = Vector4f(worldPos, 1f).mul(parentMatrix)
            position.set(local.x, local.y, local.z)
        } else {
            position.set(worldPos)
        }
    }
}