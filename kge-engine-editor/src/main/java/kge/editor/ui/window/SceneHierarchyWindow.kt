package kge.editor.ui.window

import imgui.ImGui
import imgui.flag.ImGuiInputTextFlags
import imgui.flag.ImGuiKey
import imgui.flag.ImGuiTreeNodeFlags
import imgui.type.ImString
import kge.api.editor.imgui.IRenderCallback
import kge.api.std.INode
import kge.api.std.INodeParent
import kge.api.std.IRenderable
import kge.editor.EditorApplication
import kge.editor.GameObject
import kge.editor.ui.EditorUIPanel

class SceneHierarchyWindow : EditorUIPanel("Hierarchy"), IRenderable {
    private var dragSource: INode? = null

    private var renamingNode: INode? = null
    private var renameBuffer = ImString(128)

    override fun render(delta: Float) {
        this.beginUI()
        content = {
            if (ImGui.beginPopupContextWindow("HierarchyContext", 1)) {
                ImGui.endPopup()
            }

            val scene = EditorApplication.getInstance().getProjectManager().getCurrentScene()
            if (scene != null) {
                if (ImGui.beginDragDropTarget()) {
                    val payload = ImGui.acceptDragDropPayload<Any>("NODE")
                    if (payload != null) {
                        val src = dragSource
                        if (src != null) {
                            src.parent?.removeChild(src)
                            scene.root.addChild(src)
                        }
                        dragSource = null
                    }
                    ImGui.endDragDropTarget()
                }

                for (child in scene.root.children) {
                    drawNodeRecursive(child as GameObject)
                }

                val availY = ImGui.getContentRegionAvailY()
                if (availY > 0f) {
                    ImGui.invisibleButton("##dropzone", ImGui.getContentRegionAvailX(), availY)

                    if (ImGui.isItemClicked(0)) {
                        scene.getSelection().clearSelection()
                    }

                    if (ImGui.beginDragDropTarget()) {
                        val payload = ImGui.acceptDragDropPayload<Any>("NODE")
                        if (payload != null) {
                            val src = dragSource
                            if (src != null) {
                                src.parent?.removeChild(src)
                                scene.root.addChild(src)
                            }
                            dragSource = null
                        }
                        ImGui.endDragDropTarget()
                    }
                }
            }
        }
        this.endUI()
    }

    private fun drawNodeRecursive(node: GameObject) {
        val nodeId = System.identityHashCode(node).toString()
        ImGui.pushID(nodeId)

        val selection = EditorApplication.getInstance().getEditorSelection()
        val isSelected = selection.getSelectedObjects().contains(node)
        val hasChildren = node.children.isNotEmpty()

        val flags = ImGuiTreeNodeFlags.OpenOnArrow or ImGuiTreeNodeFlags.SpanAvailWidth or
                (if (!hasChildren) ImGuiTreeNodeFlags.Leaf else 0) or
                (if (isSelected) ImGuiTreeNodeFlags.Selected else 0)

        val opened = ImGui.treeNodeEx(node.name + "##" + nodeId, flags)
        val itemClicked = ImGui.isItemClicked(0)
        val itemHovered = ImGui.isItemHovered()

        if (itemClicked) {
            if (ImGui.isKeyDown(ImGuiKey.LeftShift)) {
                selection.addSelection(node)
            } else {
                selection.select(node)
            }
        }

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
                if (src != null && src !== node && !isDescendantOf(src as INodeParent, node)) {
                    moveNode(src, node)
                }
                dragSource = null
            }
            ImGui.endDragDropTarget()
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

            if (ImGui.isKeyPressed(ImGuiKey.Escape)) {
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
            for (child in node.children) drawNodeRecursive(child as GameObject)
            ImGui.treePop()
        }

        ImGui.popID()
    }

    private fun moveNode(src: INode, dest: INode) {
        val oldParent = src.parent
        if (oldParent === dest) return
        oldParent?.removeChild(src)
        (dest as? INodeParent)?.addChild(src)
    }
    private fun isDescendantOf(dest: INodeParent, potentialChild: INodeParent): Boolean {
        if (dest === potentialChild) return true
        for (c in dest.children) {
            if (isDescendantOf(c as INodeParent, potentialChild)) return true
        }
        return false
    }

    override fun pushRenderCallback(cb: IRenderCallback) { }
}