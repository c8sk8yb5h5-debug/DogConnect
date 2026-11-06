package com.c8sk8yb5h5.dogconnect

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.provider.Settings.Secure
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var serviceStatus: TextView
    private lateinit var diagnostic: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(244, 247, 251)
        window.navigationBarColor = Color.rgb(244, 247, 251)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(28), dp(24), dp(24))
            setBackgroundColor(Color.rgb(244, 247, 251))
        }
        val title = TextView(this).apply {
            text = "DogConnect"
            textSize = 28f
            setTextColor(Color.rgb(25, 35, 50))
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        val subtitle = TextView(this).apply {
            text = "SSRDOG Lite"
            textSize = 14f
            setTextColor(Color.rgb(120, 132, 148))
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(28))
        }
        val launch = actionButton("打开 SSRDOG") { launchSsrDog() }
        val update = actionButton("检查更新") { UpdateManager.check(this) }.apply {\n            setTextColor(Color.rgb(35, 105, 180))\n            background = rounded(Color.WHITE)\n        }\n        val enable = actionButton("开启控件检测") {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }.apply {
            setTextColor(Color.rgb(35, 105, 180))
            background = rounded(Color.WHITE)
        }
        serviceStatus = TextView(this).apply {
            textSize = 13f
            setTextColor(Color.rgb(100, 115, 130))
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, dp(18))
        }
        val reportTitle = TextView(this).apply {
            text = "控件检测结果"
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(45, 55, 70))
            setPadding(0, dp(12), 0, dp(8))
        }
        diagnostic = TextView(this).apply {
            textSize = 11f
            setTextColor(Color.rgb(75, 85, 100))
            setTextIsSelectable(true)
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = rounded(Color.WHITE)
        }
        val scroll = ScrollView(this).apply {
            addView(diagnostic)
        }

        root.addView(title)
        root.addView(subtitle)
        root.addView(launch, LinearLayout.LayoutParams(-1, dp(58)))\n        root.addView(update, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(10) })
        root.addView(enable, LinearLayout.LayoutParams(-1, dp(52)).apply { topMargin = dp(12) })
        root.addView(serviceStatus)
        root.addView(reportTitle)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        refreshDiagnostic()
    }

    override fun onResume() {
        super.onResume()
        if (::serviceStatus.isInitialized) refreshDiagnostic()
    }

    private fun refreshDiagnostic() {
        val enabled = isInspectorEnabled()
        serviceStatus.text = if (enabled) "控件检测已开启" else "控件检测未开启"
        val prefs = getSharedPreferences(SsrDogInspectorService.PREFS, MODE_PRIVATE)
        val tree = prefs.getString(SsrDogInspectorService.KEY_TREE, null)
        diagnostic.text = tree ?: "暂无数据。开启控件检测后，打开 SSRDOG Lite，再返回此页面。"
    }

    private fun isInspectorEnabled(): Boolean {
        val enabled = Secure.getString(contentResolver, Secure.ENABLED_ACCESSIBILITY_SERVICES).orEmpty()
        val component = android.content.ComponentName(this, SsrDogInspectorService::class.java)
        return enabled.split(':').any { it.equals(component.flattenToString(), ignoreCase = true) }
    }

    private fun launchSsrDog() {
        val intent = packageManager.getLaunchIntentForPackage(SSR_DOG_PACKAGE)
        if (intent == null) {
            android.widget.Toast.makeText(this, "请先安装 SSRDOG Lite", android.widget.Toast.LENGTH_LONG).show()
            return
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun actionButton(label: String, action: () -> Unit) = TextView(this).apply {
        text = label
        textSize = 17f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(Color.WHITE)
        gravity = Gravity.CENTER
        background = rounded(Color.rgb(22, 137, 245))
        isClickable = true
        isFocusable = true
        setOnClickListener { action() }
    }

    private fun rounded(color: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(24).toFloat()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        private const val SSR_DOG_PACKAGE = "com.ssrdog"
    }
}
