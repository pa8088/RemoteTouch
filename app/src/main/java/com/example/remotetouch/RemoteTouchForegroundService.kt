// RemoteTouchForegroundService.kt
package com.example.remotetouch

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import android.util.Log

class RemoteTouchForegroundService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "remote_touch_channel"
        private var isServiceRunning = false

        fun isRunning() = isServiceRunning
    }

    private val TAG = "RemoteTouchService"
    private var wifiServer: WiFiTouchServer? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "服务创建")

        // 创建通知渠道
        createNotificationChannel()

        // 获取WakeLock防止休眠
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "服务启动")

        // 启动前台服务
        startForeground(NOTIFICATION_ID, createNotification("正在启动..."))

        // 启动WiFi服务器
        startWiFiServer()

        isServiceRunning = true

        // START_STICKY: 系统杀死后会尝试重启
        return START_STICKY
    }

    private fun startWiFiServer() {
        val touchService = TouchAccessibilityService.getInstance()

        if (touchService == null) {
            updateNotification("错误: 请先开启无障碍服务")
            Log.e(TAG, "无障碍服务未运行")
            return
        }

        wifiServer = WiFiTouchServer(8888, touchService).apply {
            statusCallback = object : WiFiTouchServer.StatusCallback {
                override fun onServerStarted(ip: String, port: Int) {
                    updateNotification("运行中 - $ip:$port")
                }

                override fun onClientConnected(clientIp: String) {
                    updateNotification("已连接 - $clientIp")
                }

                override fun onClientDisconnected(clientIp: String) {
                    val localIp = wifiServer?.getLocalIpAddress() ?: "0.0.0.0"
                    updateNotification("等待连接 - $localIp:8888")
                }

                override fun onError(error: String) {
                    updateNotification("错误: $error")
                }
            }
            start()
        }
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "RemoteTouch::WakeLock"
        ).apply {
            acquire(10 * 60 * 60 * 1000L) // 10小时
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "远程触摸控制",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "保持远程触摸服务运行"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(contentText: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("远程触摸控制")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(contentText: String) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, createNotification(contentText))
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "服务销毁")

        wifiServer?.stop()
        wakeLock?.release()
        isServiceRunning = false
    }
}
