package com.craftware.engine

import javax.swing.JOptionPane

object ExceptionFactory {
    fun createErrorWindow(title: String, message: String) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE)
    }

    fun createInfoWindow(title: String, message: String) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE)
    }
}