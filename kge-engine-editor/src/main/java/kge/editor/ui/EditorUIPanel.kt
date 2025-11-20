package kge.editor.ui

import imgui.ImGui
import imgui.flag.ImGuiTreeNodeFlags
import imgui.flag.ImGuiWindowFlags
import kge.api.editor.imgui.UIRenderable
import kge.editor.component.Component
import kge.ui.toolkit.EditorFont
import kge.ui.toolkit.delegates.EditorFieldDelegate
import kge.ui.toolkit.delegates.SerializeField
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation

open class EditorUIPanel(var title: String) : UIRenderable {
    var flags: Int = 0
        get() = field or windowFlagsMask()
    var isResizable: Boolean = true
    var isPinned: Boolean = false

    var content: () -> Unit = {}

    var onUserZoneRightClick: () -> Unit = {} // HierarchyPanel
    var onWindowHeaderRightClick: () -> Unit = { uiPanelContextMenu.let { ImGui.openPopup(it.id) } }

    private var uiPanelContextMenu: EditorUIPanelContextMenu = EditorUIPanelContextMenu.Default(this)

    override fun beginUI() {
        ImGui.begin(title, flags)

        //handleWindowDragging()
        content()

        getIsWindowHeaderClicked()
        uiPanelContextMenu.beginUI()
        uiPanelContextMenu.endUI()
    }

    override fun endUI() {
        ImGui.end()
    }

    fun getMouseInHeader(): Boolean {
        val mouseX = ImGui.getMousePosX()
        val mouseY = ImGui.getMousePosY()
        val winX = ImGui.getWindowPosX()
        val winY = ImGui.getWindowPosY()
        val winW = ImGui.getWindowWidth()
        val headerH = ImGui.getFrameHeight()

        return mouseX in winX..(winX + winW) && mouseY in winY..(winY + headerH)
    }

    fun drawInspectorForComponent(component: Component) {
        if (!component.shouldBeVisibleInInspector) return
        val flags = ImGuiTreeNodeFlags.DefaultOpen or
                ImGuiTreeNodeFlags.FramePadding or
                ImGuiTreeNodeFlags.SpanAvailWidth

        ImGui.dummy(0f, 2f)

        ImGui.setWindowFontScale(0.9f)
        ImGui.pushFont(EditorFont.semiBold)

        val isOpen = ImGui.treeNodeEx(
            "${component.typeName}##${System.identityHashCode(component)}",
            flags
        )

        ImGui.popFont()
        ImGui.setWindowFontScale(1f)

        if (!isOpen) {
            ImGui.separator()
            return
        }

        ImGui.separator()

        drawInspector(component)

        ImGui.separator()
        ImGui.treePop()
    }

    fun drawInspectorForFields(container: Any, title: String) {
        val flags = ImGuiTreeNodeFlags.DefaultOpen or
                ImGuiTreeNodeFlags.FramePadding or
                ImGuiTreeNodeFlags.SpanAvailWidth

        ImGui.dummy(0f, 2f)

        ImGui.setWindowFontScale(0.9f)
        ImGui.pushFont(EditorFont.semiBold)

        val isOpen = ImGui.treeNodeEx(
            "${title}##${System.identityHashCode(container)}",
            flags
        )

        ImGui.popFont()
        ImGui.setWindowFontScale(1f)

        if (!isOpen) {
            ImGui.separator()
            return
        }

        drawInspector(container)

        ImGui.separator()
        ImGui.treePop()
    }

    fun drawInspector(component: Any) {
        for (member in component::class.declaredMemberProperties) {
            @Suppress("unchecked_cast")
            val prop = member as? KProperty1<Any, *> ?: continue

            val annotation = prop.findAnnotation<SerializeField>() ?: continue
            val label = annotation.label.ifEmpty { prop.name }

            val delegateObj = try {
                val f = component.javaClass.getDeclaredField("${prop.name}\$delegate")
                f.isAccessible = true
                f.get(component)
            } catch (_: NoSuchFieldException) {
                null
            }

            val fieldDelegate = delegateObj as? EditorFieldDelegate<*> ?: continue
            fieldDelegate.draw(label)
        }
    }

    private fun getIsWindowHeaderClicked() {
        if (getMouseInHeader()) {
            if (ImGui.isMouseClicked(1)) {
                onWindowHeaderRightClick()
            }
        }
    }

    private fun windowFlagsMask(): Int {
        var flags = 0
        if (isPinned) flags = flags or ImGuiWindowFlags.NoMove
        if (!isResizable) flags = flags or ImGuiWindowFlags.NoResize
        return flags
    }
}