// MainActivity.kt
package com.example.remotetouch

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvIp: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var btnSettings: Button
    private lateinit var btnCheck: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupListeners()
        checkPermissions()
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun initViews() {
        tvStatus = findViewById(R.id.tv_status)
        tvIp = findViewById(R.id.tv_ip)
        btnStart = findViewById(R.id.btn_start)
        btnStop = findViewById(R.id.btn_stop)
        btnSettings = findViewById(R.id.btn_settings)
        btnCheck = findViewById(R.id.btn_check)
    }

    private fun setupListeners() {
        btnStart.setOnClickListener { startService() }
        btnStop.setOnClickListener { stopService() }
        btnSettings.setOnClickListener { showSettingsGuide() }
        btnCheck.setOnClickListener { checkAndShowStatus() }
    }

    private fun startService() {
        if (!isAccessibilityServiceEnabled()) {
            AlertDialog.Builder(this)
                .setTitle("需要无障碍服务")
                .setMessage("请先开启无障碍服务才能使用触摸控制")
                .setPositiveButton("去设置") { _, _ ->
                    openAccessibilitySettings()
                }
                .setNegativeButton("取消", null)
                .show()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            AlertDialog.Builder(this)
                .setTitle("需要悬浮窗权限")
                .setMessage("需要悬浮窗权限来显示光标")
                .setPositiveButton("去设置") { _, _ ->
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                }
                .setNegativeButton("取消", null)
                .show()
            return
        }

        val serviceIntent = Intent(this, RemoteTouchForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        val cursorIntent = Intent(this, CursorOverlayService::class.java)
        startService(cursorIntent)

        updateStatus()
    }

    private fun stopService() {
        val intent = Intent(this, RemoteTouchForegroundService::class.java)
        stopService(intent)
        
        val cursorIntent = Intent(this, CursorOverlayService::class.java)
        stopService(cursorIntent)
        
        updateStatus()
    }

    private fun updateStatus() {
        val isAccessibilityRunning = isAccessibilityServiceEnabled()
        val isServiceRunning = RemoteTouchForegroundService.isRunning()

        val statusText = buildString {
            append("无障碍服务: ${if (isAccessibilityRunning) "✓ 已开启" else "✗ 未开启"}\n")
            append("前台服务: ${if (isServiceRunning) "✓ 运行中" else "✗ 已停止"}")
        }

        tvStatus.text = statusText

        if (isServiceRunning && isAccessibilityRunning) {
            val touchService = TouchAccessibilityService.getInstance()
            val ip = touchService?.let { WiFiTouchServer(8888, it).getLocalIpAddress() } ?: "0.0.0.0"
            tvIp.text = "请在电脑连接: $ip:8888"
        } else {
            tvIp.text = "服务未运行"
        }

        btnStart.isEnabled = !isServiceRunning
        btnStop.isEnabled = isServiceRunning
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                requestBatteryOptimization()
            }
        }
    }

    private fun requestBatteryOptimization() {
        AlertDialog.Builder(this)
            .setTitle("需要电池优化豁免")
            .setMessage("为保证服务稳定运行,请允许应用在后台不受限制运行")
            .setPositiveButton("去设置") { _, _ ->
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
            .setNegativeButton("稍后", null)
            .show()
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedComponentName = ComponentName(this, TouchAccessibilityService::class.java)
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        return enabledServices.split(":").any {
            ComponentName.unflattenFromString(it) == expectedComponentName
        }
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    private fun showSettingsGuide() {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val guide = buildString {
            append("=== 必须完成的设置 ===\n\n")

            append("1. 【开启无障碍服务】\n")
            append("   设置 → 无障碍 → 已下载的服务 → 远程触摸控制 → 开启\n\n")

            append("2. 【允许悬浮窗】\n")
            append("   设置 → 应用 → 远程触摸控制 → 显示在其他应用上层 → 允许\n\n")

            append("3. 【关闭电池优化】\n")
            append("   设置 → 电池 → 电池优化 → 远程触摸控制 → 不优化\n\n")

            append("4. 【允许后台运行】\n")
            when {
                manufacturer.contains("xiaomi") -> {
                    append("   设置 → 应用设置 → 应用管理 → 远程触摸控制\n")
                    append("   → 省电策略 → 无限制\n")
                    append("   → 自启动 → 开启\n")
                }
                manufacturer.contains("huawei") || manufacturer.contains("honor") -> {
                    append("   设置 → 应用和服务 → 应用启动管理\n")
                    append("   → 远程触摸控制 → 手动管理\n")
                    append("   → 全部勾选\n")
                }
                manufacturer.contains("oppo") -> {
                    append("   设置 → 应用管理 → 远程触摸控制\n")
                    append("   → 允许自启动\n")
                    append("   → 允许后台运行\n")
                }
                else -> {
                    append("   参考手机设置中的后台运行选项\n")
                }
            }

            append("\n5. 【锁定后台】\n")
            append("   最近任务界面 → 下拉本应用 → 锁定\n\n")

            append("完成以上设置后,服务将稳定运行不被杀死!")
        }

        AlertDialog.Builder(this)
            .setTitle("设置引导")
            .setMessage(guide)
            .setPositiveButton("知道了", null)
            .show()
    }

    private fun checkAndShowStatus() {
        val results = mutableListOf<String>()

        val isAccessibilityEnabled = isAccessibilityServiceEnabled()
        results.add("${if (isAccessibilityEnabled) "✓" else "✗"} 无障碍服务")

        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        val isBatteryOptimized = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pm.isIgnoringBatteryOptimizations(packageName)
        } else {
            true
        }
        results.add("${if (isBatteryOptimized) "✓" else "✗"} 电池优化豁免")

        val canDrawOverlays = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
        results.add("${if (canDrawOverlays) "✓" else "✗"} 悬浮窗权限")

        val isNotificationEnabled = NotificationManagerCompat.from(this).areNotificationsEnabled()
        results.add("${if (isNotificationEnabled) "✓" else "✗"} 通知权限")

        val isServiceRunning = RemoteTouchForegroundService.isRunning()
        results.add("${if (isServiceRunning) "✓" else "✗"} 前台服务运行")

        val allOk = isAccessibilityEnabled && isBatteryOptimized && canDrawOverlays && 
                    isNotificationEnabled && isServiceRunning

        val message = results.joinToString("\n") + "\n\n" +
                if (allOk) "✅ 所有检查通过,服务正常运行!"
                else "⚠️ 请完成未通过的设置项"

        AlertDialog.Builder(this)
            .setTitle("状态检查")
            .setMessage(message)
            .setPositiveButton("知道了", null)
            .show()
    }
}
