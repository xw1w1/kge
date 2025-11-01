package com.craftware.editor.ui

import imgui.ImGui
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max

// this snap to edge shit is not really working
object UIPanelManager {
    private val panels = mutableListOf<UIPanel>()
    private const val SNAP_DIST = 12f
    private const val MIN_PANEL_SIZE = 100f
    private fun getViewportBounds(): Pair<Float, Float> {
        val io = ImGui.getIO()
        return io.displaySize.x to io.displaySize.y
    }

    fun register(p: UIPanel) {
        if (panels.none { it.id == p.id }) panels += p
    }

    internal fun findSnapForMove(moving: UIPanel): SnapResult? {
        val (screenW, screenH) = getViewportBounds()
        val ax1 = moving.pos.x
        val ay1 = moving.pos.y
        val ax2 = ax1 + moving.size.x
        val ay2 = ay1 + moving.size.y

        var best: SnapResult? = null

        for (other in panels) {
            if (other === moving) continue

            val bx1 = other.pos.x
            val by1 = other.pos.y
            val bx2 = bx1 + other.size.x
            val by2 = by1 + other.size.y

            val overlapY = ay2 > by1 && ay1 < by2
            val overlapX = ax2 > bx1 && ax1 < bx2

            fun trySnap(dx: Float, dy: Float, kind: SnapKind) {
                val dist = hypot(dx.toDouble(), dy.toDouble()).toFloat()
                if (dist <= SNAP_DIST) {
                    if (best == null || dist < best!!.dist)
                        best = SnapResult(other, dx, dy, dist, kind)
                }
            }

            // вертикальные прилипания
            if (overlapY) {
                trySnap(bx1 - ax2, 0f, SnapKind.LEFT)   // справа от other
                trySnap(bx2 - ax1, 0f, SnapKind.RIGHT)  // слева от other
            }
            // горизонтальные прилипания
            if (overlapX) {
                trySnap(0f, by1 - ay2, SnapKind.TOP)
                trySnap(0f, by2 - ay1, SnapKind.BOTTOM)
            }
        }

        val borderSnapX = when {
            abs(ax1) < SNAP_DIST -> -ax1
            abs(ax2 - screenW) < SNAP_DIST -> screenW - ax2
            else -> 0f
        }
        val borderSnapY = when {
            abs(ay1) < SNAP_DIST -> -ay1
            abs(ay2 - screenH) < SNAP_DIST -> screenH - ay2
            else -> 0f
        }

        if (borderSnapX != 0f || borderSnapY != 0f) {
            val dist = hypot(borderSnapX.toDouble(), borderSnapY.toDouble()).toFloat()
            if (best == null || dist < best.dist)
                best = SnapResult(null, borderSnapX, borderSnapY, dist, SnapKind.BORDER)
        }

        return best
    }

    internal fun findSnapForResize(resizing: UIPanel, resizeEdge: ResizeEdge): SnapResult? {
        val (screenW, screenH) = getViewportBounds()

        val ax1 = resizing.pos.x
        val ay1 = resizing.pos.y
        val ax2 = ax1 + resizing.size.x
        val ay2 = ay1 + resizing.size.y

        var best: SnapResult? = null

        fun consider(delta: Float, overlap: Boolean, kind: SnapKind) {
            if (!overlap) return
            val d = abs(delta)
            if (d <= SNAP_DIST && (best == null || d < best!!.dist)) {
                best = SnapResult(null, delta, 0f, d, kind)
            }
        }

        for (other in panels) {
            if (other === resizing) continue
            val bx1 = other.pos.x
            val by1 = other.pos.y
            val bx2 = bx1 + other.size.x
            val by2 = by1 + other.size.y

            val overlapY = ay2 > by1 && ay1 < by2
            val overlapX = ax2 > bx1 && ax1 < bx2

            when (resizeEdge) {
                ResizeEdge.RIGHT -> {
                    consider(bx1 - ax2, overlapY, SnapKind.LEFT)
                    consider(bx2 - ax2, overlapY, SnapKind.RIGHT)
                }
                ResizeEdge.LEFT -> {
                    consider(bx2 - ax1, overlapY, SnapKind.RIGHT)
                    consider(bx1 - ax1, overlapY, SnapKind.LEFT)
                }
                ResizeEdge.BOTTOM -> {
                    consider(by1 - ay2, overlapX, SnapKind.TOP)
                    consider(by2 - ay2, overlapX, SnapKind.BOTTOM)
                }
                ResizeEdge.TOP -> {
                    consider(by2 - ay1, overlapX, SnapKind.BOTTOM)
                    consider(by1 - ay1, overlapX, SnapKind.TOP)
                }
            }
        }

        when (resizeEdge) {
            ResizeEdge.LEFT -> if (abs(ax1) < SNAP_DIST) best = SnapResult(null, -ax1, 0f, abs(ax1), SnapKind.BORDER)
            ResizeEdge.RIGHT -> if (abs(ax2 - screenW) < SNAP_DIST) best = SnapResult(null, screenW - ax2, 0f, abs(screenW - ax2), SnapKind.BORDER)
            ResizeEdge.TOP -> if (abs(ay1) < SNAP_DIST) best = SnapResult(null, 0f, -ay1, abs(ay1), SnapKind.BORDER)
            ResizeEdge.BOTTOM -> if (abs(ay2 - screenH) < SNAP_DIST) best = SnapResult(null, 0f, screenH - ay2, abs(screenH - ay2), SnapKind.BORDER)
        }

        return best
    }

    fun clampToScreen(panel: UIPanel) {
        val (screenW, screenH) = getViewportBounds()

        if (panel.pos.x < 0f) panel.pos.x = 0f
        if (panel.pos.y < 0f) panel.pos.y = 0f

        if (panel.pos.x + panel.size.x > screenW)
            panel.pos.x = screenW - panel.size.x
        if (panel.pos.y + panel.size.y > screenH)
            panel.pos.y = screenH - panel.size.y
    }

    internal fun applyMutualResize(snap: SnapResult, resizeEdge: ResizeEdge) {
        val other = snap.other ?: return
        val delta = when (resizeEdge) {
            ResizeEdge.RIGHT, ResizeEdge.LEFT -> snap.dx
            ResizeEdge.TOP, ResizeEdge.BOTTOM -> snap.dy
        }

        when (resizeEdge) {
            ResizeEdge.RIGHT -> {
                other.size.x = max(MIN_PANEL_SIZE, other.size.x - delta)
            }
            ResizeEdge.LEFT -> {
                other.pos.x += delta
                other.size.x = max(MIN_PANEL_SIZE, other.size.x - delta)
            }
            ResizeEdge.BOTTOM -> {
                other.size.y = max(MIN_PANEL_SIZE, other.size.y - delta)
            }
            ResizeEdge.TOP -> {
                other.pos.y += delta
                other.size.y = max(MIN_PANEL_SIZE, other.size.y - delta)
            }
        }

        ImGui.setWindowPos(other.id, other.pos.x, other.pos.y)
        ImGui.setWindowSize(other.id, other.size.x, other.size.y)
    }
}
