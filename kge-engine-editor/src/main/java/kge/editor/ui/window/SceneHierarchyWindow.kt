package kge.editor.ui.window

import imgui.ImGui
import imgui.flag.ImGuiInputTextFlags
import imgui.flag.ImGuiKey
import imgui.flag.ImGuiTreeNodeFlags
import imgui.type.ImString
import kge.api.editor.imgui.IRenderCallback
import kge.api.std.INode
import kge.api.std.IRenderable
import kge.editor.*
import kge.editor.ui.EditorText
import kge.editor.ui.EditorUIPanel
import kge.editor.ui.dragndrop.EditorDragManager

class SceneHierarchyWindow : EditorUIPanel("Hierarchy"), IRenderable {
    private var renamingNode: INode? = null
    private var renameBuffer = ImString(128)

    override fun render(delta: Float) {
        beginUI()
        content = {
            val scene = EditorApplication.getInstance().getProjectManager().getCurrentScene()
            if (scene != null) {

                if (ImGui.beginPopupContextWindow("HierarchyContext", 1)) {
                    CreateMenuRenderable.render()
                    ImGui.endPopup()
                }

                scene.root.children.toList().forEach { drawNodeRecursive(it as GameObject) }

                val availY = ImGui.getContentRegionAvailY()
                if (availY > 0f) {
                    ImGui.invisibleButton("##dropzone", ImGui.getContentRegionAvailX(), availY)
                    if (ImGui.isItemClicked(0)) {
                        scene.getSelection().clearSelection()
                        renamingNode = null
                        renameBuffer.clear()
                    }

                    val payload = EditorDragManager.getPayload<GameObject>()
                    if (payload != null) {
                        val dl = ImGui.getWindowDrawList()
                        val min = ImGui.getItemRectMin()
                        val max = ImGui.getItemRectMax()
                        dl.addRect(min.x, min.y, max.x, max.y, ImGui.getColorU32(1f,1f,0f,1f),3f)
                    }

                    val min = ImGui.getItemRectMin()
                    val max = ImGui.getItemRectMax()
                    val droppedRoot = EditorDragManager.handleDrop(GameObject::class, min, max)
                    if (droppedRoot != null) {
                        droppedRoot.parent?.removeChild(droppedRoot)
                        scene.root.addChild(droppedRoot)
                    }
                }
            } else {
                EditorText.header("(No active scene)")
            }
        }
        endUI()
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

        val opened = ImGui.treeNodeEx(node.name + "##$nodeId", flags)
        val itemHovered = ImGui.isItemHovered()

        if (ImGui.isItemClicked(0)) {
            if (ImGui.isKeyDown(ImGuiKey.LeftShift)) selection.addSelection(node)
            else selection.select(node)

            EditorDragManager.beginDragCandidate(GameObject::class, node, "Move: ${node.name}")
        }

        if (itemHovered && ImGui.isMouseDoubleClicked(0)) {
            renamingNode = node
            renameBuffer.set(node.name)
        }

        ImGui.sameLine()
        if (renamingNode === node) {
            val enterPressed = ImGui.inputText("##rename_$nodeId", renameBuffer,
                ImGuiInputTextFlags.EnterReturnsTrue or ImGuiInputTextFlags.AutoSelectAll)
            if (enterPressed) {
                node.name = renameBuffer.get()
                renamingNode = null
            }
            if (ImGui.isKeyPressed(ImGuiKey.Escape)) renamingNode = null
        } else {
            ImGui.sameLine()
            ImGui.textDisabled("(${node.displayType})")
        }

        val payload = EditorDragManager.getPayload<GameObject>()
        val canDropHere = payload != null && payload.data !== node && !isDescendantOf(payload.data, node)
        if (canDropHere) {
            val dl = ImGui.getWindowDrawList()
            val min = ImGui.getItemRectMin()
            val max = ImGui.getItemRectMax()
            dl.addRect(min.x, min.y, max.x, max.y, ImGui.getColorU32(1f,1f,0f,1f),2f)
        }

        val min = ImGui.getItemRectMin()
        val max = ImGui.getItemRectMax()
        val dropped = EditorDragManager.handleDrop(GameObject::class, min, max)
        if (dropped != null && dropped !== node && !isDescendantOf(dropped, node)) {
            dropped.parent?.removeChild(dropped)
            node.addChild(dropped)
        }

        if (opened) {
            node.children.toList().forEach { drawNodeRecursive(it as GameObject) }
            ImGui.treePop()
        }

        ImGui.popID()
    }

    private fun isDescendantOf(parent: GameObject, possibleChild: GameObject): Boolean {
        for (child in parent.children) {
            if (child === possibleChild || isDescendantOf(child as GameObject, possibleChild)) return true
        }
        return false
    }

    override fun pushRenderCallback(cb: IRenderCallback) {}
}
