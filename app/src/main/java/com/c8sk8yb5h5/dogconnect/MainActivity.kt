package com.c8sk8yb5h5.dogconnect

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(244, 247, 251)
        window.navigationBarColor = Color.rgb(244, 247, 251)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(28), dp(32), dp(28), dp(32))
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
            setPadding(0, dp(8), 0, dp(36))
        }
        val button = TextView(this).apply {
            text = "打开 SSRDOG"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                setColor(Color.rgb(22, 137, 245))
                cornerRadius = dp(28).toFloat()
            }
            setPadding(dp(34), dp(18), dp(34), dp(18))
            isClickable = true
            isFocusable = true
            setOnClickListener { launchSsrDog() }
        }
        val status = TextView(this).apply {
            textSize = 13f
            setTextColor(Color.rgb(120, 132, 148))
            gravity = Gravity.CENTER
            setPadding(0, dp(22), 0, 0)
            text = if (isSsrDogInstalled()) "已检测到 SSRDOG Lite" else "未检测到 SSRDOG Lite"
        }

        root.addView(title)
        root.addView(subtitle)
        root.addView(button, LinearLayout.LayoutParams(-1, dp(60)))
        root.addView(status)
        setContentView(root)
    }

    private fun launchSsrDog() {
        val launchIntent = packageManager.getLaunchIntentForPackage(SSR_DOG_PACKAGE)
        if (launchIntent == null) {
            android.widget.Toast.makeText(this, "请先安装 SSRDOG Lite", android.widget.Toast.LENGTH_LONG).show()
            return
        }
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(launchIntent)
    }

    private fun isSsrDogInstalled(): Boolean =
        packageManager.getLaunchIntentForPackage(SSR_DOG_PACKAGE) != null

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        private const val SSR_DOG_PACKAGE = "com.ssrdog"
    }
}
