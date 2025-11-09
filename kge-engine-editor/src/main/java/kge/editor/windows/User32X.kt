package kge.editor.windows

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.platform.win32.WinDef

interface User32X : Library {
    companion object {
        val INSTANCE: User32X = Native.load("user32", User32X::class.java)
    }

    fun MessageBoxW(hwnd: WinDef.HWND?, text: String, caption: String, type: Int): Int
}