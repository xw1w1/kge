package kge.editor.viewport

import kge.api.editor.EditorSelectionGizmoAxis
import kge.editor.Raycast
import kge.editor.ResourceLoader
import kge.editor.core.GameObject
import kge.editor.viewport.gizmos.ScaleGizmoRenderer
import kge.editor.viewport.gizmos.GizmoMode
import kge.editor.viewport.gizmos.RotationGizmoRenderer
import kge.editor.viewport.gizmos.TranslationGizmoRenderer
import kge.ui.toolkit.delegates.SerializeField
import kge.ui.toolkit.delegates.boolField
import kge.ui.toolkit.delegates.enumField
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.abs
import kotlin.math.sqrt

object ViewportGizmoManager {
    private var selectedAxis: EditorSelectionGizmoAxis? = null
    private var highlightedAxis: EditorSelectionGizmoAxis? = null
    var isDragging = false
        private set

    @SerializeField("Gizmo Mode")
    var currentMode by enumField(GizmoMode.Translate, GizmoMode::class)

    @SerializeField("Use local transformations")
    var useLocals by boolField(false)

    private val dragStartHit = Vector3f()
    private val dragPlaneNormal = Vector3f()
    private var dragStartVector = Vector3f()

    private var originalPositions: Map<GameObject, Vector3f> = mapOf()
    private var originalRotations: Map<GameObject, Quaternionf> = mapOf()
    private var originalScales: Map<GameObject, Vector3f> = mapOf()

    private val temp = Vector3f()

    val translationGizmo = TranslationGizmoRenderer()
    val rotationGizmo = RotationGizmoRenderer()
    val scaleGizmo = ScaleGizmoRenderer()

    fun init() {
        val shader = ResourceLoader.loadShader(
                "std/shaders/gizmo.vert",
        "std/shaders/gizmo.frag"
        )
        translationGizmo.init(shader)
        rotationGizmo.init(shader)
        scaleGizmo.init(shader)
    }

    fun setMode(mode: GizmoMode) {
        currentMode = mode
        selectedAxis = null
        highlightedAxis = null
        isDragging = false
    }

    fun getMode(): GizmoMode = currentMode

    fun render(viewProj: Matrix4f, gizmoPos: Vector3f, cameraPos: Vector3f, highlight: Boolean) {
        val activeAxis = if (highlight) selectedAxis ?: highlightedAxis else null

        when (currentMode) {
            GizmoMode.Translate -> translationGizmo.render(viewProj, gizmoPos, activeAxis, cameraPos)
            GizmoMode.Rotate -> rotationGizmo.render(viewProj, gizmoPos, activeAxis, cameraPos)
            GizmoMode.Scale -> scaleGizmo.render(viewProj, gizmoPos, activeAxis, cameraPos)
        }
    }

    fun handleMouse(
        rayOrigin: Vector3f,
        rayDir: Vector3f,
        selectedObjects: Set<GameObject>,
        isDown: Boolean,
        isClicked: Boolean,
        cameraPos: Vector3f
    ) {
        if (selectedObjects.isEmpty()) {
            highlightedAxis = null
            return
        }

        val center = Vector3f()
        for (obj in selectedObjects) {
            center.add(obj.transform.getWorldPosition())
        }
        center.div(selectedObjects.size.toFloat())

        val camDist = center.distance(cameraPos)
        val scale = (camDist * 0.25f).coerceIn(0.2f, 2.2f)

        if (!isDragging) {
            highlightedAxis = when (currentMode) {
                GizmoMode.Translate -> detectHoverTranslation(rayOrigin, rayDir, center, scale)
                GizmoMode.Rotate -> detectHoverRotation(rayOrigin, rayDir, center, scale, cameraPos)
                GizmoMode.Scale -> detectHoverScale(rayOrigin, rayDir, center, scale)
            }
        }

        if (isClicked && highlightedAxis != null && !isDragging) {
            selectedAxis = highlightedAxis
            isDragging = true

            when (currentMode) {
                GizmoMode.Translate -> {
                    dragPlaneNormal.set(computeTranslationPlaneNormal(selectedAxis!!, cameraPos, center))
                    getRayPlaneIntersection(rayOrigin, rayDir, center, dragPlaneNormal, dragStartHit)
                    originalPositions = selectedObjects.associateWith { obj ->
                        obj.transform.getWorldPosition()
                    }
                }
                GizmoMode.Rotate -> {
                    originalRotations = selectedObjects.associateWith { obj ->
                        Quaternionf(obj.transform.getWorldRotation())
                    }
                    getRayPlaneIntersection(rayOrigin, rayDir, center,
                        Vector3f(cameraPos).sub(center).normalize(), dragStartHit)
                    dragStartVector.set(dragStartHit).sub(center).normalize()
                }
                GizmoMode.Scale -> {
                    dragPlaneNormal.set(computeScalePlaneNormal(selectedAxis!!, cameraPos, center))
                    getRayPlaneIntersection(rayOrigin, rayDir, center, dragPlaneNormal, dragStartHit)
                    originalScales = selectedObjects.associateWith { obj ->
                        Vector3f(obj.transform.getWorldScale())
                    }
                    originalPositions = selectedObjects.associateWith { obj ->
                        obj.transform.getWorldPosition()
                    }
                }
            }
            return
        }

        if (!isDown && isDragging) {
            isDragging = false
            selectedAxis = null
            return
        }

        if (isDragging && selectedAxis != null) {
            val hit = Vector3f()
            when (currentMode) {
                GizmoMode.Translate -> handleTranslationDrag(rayOrigin, rayDir, center, selectedObjects, hit)
                GizmoMode.Rotate -> handleRotationDrag(rayOrigin, rayDir, center, selectedObjects, hit)
                GizmoMode.Scale -> handleScaleDrag(rayOrigin, rayDir, center, selectedObjects, hit)
            }
        }
    }

    private fun detectHoverTranslation(
        rayOrigin: Vector3f,
        rayDir: Vector3f,
        origin: Vector3f,
        scale: Float
    ): EditorSelectionGizmoAxis? {
        val shaftLen = 0.8f * scale
        val headLen = 0.18f * scale
        val axisLength = shaftLen + headLen
        val sphereRadius = 0.1f * scale
        val planeSize = 0.35f * axisLength

        val pickRadius = 0.03f * scale
        val spherePickRadius = sphereRadius * 2f

        if (intersectSphere(rayOrigin, rayDir, origin, spherePickRadius))
            return EditorSelectionGizmoAxis.TRANSLATE_CENTER

        if (intersectAxisCylinder(rayOrigin, rayDir, origin, Vector3f(1f, 0f, 0f), axisLength, pickRadius * 2f))
            return EditorSelectionGizmoAxis.TRANSLATE_X
        if (intersectAxisCylinder(rayOrigin, rayDir, origin, Vector3f(0f, 1f, 0f), axisLength, pickRadius * 2f))
            return EditorSelectionGizmoAxis.TRANSLATE_Y
        if (intersectAxisCylinder(rayOrigin, rayDir, origin, Vector3f(0f, 0f, 1f), axisLength, pickRadius * 2f))
            return EditorSelectionGizmoAxis.TRANSLATE_Z

        if (intersectPlaneHandle(rayOrigin, rayDir, origin, Vector3f(1.2f, 0f, 0f), Vector3f(0f, 1.2f, 0f), planeSize))
            return EditorSelectionGizmoAxis.TRANSLATE_PLANE_XY
        if (intersectPlaneHandle(rayOrigin, rayDir, origin, Vector3f(1.2f, 0f, 0f), Vector3f(0f, 0f, 1.2f), planeSize))
            return EditorSelectionGizmoAxis.TRANSLATE_PLANE_XZ
        if (intersectPlaneHandle(rayOrigin, rayDir, origin, Vector3f(0f, 1.2f, 0f), Vector3f(0f, 0f, 1.2f), planeSize))
            return EditorSelectionGizmoAxis.TRANSLATE_PLANE_ZY

        return null
    }

    private fun intersectAxisCylinder(
        rayOrigin: Vector3f,
        rayDir: Vector3f,
        axisOrigin: Vector3f,
        axisDir: Vector3f,
        length: Float,
        radius: Float
    ): Boolean {
        val axisEnd = Vector3f(axisDir).mul(length).add(axisOrigin)
        return intersectCylinder(rayOrigin, rayDir, axisOrigin, axisEnd, radius)
    }

    private fun intersectCylinder(
        rayOrigin: Vector3f,
        rayDir: Vector3f,
        cylinderStart: Vector3f,
        cylinderEnd: Vector3f,
        radius: Float
    ): Boolean {
        val axis = Vector3f(cylinderEnd).sub(cylinderStart)
        val axisLength = axis.length()
        if (axisLength < 1e-6f) return false

        val axisDir = axis.div(axisLength)

        val oc = Vector3f(rayOrigin).sub(cylinderStart)

        val doc = rayDir.dot(axisDir)
        val oac = oc.dot(axisDir)

        val a = 1f - doc * doc
        val b = oc.dot(rayDir) - oac * doc
        val c = oc.lengthSquared() - oac * oac - radius * radius

        val discriminant = b * b - a * c
        if (discriminant < 0f) return false

        val sqrtDisc = sqrt(discriminant.toDouble()).toFloat()
        val t1 = (-b - sqrtDisc) / a
        val t2 = (-b + sqrtDisc) / a

        fun isValidT(t: Float): Boolean {
            if (t < 0f) return false
            val point = Vector3f(rayOrigin).fma(t, rayDir)
            val projection = point.sub(cylinderStart, Vector3f()).dot(axisDir)
            return projection in 0f..axisLength
        }

        return isValidT(t1) || isValidT(t2)
    }

    private fun intersectPlaneHandle(
        rayOrigin: Vector3f,
        rayDir: Vector3f,
        origin: Vector3f,
        dirA: Vector3f,
        dirB: Vector3f,
        size: Float
    ): Boolean {
        val centerOffset = (0.6f * size)

        val p0 = Vector3f(origin).add(Vector3f(dirA).mul(centerOffset)).add(Vector3f(dirB).mul(centerOffset))
        val p1 = Vector3f(p0).add(Vector3f(dirA).mul(size))
        val p2 = Vector3f(p0).add(Vector3f(dirB).mul(size))

        val edge1 = Vector3f(p1).sub(p0)
        val edge2 = Vector3f(p2).sub(p0)
        val planeNormal = edge1.cross(edge2, Vector3f()).normalize()

        val hit = Vector3f()
        if (!getRayPlaneIntersection(rayOrigin, rayDir, p0, planeNormal, hit)) {
            return false
        }

        return pointInTriangle(hit, p0, p1, p2)
    }

    private fun pointInTriangle(p: Vector3f, a: Vector3f, b: Vector3f, c: Vector3f): Boolean {
        val v0 = Vector3f(c).sub(a)
        val v1 = Vector3f(b).sub(a)
        val v2 = Vector3f(p).sub(a)

        val dot00 = v0.dot(v0)
        val dot01 = v0.dot(v1)
        val dot02 = v0.dot(v2)
        val dot11 = v1.dot(v1)
        val dot12 = v1.dot(v2)

        val invDenom = 1f / (dot00 * dot11 - dot01 * dot01).coerceAtLeast(1e-8f)
        val u = (dot11 * dot02 - dot01 * dot12) * invDenom
        val v = (dot00 * dot12 - dot01 * dot02) * invDenom

        return u >= 0f && v >= 0f && u + v <= 1f
    }

    private fun detectHoverRotation(
        rayOrigin: Vector3f,
        rayDir: Vector3f,
        origin: Vector3f,
        scale: Float,
        cameraPos: Vector3f
    ): EditorSelectionGizmoAxis? {
        val ringRadius = 1.0f * scale
        val ringThickness = 0.08f * scale

        if (intersectTorus(rayOrigin, rayDir, origin, Vector3f(1f, 0f, 0f), ringRadius, ringThickness))
            return EditorSelectionGizmoAxis.ROTATE_X
        if (intersectTorus(rayOrigin, rayDir, origin, Vector3f(0f, 1f, 0f), ringRadius, ringThickness))
            return EditorSelectionGizmoAxis.ROTATE_Y
        if (intersectTorus(rayOrigin, rayDir, origin, Vector3f(0f, 0f, 1f), ringRadius, ringThickness))
            return EditorSelectionGizmoAxis.ROTATE_Z

        if (intersectScreenRing(rayOrigin, rayDir, origin, 1.6f * scale, 0.08f * scale, cameraPos))
            return EditorSelectionGizmoAxis.ROTATE_CENTER

        return null
    }

    private fun detectHoverScale(
        rayOrigin: Vector3f,
        rayDir: Vector3f,
        origin: Vector3f,
        scale: Float
    ): EditorSelectionGizmoAxis? {
        val cubeSize = 0.1f * scale
        val lineLength = 0.8f * scale

        if (intersectCube(rayOrigin, rayDir, origin, Vector3f(lineLength, 0f, 0f), cubeSize))
            return EditorSelectionGizmoAxis.SCALE_X
        if (intersectCube(rayOrigin, rayDir, origin, Vector3f(0f, lineLength, 0f), cubeSize))
            return EditorSelectionGizmoAxis.SCALE_Y
        if (intersectCube(rayOrigin, rayDir, origin, Vector3f(0f, 0f, lineLength), cubeSize))
            return EditorSelectionGizmoAxis.SCALE_Z
        if (intersectCube(rayOrigin, rayDir, origin, Vector3f(0f, 0f, 0f), cubeSize * 1.5f))
            return EditorSelectionGizmoAxis.SCALE_CENTER

        return null
    }

    private fun handleTranslationDrag(
        rayOrigin: Vector3f,
        rayDir: Vector3f,
        center: Vector3f,
        selectedObjects: Set<GameObject>,
        hit: Vector3f
    ) {
        if (!getRayPlaneIntersection(rayOrigin, rayDir, center, dragPlaneNormal, hit)) return
        val delta = Vector3f(hit).sub(dragStartHit)

        for (obj in selectedObjects) {
            val t = obj.transform
            val orig = originalPositions[obj] ?: continue

            when (selectedAxis) {
                EditorSelectionGizmoAxis.TRANSLATE_X,
                EditorSelectionGizmoAxis.TRANSLATE_Y,
                EditorSelectionGizmoAxis.TRANSLATE_Z -> {
                    val axis = when (selectedAxis) {
                        EditorSelectionGizmoAxis.TRANSLATE_X -> Vector3f(1f, 0f, 0f)
                        EditorSelectionGizmoAxis.TRANSLATE_Y -> Vector3f(0f, 1f, 0f)
                        EditorSelectionGizmoAxis.TRANSLATE_Z -> Vector3f(0f, 0f, 1f)
                        else -> Vector3f(1f, 0f, 0f)
                    }
                    var newWorld: Vector3f
                    if (useLocals) {
                        val model = t.getWorldMatrix()
                        val worldAxis = model.transformDirection(axis).normalize()
                        val amount = delta.dot(worldAxis)
                        temp.set(worldAxis).mul(amount)
                        newWorld = Vector3f(orig).add(temp)
                    } else {
                        val amount = delta.dot(axis)
                        temp.set(axis).mul(amount)
                        newWorld = Vector3f(orig).add(temp)
                    }
                    t.setWorldPosition(newWorld)
                }
                EditorSelectionGizmoAxis.TRANSLATE_CENTER -> {
                    val newWorld = Vector3f(orig).add(delta)
                    t.setWorldPosition(newWorld)
                }
                EditorSelectionGizmoAxis.TRANSLATE_PLANE_XY,
                EditorSelectionGizmoAxis.TRANSLATE_PLANE_XZ,
                EditorSelectionGizmoAxis.TRANSLATE_PLANE_ZY -> {
                    val newWorld = Vector3f(orig).add(delta)
                    t.setWorldPosition(newWorld)
                }
                else -> {}
            }
        }
    }

    private fun handleRotationDrag(
        rayOrigin: Vector3f,
        rayDir: Vector3f,
        center: Vector3f,
        selectedObjects: Set<GameObject>,
        hit: Vector3f
    ) {
        val cameraDir = Vector3f(rayOrigin).sub(center).normalize()
        if (!getRayPlaneIntersection(rayOrigin, rayDir, center, cameraDir, hit)) return

        val currentVector = Vector3f(hit).sub(center).normalize()
        val rotationAxis = when (selectedAxis) {
            EditorSelectionGizmoAxis.ROTATE_X -> Vector3f(-1f, 0f, 0f)
            EditorSelectionGizmoAxis.ROTATE_Y -> Vector3f(0f, 1f, 0f)
            EditorSelectionGizmoAxis.ROTATE_Z -> Vector3f(0f, 0f, -1f)
            EditorSelectionGizmoAxis.ROTATE_CENTER -> cameraDir
            else -> Vector3f(0f, 1f, 0f)
        }

        var angle = dragStartVector.angle(currentVector)

        val cross = dragStartVector.cross(currentVector, Vector3f())
        if (cross.dot(rotationAxis) < 0) {
            angle = -angle
        }

        for (obj in selectedObjects) {
            val t = obj.transform
            val origRotation = originalRotations[obj] ?: continue

            when (selectedAxis) {
                EditorSelectionGizmoAxis.ROTATE_X, EditorSelectionGizmoAxis.ROTATE_Y,
                EditorSelectionGizmoAxis.ROTATE_Z, EditorSelectionGizmoAxis.ROTATE_CENTER -> {
                    val rotation = Quaternionf().rotateAxis(angle, rotationAxis)
                    var newRotation: Quaternionf
                    if (useLocals) {
                        newRotation = Quaternionf(rotation).mul(origRotation)
                    } else {
                        newRotation = Quaternionf(origRotation).mul(rotation)
                    }
                    t.setWorldRotation(newRotation)
                }
                else -> {}
            }
        }
    }

    private fun handleScaleDrag(
        rayOrigin: Vector3f,
        rayDir: Vector3f,
        center: Vector3f,
        selectedObjects: Set<GameObject>,
        hit: Vector3f
    ) {
        if (!getRayPlaneIntersection(rayOrigin, rayDir, center, dragPlaneNormal, hit)) return

        val delta = hit.sub(dragStartHit, Vector3f())

        for (obj in selectedObjects) {
            val t = obj.transform
            val origScale = originalScales[obj] ?: continue

            when (selectedAxis) {
                EditorSelectionGizmoAxis.SCALE_X -> {
                    val axis = Vector3f(1f, 0f, 0f)
                    val model = t.getWorldMatrix()
                    val worldAxis = model.transformDirection(axis).normalize()
                    val amount = delta.dot(worldAxis)

                    val scaleFactor = 1f + amount * 2f
                    val newScaleX = origScale.x * scaleFactor

                    if (newScaleX > 0.01f) {
                        t.setWorldScale(newScaleX, origScale.y, origScale.z)
                    }
                }
                EditorSelectionGizmoAxis.SCALE_Y -> {
                    val axis = Vector3f(0f, 1f, 0f)
                    val model = t.getWorldMatrix()
                    val worldAxis = model.transformDirection(axis).normalize()
                    val amount = delta.dot(worldAxis)

                    val scaleFactor = 1f + amount * 2f
                    val newScaleY = origScale.y * scaleFactor

                    if (newScaleY > 0.01f) {
                        t.setWorldScale(origScale.x, newScaleY, origScale.z)
                    }
                }
                EditorSelectionGizmoAxis.SCALE_Z -> {
                    val axis = Vector3f(0f, 0f, 1f)
                    val model = t.getWorldMatrix()
                    val worldAxis = model.transformDirection(axis).normalize()
                    val amount = delta.dot(worldAxis)

                    val scaleFactor = 1f + amount * 2f
                    val newScaleZ = origScale.z * scaleFactor

                    if (newScaleZ > 0.01f) {
                        t.setWorldScale(origScale.x, origScale.y, newScaleZ)
                    }
                }
                EditorSelectionGizmoAxis.SCALE_CENTER -> {
                    val scaleAmount = delta.length() * 2f
                    val scaleDirection = if (delta.dot(dragPlaneNormal) > 0) 1f else -1f
                    val scaleFactor = 1f + scaleAmount * scaleDirection

                    val newScale = Vector3f(origScale).mul(scaleFactor)

                    if (newScale.x > 0.001f && newScale.y > 0.001f && newScale.z > 0.001f) {
                        t.setWorldScale(newScale)
                    }
                }
                else -> {}
            }
        }
    }

    private fun intersectTorus(
        rayOrigin: Vector3f,
        rayDir: Vector3f,
        center: Vector3f,
        axis: Vector3f,
        radius: Float,
        thickness: Float
    ): Boolean {
        val hit = Vector3f()
        if (!getRayPlaneIntersection(rayOrigin, rayDir, center, axis, hit)) {
            return false
        }

        val dist = hit.distance(center)

        return abs(dist - radius) <= thickness
    }

    private fun intersectScreenRing(
        rayOrigin: Vector3f,
        rayDir: Vector3f,
        center: Vector3f,
        radius: Float,
        thickness: Float,
        cameraPos: Vector3f
    ): Boolean {
        val cameraDir = Vector3f(cameraPos).sub(center).normalize()
        val hit = Vector3f()

        if (!getRayPlaneIntersection(rayOrigin, rayDir, center, cameraDir, hit)) {
            return false
        }

        val dist = hit.distance(center)
        return abs(dist - radius) <= thickness
    }

    private fun intersectCube(
        rayOrigin: Vector3f,
        rayDir: Vector3f,
        center: Vector3f,
        cubeCenter: Vector3f,
        size: Float
    ): Boolean {
        val worldCubeCenter = Vector3f(center).add(cubeCenter)
        val min = Vector3f(worldCubeCenter).sub(Vector3f(size / 2f))
        val max = Vector3f(worldCubeCenter).add(Vector3f(size / 2f))
        return Raycast.intersectRayAABB(rayOrigin, rayDir, min, max) != null
    }

    private fun computeTranslationPlaneNormal(axis: EditorSelectionGizmoAxis, cameraPos: Vector3f, center: Vector3f): Vector3f {
        return when (axis) {
            EditorSelectionGizmoAxis.TRANSLATE_X -> Vector3f(0f, 1f, 0f)
            EditorSelectionGizmoAxis.TRANSLATE_Y -> Vector3f(0f, 0f, 1f)
            EditorSelectionGizmoAxis.TRANSLATE_Z -> Vector3f(1f, 0f, 0f)
            EditorSelectionGizmoAxis.TRANSLATE_CENTER -> Vector3f(cameraPos).sub(center).normalize()
            EditorSelectionGizmoAxis.TRANSLATE_PLANE_XY -> Vector3f(0f, 0f, 1f)
            EditorSelectionGizmoAxis.TRANSLATE_PLANE_XZ -> Vector3f(0f, 1f, 0f)
            EditorSelectionGizmoAxis.TRANSLATE_PLANE_ZY -> Vector3f(1f, 0f, 0f)
            else -> Vector3f(0f, 1f, 0f)
        }
    }

    private fun computeScalePlaneNormal(axis: EditorSelectionGizmoAxis, cameraPos: Vector3f, center: Vector3f): Vector3f {
        return when (axis) {
            EditorSelectionGizmoAxis.SCALE_X -> Vector3f(0f, 1f, 0f)
            EditorSelectionGizmoAxis.SCALE_Y -> Vector3f(0f, 0f, 1f)
            EditorSelectionGizmoAxis.SCALE_Z -> Vector3f(1f, 0f, 0f)
            EditorSelectionGizmoAxis.SCALE_CENTER -> Vector3f(cameraPos).sub(center).normalize()
            else -> Vector3f(0f, 1f, 0f)
        }
    }

    private fun getRayPlaneIntersection(
        rayOrigin: Vector3f,
        rayDir: Vector3f,
        planePoint: Vector3f,
        planeNormal: Vector3f,
        out: Vector3f
    ): Boolean {
        val denom = rayDir.dot(planeNormal)
        if (abs(denom) < 1e-6f) return false
        val t = Vector3f(planePoint).sub(rayOrigin).dot(planeNormal) / denom
        if (t < 0f) return false
        out.set(rayOrigin).fma(t, rayDir)
        return true
    }

    private fun intersectSphere(rayOrigin: Vector3f, rayDir: Vector3f, center: Vector3f, radius: Float): Boolean {
        val l = Vector3f(center).sub(rayOrigin)
        val tca = l.dot(rayDir)
        if (tca < 0f) return false
        val d2 = l.lengthSquared() - tca * tca
        return d2 <= radius * radius
    }
}