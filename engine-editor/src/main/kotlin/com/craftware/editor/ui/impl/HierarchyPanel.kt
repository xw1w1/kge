package com.craftware.editor.ui.impl

import com.craftware.editor.Selection
import com.craftware.editor.ui.UIPanel
import com.craftware.editor.Node
import com.craftware.editor.NodeParent
import com.craftware.editor.Primitives
import com.craftware.editor.Scene
import com.craftware.editor.component.MeshRenderer
import com.craftware.engine.imgui.menu
import com.craftware.engine.imgui.menuItem
import com.craftware.engine.imgui.popupContext
import imgui.ImGui
import imgui.flag.ImGuiInputTextFlags
import imgui.flag.ImGuiKey
import imgui.flag.ImGuiTreeNodeFlags
import imgui.type.ImString

class HierarchyPanel(
    private val scene: Scene,
    private val selection: Selection
) : UIPanel("Hierarchy", "Hierarchy") {
    private var dragSource: Node? = null
    private var renamingNode: Node? = null
    private var renameBuffer = ImString(128)

    fun render() = render {
        if (ImGui.beginPopupContextWindow("HierarchyContext", 1)) {
            if (ImGui.beginMenu("Create")) {
                if (ImGui.menuItem("GameObject")) scene.createEmpty("GameObject")
                if (ImGui.menuItem("Cube")) scene.createCube("Cube")
                if (ImGui.menuItem("Plane")) scene.create("Plane", scene) {
                    val renderer = MeshRenderer(Primitives.plane())
                    it.addComponent(renderer)
                }
                if (ImGui.menuItem("Sphere")) scene.create("Sphere", scene) {
                    val renderer = MeshRenderer(Primitives.sphere())
                    it.addComponent(renderer)
                }
                if (ImGui.menuItem("Cylinder")) scene.create("Cylinder", scene) {
                    val renderer = MeshRenderer(Primitives.cylinder())
                    it.addComponent(renderer)
                }
                if (ImGui.menuItem("Pyramid")) scene.create("Pyramid", scene) {
                    val renderer = MeshRenderer(Primitives.pyramid())
                    it.addComponent(renderer)
                }
                if (ImGui.menuItem("Cone")) scene.create("Cone", scene) {
                    val renderer = MeshRenderer(Primitives.cone())
                    it.addComponent(renderer)
                }
                if (ImGui.menuItem("Torus")) scene.create("Torus", scene) {
                    val renderer = MeshRenderer(Primitives.torus())
                    it.addComponent(renderer)
                }
                if (ImGui.menuItem("Octagon")) scene.create("Octagon", scene) {
                    val renderer = MeshRenderer(Primitives.octagon())
                    it.addComponent(renderer)
                }
                if (ImGui.menuItem("Polygon")) scene.create("Polygon", scene) {
                    val renderer = MeshRenderer(Primitives.polygon())
                    it.addComponent(renderer)
                }
                if (ImGui.menuItem("DebugGrid")) scene.create("DebugGrid", scene) {
                    val renderer = MeshRenderer(Primitives.d_Grid())
                    it.addComponent(renderer)
                }
                if (ImGui.menuItem("DebugAxis")) scene.create("DebugAxis", scene) {
                    val renderer = MeshRenderer(Primitives.d_Axis())
                    it.addComponent(renderer)
                }

                ImGui.endMenu()
            }
            ImGui.endPopup()
        }

        if (ImGui.beginDragDropTarget()) {
            val payload = ImGui.acceptDragDropPayload<Any>("NODE")
            if (payload != null) {
                val src = dragSource
                if (src != null) {
                    src.parent?.removeChild(src)
                    scene.addChild(src)
                }
                dragSource = null
            }
            ImGui.endDragDropTarget()
        }

        for (child in scene.getChildren()) {
            drawNodeRecursive(child)
        }

        val availY = ImGui.getContentRegionAvailY()
        if (availY > 0f) {
            ImGui.invisibleButton("##dropzone", ImGui.getContentRegionAvailX(), availY)
            if (ImGui.beginDragDropTarget()) {
                val payload = ImGui.acceptDragDropPayload<Any>("NODE")
                if (payload != null) {
                    val src = dragSource
                    if (src != null) {
                        src.parent?.removeChild(src)
                        scene.addChild(src)
                    }
                    dragSource = null
                }
                ImGui.endDragDropTarget()
            }
        }
    }

    private fun drawNodeRecursive(node: Node) {
        val nodeId = System.identityHashCode(node).toString()
        ImGui.pushID(nodeId)

        val isSelected = selection.selected === node
        val hasChildren = node.getChildren().isNotEmpty()

        val flags = ImGuiTreeNodeFlags.OpenOnArrow or ImGuiTreeNodeFlags.SpanAvailWidth or
                (if (!hasChildren) ImGuiTreeNodeFlags.Leaf else 0) or
                (if (isSelected) ImGuiTreeNodeFlags.Selected else 0)

        val opened = ImGui.treeNodeEx(node.name + "##" + nodeId, flags)
        val itemClicked = ImGui.isItemClicked(0)
        val itemHovered = ImGui.isItemHovered()

        if (itemClicked) selection.select(node)
        if (itemHovered && ImGui.isMouseDoubleClicked(0)) {
            renamingNode = node
            renameBuffer.set(node.name)
        }

        if (ImGui.beginDragDropSource()) {
            dragSource = node
            ImGui.setDragDropPayload("NODE", node)
            ImGui.text("Move: ${node.name}")
            ImGui.endDragDropSource()
        }

        if (ImGui.beginDragDropTarget()) {
            val payload = ImGui.acceptDragDropPayload<Any>("NODE")
            if (payload != null) {
                val src = dragSource
                if (src != null && src !== node && !isDescendantOf(src, node)) {
                    moveNode(src, node)
                }
                dragSource = null
            }
            ImGui.endDragDropTarget()
        }

        popupContext("NodeContext##$nodeId") {
            menu("Create") {
                menuItem("GameObject") {
                    scene.createEmpty("GameObject")
                }
                menuItem("Cube") {
                    scene.createCube("Cube")
                }
            }
            menuItem("Rename") {
                renamingNode = node
                renameBuffer.set(node.name)
            }
            menuItem("Delete") {
                node.parent?.removeChild(node)
                if (selection.selected === node) selection.clear()
            }
        }

        ImGui.sameLine()
        if (renamingNode === node) {
            val labelStart = ImGui.getCursorScreenPos()

            val fullLabel = "${node.name} (${node.displayType})"
            val textSize = ImGui.calcTextSize(fullLabel)
            labelStart.y -= textSize.y / 4

            ImGui.setCursorScreenPos(labelStart.x, labelStart.y)

            ImGui.setNextItemWidth(textSize.x)
            ImGui.setKeyboardFocusHere()

            val enterPressed = ImGui.inputText(
                "##rename_${System.identityHashCode(node)}",
                renameBuffer,
                ImGuiInputTextFlags.EnterReturnsTrue or ImGuiInputTextFlags.AutoSelectAll
            )

            if (enterPressed) {
                node.name = renameBuffer.get()
                renamingNode = null
            }

            if (ImGui.isKeyPressed(ImGui.getKeyIndex(ImGuiKey.Escape))) {
                renamingNode = null
            }
        } else {
            ImGui.sameLine()
            ImGui.textDisabled("(${node.displayType})")
            if (!node.isActive) {
                ImGui.sameLine()
                ImGui.textDisabled("(Disabled)")
            }
        }

        if (opened) {
            for (child in node.getChildren()) drawNodeRecursive(child)
            ImGui.treePop()
        }

        ImGui.popID()
    }

    private fun moveNode(src: Node, dest: Node) {
        val oldParent = src.parent
        if (oldParent === dest) return
        oldParent?.removeChild(src)
        (dest as? NodeParent)?.addChild(src)
    }

    private fun isDescendantOf(dest: Node, potentialChild: Node): Boolean {
        if (dest === potentialChild) return true
        for (c in dest.getChildren()) {
            if (isDescendantOf(c, potentialChild)) return true
        }
        return false
    }
}
