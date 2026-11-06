package com.c8sk8yb5h5.dogconnect

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/** State-aware SSRDOG connector; never toggles an already connected tunnel. */
class SsrDogInspectorService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())
    private var pendingScan: Runnable? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.packageName?.toString() != SSR_DOG_PACKAGE) return
        if (event.eventType !in setOf(
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
                AccessibilityEvent.TYPE_VIEW_FOCUSED
            )) return
        pendingScan?.let(handler::removeCallbacks)
        pendingScan = Runnable { inspectAndConnect() }.also { handler.postDelayed(it, SCAN_DELAY_MS) }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        handler.postDelayed({ inspectAndConnect() }, SCAN_DELAY_MS)
    }

    private fun inspectAndConnect() {
        val root = rootInActiveWindow ?: return
        val output = StringBuilder()
        appendNode(root, output, 0)
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val nodes = mutableListOf<NodeSnapshot>()
        collectNodes(root, nodes, 0)
        prefs.edit().putString(KEY_TREE, output.toString().take(MAX_CHARS))
            .putLong(KEY_TIME, System.currentTimeMillis()).apply()

        if (nodes.any { it.text.contains("已连接") || it.description.contains("已连接") }) {
            prefs.edit().putString(KEY_STATE, "VPN 已连接").apply()
            return
        }
        val disconnected = nodes.any {
            it.text.contains("未开启") || it.description.contains("未开启") ||
                it.text.contains("当前未保护") || it.description.contains("当前未保护")
        }
        if (!disconnected) {
            prefs.edit().putString(KEY_STATE, "等待识别 SSRDOG 连接状态").apply()
            return
        }

        val now = System.currentTimeMillis()
        if (now - prefs.getLong(KEY_LAST_ATTEMPT, 0L) < ATTEMPT_COOLDOWN_MS) return
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        (getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay.getRealMetrics(metrics)
        val target = chooseConnectControl(nodes, metrics.widthPixels, metrics.heightPixels)
        if (target == null) {
            prefs.edit().putString(KEY_STATE, "未找到明确的连接开关，未执行点击").apply()
            return
        }

        prefs.edit().putLong(KEY_LAST_ATTEMPT, now)
            .putString(KEY_STATE, "检测到未连接，正在发起连接").apply()
        val clicked = target.node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        prefs.edit().putString(KEY_STATE,
            if (clicked) "已发起连接，等待 SSRDOG 确认" else "连接开关暂不可操作").apply()
    }

    private fun chooseConnectControl(nodes: List<NodeSnapshot>, width: Int, height: Int): NodeSnapshot? {
        val candidates = nodes.filter { item ->
            val b = item.bounds
            val central = b.centerX() in (width * 0.25).toInt()..(width * 0.75).toInt() &&
                b.centerY() in (height * 0.15).toInt()..(height * 0.55).toInt()
            val size = b.width() in 40..(width * 0.8).toInt() &&
                b.height() in 30..(height * 0.22).toInt()
            central && size && (item.checkable || item.clickable)
        }
        val checkable = candidates.filter { it.checkable }
        if (checkable.size == 1) return checkable.first()
        if (checkable.size > 1 || candidates.isEmpty()) return null
        val ranked = candidates.sortedBy { score(it.bounds, width, height) }
        if (ranked.size == 1) return ranked.first()
        val first = score(ranked[0].bounds, width, height)
        val second = score(ranked[1].bounds, width, height)
        return if (second - first > width * width * 0.025f) ranked.first() else null
    }

    private fun score(bounds: Rect, width: Int, height: Int): Float {
        val dx = bounds.centerX() - width / 2f
        val dy = bounds.centerY() - height * 0.31f
        return dx * dx + dy * dy
    }

    private fun collectNodes(node: AccessibilityNodeInfo, output: MutableList<NodeSnapshot>, depth: Int) {
        if (depth > MAX_DEPTH) return
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        output += NodeSnapshot(node, node.text?.toString().orEmpty(),
            node.contentDescription?.toString().orEmpty(), node.isClickable, node.isCheckable, bounds)
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            collectNodes(child, output, depth + 1)
        }
    }

    private fun appendNode(node: AccessibilityNodeInfo, out: StringBuilder, depth: Int) {
        if (depth > MAX_DEPTH || out.length >= MAX_CHARS) return
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        out.append("  ".repeat(depth)).append(node.className ?: "View")
            .append(" text=").append(node.text?.toString().orEmpty().take(100))
            .append(" desc=").append(node.contentDescription?.toString().orEmpty().take(100))
            .append(" clickable=").append(node.isClickable)
            .append(" checkable=").append(node.isCheckable)
            .append(" checked=").append(node.isChecked)
            .append(" bounds=").append(bounds.flattenToString()).append('\n')
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            appendNode(child, out, depth + 1)
        }
    }

    override fun onInterrupt() = Unit

    private data class NodeSnapshot(
        val node: AccessibilityNodeInfo,
        val text: String,
        val description: String,
        val clickable: Boolean,
        val checkable: Boolean,
        val bounds: Rect
    )

    companion object {
        const val PREFS = "dogconnect_inspector"
        const val KEY_TREE = "last_tree"
        const val KEY_TIME = "last_time"
        const val KEY_STATE = "connection_state"
        private const val KEY_LAST_ATTEMPT = "last_connect_attempt"
        private const val SSR_DOG_PACKAGE = "com.ssrdog"
        private const val MAX_DEPTH = 18
        private const val MAX_CHARS = 12000
        private const val SCAN_DELAY_MS = 450L
        private const val ATTEMPT_COOLDOWN_MS = 15_000L
    }
}
