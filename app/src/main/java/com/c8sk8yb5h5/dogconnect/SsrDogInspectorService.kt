package com.c8sk8yb5h5.dogconnect

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.graphics.Rect

/**
 * Diagnostic-only service. It records SSRDOG's accessibility tree for inspection.
 * It deliberately performs no clicks, gestures, or VPN state changes.
 */
class SsrDogInspectorService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.packageName?.toString() != SSR_DOG_PACKAGE) return
        val root = rootInActiveWindow ?: event.source ?: return
        val output = StringBuilder()
        appendNode(root, output, 0)
        getSharedPreferences(PREFS, MODE_PRIVATE)
            .edit()
            .putString(KEY_TREE, output.toString().take(MAX_CHARS))
            .putLong(KEY_TIME, System.currentTimeMillis())
            .apply()
    }

    private fun appendNode(node: AccessibilityNodeInfo, out: StringBuilder, depth: Int) {
        if (depth > MAX_DEPTH || out.length >= MAX_CHARS) return
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        val indent = "  ".repeat(depth)
        val text = node.text?.toString().orEmpty()
        val description = node.contentDescription?.toString().orEmpty()
        out.append(indent)
            .append(node.className ?: "View")
            .append(" text=").append(text.take(100))
            .append(" desc=").append(description.take(100))
            .append(" clickable=").append(node.isClickable)
            .append(" checkable=").append(node.isCheckable)
            .append(" checked=").append(node.isChecked)
            .append(" bounds=").append(bounds.flattenToString())
            .append('\n')
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            appendNode(child, out, depth + 1)
            child.recycle()
        }
    }

    override fun onInterrupt() = Unit

    companion object {
        const val PREFS = "dogconnect_inspector"
        const val KEY_TREE = "last_tree"
        const val KEY_TIME = "last_time"
        private const val SSR_DOG_PACKAGE = "com.ssrdog"
        private const val MAX_DEPTH = 18
        private const val MAX_CHARS = 12000
    }
}
