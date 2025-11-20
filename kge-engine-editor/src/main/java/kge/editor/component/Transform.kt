package kge.editor.component

import kge.api.std.ITransform
import kge.editor.core.GameObject
import kge.ui.toolkit.delegates.SerializeField
import kge.ui.toolkit.delegates.vec3Field
import org.joml.*
import org.joml.Math.toDegrees
import org.joml.Math.toRadians

class Transform(node: GameObject) :
    Component(node),
    ITransform
{
    override var localPosition: Vector3f = Vector3f()
    override var localRotation: Quaternionf = Quaternionf()
    override var localScale: Vector3f = Vector3f(1f, 1f, 1f)

    private val parentTransform: Transform?
        get() = gameObject.parent?.transform

    override var position: Vector3f
        get() = getWorldPosition()
        set(v) {
            setWorldPosition(v)
            syncEditorFromLocal()
        }

    override var rotation: Quaternionf
        get() = getWorldRotation()
        set(q) {
            setWorldRotation(q)
            syncEditorFromLocal()
        }

    override var scale: Vector3f
        get() = getWorldScale()
        set(v) {
            setWorldScale(v)
            syncEditorFromLocal()
        }

    override val forward: Vector3f
        get() {
            val result = Vector3f(0f, 0f, -1f)
            getWorldRotation().transform(result)
            return result.normalize()
        }

    override val right: Vector3f
        get() {
            val result = Vector3f(1f, 0f, 0f)
            getWorldRotation().transform(result)
            return result.normalize()
        }

    override val up: Vector3f
        get() {
            val result = Vector3f(0f, 1f, 0f)
            getWorldRotation().transform(result)
            return result.normalize()
        }

    override val typeName: String = "Transform"

    @SerializeField("Position")
    var editorPosition by vec3Field(Vector3f(localPosition))

    @SerializeField("Rotation")
    var editorRotationEuler by vec3Field(Vector3f())

    @SerializeField("Scale")
    var editorScale by vec3Field(Vector3f(localScale))

    private var suppress = false

    override fun onUpdate() {
        if (suppress) return
        suppress = true

        localPosition.set(editorPosition)
        localScale.set(editorScale)
        localRotation.identity().rotateXYZ(
            toRadians(editorRotationEuler.x),
            toRadians(editorRotationEuler.y),
            toRadians(editorRotationEuler.z)
        )

        editorPosition.set(localPosition)
        editorScale.set(localScale)
        val e = Vector3f()
        localRotation.getEulerAnglesXYZ(e)
        editorRotationEuler.set(
            toDegrees(e.x.toDouble()).toFloat(),
            toDegrees(e.y.toDouble()).toFloat(),
            toDegrees(e.z.toDouble()).toFloat()
        )

        suppress = false
    }

    override fun getLocalMatrix(): Matrix4f =
        Matrix4f().translate(localPosition).rotate(localRotation).scale(localScale)

    fun getWorldMatrix(): Matrix4f =
        parentTransform?.let { Matrix4f(it.getWorldMatrix()).mul(getLocalMatrix()) } ?: getLocalMatrix()

    fun getWorldPosition(): Vector3f {
        val result = Vector3f()
        getWorldMatrix().getTranslation(result)
        return result
    }

    fun setWorldPosition(worldPos: Vector3f) {
        val parent = parentTransform
        if (parent == null) localPosition.set(worldPos)
        else {
            val inv = Matrix4f(parent.getWorldMatrix()).invert()
            val local = Vector4f(worldPos, 1f).mul(inv)
            localPosition.set(local.x, local.y, local.z)
        }
        syncEditorFromLocal()
    }

    fun getWorldRotation(): Quaternionf =
        parentTransform?.let { Quaternionf(it.rotation).mul(localRotation) } ?: Quaternionf(localRotation)

    fun setWorldRotation(worldRot: Quaternionf) {
        val parent = parentTransform
        if (parent == null) localRotation.set(worldRot)
        else {
            val invParentRot = Quaternionf(parent.rotation).invert()
            localRotation.set(invParentRot.mul(worldRot))
        }
        syncEditorFromLocal()
    }

    fun getWorldScale(): Vector3f =
        parentTransform?.let { Vector3f(it.scale).mul(localScale) } ?: Vector3f(localScale)

    fun setWorldScale(worldScale: Vector3f) {
        val parent = parentTransform
        if (parent == null) localScale.set(worldScale)
        else {
            val p = parent.scale
            localScale.set(
                worldScale.x / p.x,
                worldScale.y / p.y,
                worldScale.z / p.z
            )
        }
        syncEditorFromLocal()
    }

    fun setWorldScale(x: Float, y: Float, z: Float) {
        setWorldScale(Vector3f(x, y, z))
    }

    override fun lookAt(target: Vector3f, up: Vector3f) {
        val worldPos = getWorldPosition()
        val direction = Vector3f(target).sub(worldPos)
        if (direction.lengthSquared() < 1e-6f) {
            return
        }

        direction.normalize()
        val rotationMatrix = Matrix4f()
            .lookAt(worldPos, target, up)
            .invert()
        val newWorldRotation = Quaternionf().setFromNormalized(rotationMatrix)

        setWorldRotation(newWorldRotation)
    }

    fun lookAt(target: Vector3f) {
        lookAt(target.x, target.y, target.z)
    }

    fun lookAt(
        targetX: Float, targetY: Float, targetZ: Float,
        worldUp: Vector3f = Vector3f(0f, 1f, 0f)
    ) {
        lookAt(Vector3f(targetX, targetY, targetZ), worldUp)
    }

    private fun syncEditorFromLocal() {
        if (suppress) return
        suppress = true

        editorPosition.set(localPosition)
        editorScale.set(localScale)
        val e = Vector3f()
        localRotation.getEulerAnglesXYZ(e)
        editorRotationEuler.set(
            toDegrees(e.x.toDouble()).toFloat(),
            toDegrees(e.y.toDouble()).toFloat(),
            toDegrees(e.z.toDouble()).toFloat()
        )

        suppress = false
    }
}
