package kge.editor.ui.window

import imgui.ImGui
import imgui.flag.ImGuiInputTextFlags
import imgui.flag.ImGuiKey
import imgui.flag.ImGuiTreeNodeFlags
import imgui.type.ImString
import kge.api.editor.imgui.IRenderCallback
import kge.api.std.IRenderable
import kge.editor.*
import kge.editor.core.GameObject
import kge.ui.toolkit.UIText
import kge.editor.ui.EditorUIPanel
import kge.editor.ui.UIIcon
import kge.ui.toolkit.dragndrop.EditorDragManager
import kge.ui.toolkit.dragndrop.TextDecorations

class SceneHierarchyWindow : EditorUIPanel("Hierarchy"), IRenderable {
    private var renamingNode: GameObject? = null
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

                drawNodeRecursive(scene.root)

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
                UIText.header("(No active scene)")
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
        val flags =
            ImGuiTreeNodeFlags.OpenOnArrow or
                    ImGuiTreeNodeFlags.SpanAvailWidth or
                    ImGuiTreeNodeFlags.DefaultOpen or
                    (if (!hasChildren) ImGuiTreeNodeFlags.Leaf else 0) or
                    (if (isSelected) ImGuiTreeNodeFlags.Selected else 0)

        val textLineHeight = ImGui.getTextLineHeight()
        val framePadding = ImGui.getStyle().framePadding.y
        val totalItemHeight = textLineHeight + framePadding * 2f

        val cursorPosBefore = ImGui.getCursorPos()
        val leafPadding = 30f

        ImGui.setCursorPos(cursorPosBefore.x + leafPadding, cursorPosBefore.y)
        ImGui.invisibleButton(
            "##node_area_$nodeId",
            ImGui.getContentRegionAvailX() - leafPadding,
            totalItemHeight
        )

        val nodeAreaMin = ImGui.getItemRectMin()
        val nodeAreaMax = ImGui.getItemRectMax()

        val nodeAreaHovered = ImGui.isItemHovered()
        val nodeAreaClicked = ImGui.isItemClicked(0)

        if (nodeAreaClicked) {
            if (ImGui.isKeyDown(ImGuiKey.LeftShift))
                selection.addSelection(node)
            else if (ImGui.isKeyDown(ImGuiKey.LeftCtrl))
                selection.deselect(node)
            else selection.select(node)

            EditorDragManager.beginDragCandidate(GameObject::class, node, "Move: ${node.name}")
        }

        if (nodeAreaHovered && ImGui.isMouseDoubleClicked(0)) {
            renamingNode = node
            renameBuffer.set(node.name)
        }

        ImGui.setCursorPos(cursorPosBefore)

        if (!node.activeInHierarchy) TextDecorations.pushGrayedText()

        val opened = ImGui.treeNodeEx("##node_$nodeId", flags)

        ImGui.sameLine()

        val iconSize = ImGui.getTextLineHeight()
        val iconSpacing = 4f
        ImGui.image(UIIcon.getIcon("go_sillouette")!!, iconSize, iconSize)
        ImGui.sameLine()
        ImGui.setCursorPosX(ImGui.getCursorPosX() + iconSpacing)

        if (renamingNode === node) {
            val enterPressed = ImGui.inputText(
                "##rename_$nodeId", renameBuffer,
                ImGuiInputTextFlags.EnterReturnsTrue or ImGuiInputTextFlags.AutoSelectAll
            )
            if (enterPressed) {
                node.name = renameBuffer.get()
                renamingNode = null
            }
            if (ImGui.isKeyPressed(ImGuiKey.Escape)) renamingNode = null
        } else {
            ImGui.text(node.name)
        }

        if (!node.activeInHierarchy) TextDecorations.popColor()

        if (ImGui.beginPopupContextItem("NodeContext_$nodeId")) {
            if (!isSelected) {
                selection.select(node)
            }

            CreateMenuRenderable.render(node)
            ImGui.separator()

            if (ImGui.menuItem("Rename")) {
                renamingNode = node
                renameBuffer.set(node.name)
            }
            if (ImGui.menuItem("Delete")) {
                GameObject.destroy(node)
            }

            ImGui.endPopup()
        }

        if (ImGui.isKeyPressed(ImGuiKey.Delete) && isSelected) {
            GameObject.destroy(node)
        }

        val payload = EditorDragManager.getPayload<GameObject>()
        val canDropHere = payload != null && payload.data !== node && !isDescendantOf(payload.data, node)
        if (canDropHere) {
            val dl = ImGui.getWindowDrawList()
            dl.addRect(
                nodeAreaMin.x,
                nodeAreaMin.y - ImGui.getStyle().itemSpacingY,
                nodeAreaMax.x,
                nodeAreaMax.y - ImGui.getStyle().itemSpacingY,
                ImGui.getColorU32(1f, 1f, 0f, 1f),
                2f
            )
        }

        val dropped = EditorDragManager.handleDrop(GameObject::class, nodeAreaMin, nodeAreaMax)
        if (dropped != null && dropped !== node && !isDescendantOf(dropped, node)) {
            dropped.parent?.removeChild(dropped)
            node.addChild(dropped)
        }

        if (opened) {
            node.children.toList().forEach { drawNodeRecursive(it) }
            ImGui.treePop()
        }

        ImGui.popID()
    }

    private fun isDescendantOf(parent: GameObject, possibleChild: GameObject): Boolean {
        for (child in parent.children) {
            if (child === possibleChild || isDescendantOf(child, possibleChild)) return true
        }
        return false
    }

    override fun pushRenderCallback(cb: IRenderCallback) {}
}
