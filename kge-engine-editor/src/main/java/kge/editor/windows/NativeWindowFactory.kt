package kge.editor.windows

import com.sun.jna.platform.win32.WinDef

fun messageBox(
    lpText: String = "KGE Editor",
    lpCaption: String,
    type: MessageBoxButtons = MessageBoxButtons.OK,
    icon: MessageBoxIcon = MessageBoxIcon.ICON_INFORMATION,
    defaultButton: MessageBoxDefaultButton = MessageBoxDefaultButton.DEFBUTTON1,
    options: MessageBoxModalityAndOptions = MessageBoxModalityAndOptions.APPLY_MODAL
): Int {
    val hwnd: WinDef.HWND? = null
    val flags = type.value or icon.value or defaultButton.value or options.value

    return User32X.INSTANCE.MessageBoxW(hwnd, lpText, lpCaption, flags)
}

enum class MessageBoxButtons(val value: Int) {
    OK(0x00000000),
    OK_CANCEL(0x00000001),
    ABORT_RETRY_IGNORE(0x00000002),
    YES_NO_CANCEL(0x00000003),
    YES_NO(0x00000004),
    RETRY_CANCEL(0x00000005),
    CANCEL_TRY_CONTINUE(0x00000006),
    HELP(0x00004000)
}

enum class MessageBoxIcon(val value: Int) {
    ICON_STOP(0x00000010),        // MB_ICONSTOP / MB_ICONERROR / MB_ICONHAND
    ICON_ERROR(0x00000010),
    ICON_HAND(0x00000010),
    ICON_QUESTION(0x00000020),
    ICON_EXCLAMATION(0x00000030), // MB_ICONEXCLAMATION / MB_ICONWARNING
    ICON_WARNING(0x00000030),
    ICON_INFORMATION(0x00000040), // MB_ICONINFORMATION / MB_ICONASTERISK
    ICON_ASTERISK(0x00000040)
}

enum class MessageBoxDefaultButton(val value: Int) {
    DEFBUTTON1(0x00000000),
    DEFBUTTON2(0x00000100),
    DEFBUTTON3(0x00000200),
    DEFBUTTON4(0x00000300)
}

enum class MessageBoxModalityAndOptions(val value: Int) {
    APPLY_MODAL(0x00000000),
    SYSTEM_MODAL(0x00001000),
    TASK_MODAL(0x00002000),
    DEFAULT_DESKTOP_ONLY(0x00020000),
    RIGHT(0x00080000),
    RTL_READING(0x00100000),
    SET_FOREGROUND(0x00010000),
    TOPMOST(0x00040000),
    SERVICE_NOTIFICATION(0x00200000)
}
