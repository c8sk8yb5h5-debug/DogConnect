package com.c8sk8yb5h5.dogconnect

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object UpdateManager {
    private const val RELEASE_API = "https://api.github.com/repos/c8sk8yb5h5-debug/DogConnect/releases/latest"

    fun check(activity: Activity, showUpToDate: Boolean = true) {
        Thread {
            try {
                val connection = (URL(RELEASE_API).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 12000
                    readTimeout = 12000
                    setRequestProperty("Accept", "application/vnd.github+json")
                    setRequestProperty("User-Agent", "DogConnect-Android")
                }
                val release = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
                connection.disconnect()
                if (release.optBoolean("draft") || release.optBoolean("prerelease")) {
                    throw IllegalStateException("暂无正式版本")
                }
                val latest = release.optString("tag_name").removePrefix("v")
                val current = activity.packageManager.getPackageInfo(activity.packageName, 0).versionName.orEmpty()
                val assets = release.getJSONArray("assets")
                val apk = (0 until assets.length()).map { assets.getJSONObject(it) }
                    .firstOrNull { it.optString("name").endsWith(".apk", true) }
                    ?: throw IllegalStateException("该版本没有 APK 文件")
                activity.runOnUiThread {
                    if (compareVersions(latest, current) > 0) {
                        AlertDialog.Builder(activity)
                            .setTitle("发现新版本 $latest")
                            .setMessage(release.optString("body").take(400).ifBlank { "是否下载并安装更新？" })
                            .setNegativeButton("稍后", null)
                            .setPositiveButton("立即更新") { _, _ ->
                                downloadAndInstall(activity, apk.getString("browser_download_url"), latest)
                            }.show()
                    } else if (showUpToDate) {
                        AlertDialog.Builder(activity).setMessage("当前已是最新版本")
                            .setPositiveButton("确定", null).show()
                    }
                }
            } catch (error: Exception) {
                activity.runOnUiThread {
                    if (!activity.isFinishing) AlertDialog.Builder(activity)
                        .setTitle("检查更新失败").setMessage(error.message ?: "请检查网络后重试")
                        .setPositiveButton("确定", null).show()
                }
            }
        }.start()
    }

    private fun downloadAndInstall(activity: Activity, url: String, version: String) {
        Thread {
            try {
                val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 30000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "DogConnect-Android")
                }
                val file = File(activity.cacheDir, "updates/DogConnect-$version.apk")
                file.parentFile?.mkdirs()
                connection.inputStream.use { input -> file.outputStream().use { output -> input.copyTo(output) } }
                connection.disconnect()
                @Suppress("DEPRECATION")
                val archive = activity.packageManager.getPackageArchiveInfo(file.absolutePath, 0)
                    ?: throw IllegalStateException("下载文件不是有效 APK")
                if (archive.packageName != activity.packageName) {
                    file.delete()
                    throw IllegalStateException("APK 包名不匹配，已取消安装")
                }
                activity.runOnUiThread {
                    if (Build.VERSION.SDK_INT >= 26 && !activity.packageManager.canRequestPackageInstalls()) {
                        activity.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                            Uri.parse("package:${activity.packageName}")))
                        AlertDialog.Builder(activity).setMessage("请允许 DogConnect 安装应用，然后重新点击更新。")
                            .setPositiveButton("确定", null).show()
                        return@runOnUiThread
                    }
                    val uri = FileProvider.getUriForFile(activity,
                        "${activity.packageName}.fileprovider", file)
                    val install = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/vnd.android.package-archive")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    activity.startActivity(install)
                }
            } catch (error: Exception) {
                activity.runOnUiThread {
                    if (!activity.isFinishing) AlertDialog.Builder(activity).setTitle("更新下载失败")
                        .setMessage(error.message ?: "请稍后重试").setPositiveButton("确定", null).show()
                }
            }
        }.start()
    }

    private fun compareVersions(first: String, second: String): Int {
        val a = first.split(".", "-", "+").map { it.toIntOrNull() ?: 0 }
        val b = second.split(".", "-", "+").map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(a.size, b.size)) {
            val diff = a.getOrElse(i) { 0 }.compareTo(b.getOrElse(i) { 0 })
            if (diff != 0) return diff
        }
        return 0
    }
}
