// WiFiTouchServer.kt
package com.example.remotetouch

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.net.NetworkInterface

class WiFiTouchServer(
    private val port: Int = 8888,
    private val touchSimulator: TouchSimulator
) {
    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    private val TAG = "WiFiTouchServer"

    interface StatusCallback {
        fun onServerStarted(ip: String, port: Int)
        fun onClientConnected(clientIp: String)
        fun onClientDisconnected(clientIp: String)
        fun onError(error: String)
    }

    var statusCallback: StatusCallback? = null

    fun start() {
        if (isRunning) {
            Log.w(TAG, "服务已在运行")
            return
        }

        isRunning = true

        Thread {
            try {
                serverSocket = ServerSocket(port)
                val localIp = getLocalIpAddress()
                Log.i(TAG, "服务启动成功: $localIp:$port")
                statusCallback?.onServerStarted(localIp, port)

                while (isRunning) {
                    try {
                        val clientSocket = serverSocket?.accept()
                        clientSocket?.let { handleClient(it) }
                    } catch (e: Exception) {
                        if (isRunning) {
                            Log.e(TAG, "接受连接失败", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "服务启动失败", e)
                statusCallback?.onError("启动失败: ${e.message}")
            }
        }.start()
    }

    private fun handleClient(socket: Socket) {
        val clientIp = socket.inetAddress.hostAddress ?: "unknown"
        Log.i(TAG, "客户端连接: $clientIp")
        statusCallback?.onClientConnected(clientIp)

        Thread {
            try {
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                while (isRunning) {
                    val line = reader.readLine() ?: break
                    if (line.isNotEmpty()) {
                        parseAndExecute(line.trim())
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "处理客户端数据失败", e)
            } finally {
                socket.close()
                Log.i(TAG, "客户端断开: $clientIp")
                statusCallback?.onClientDisconnected(clientIp)
            }
        }.start()
    }

    private fun parseAndExecute(command: String) {
        try {
            val parts = command.split(":")
            when (parts[0]) {
                "CLICK" -> {
                    val coords = parts[1].split(",")
                    val x = coords[0].toInt()
                    val y = coords[1].toInt()
                    touchSimulator.click(x, y)
                }
                "MOVE" -> {
                    val coords = parts[1].split(",")
                    val x = coords[0].toInt()
                    val y = coords[1].toInt()
                    touchSimulator.move(x, y)
                }
                "SWIPE" -> {
                    val params = parts[1].split(",")
                    touchSimulator.swipe(
                        params[0].toInt(),
                        params[1].toInt(),
                        params[2].toInt(),
                        params[3].toInt(),
                        params[4].toLong()
                    )
                }
                "LONG_PRESS" -> {
                    val params = parts[1].split(",")
                    touchSimulator.longPress(
                        params[0].toInt(),
                        params[1].toInt(),
                        params[2].toLong()
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "解析指令失败: $command", e)
        }
    }

    fun stop() {
        isRunning = false
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "关闭服务失败", e)
        }
    }

    fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                if (iface.isLoopback || !iface.isUp) continue

                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                        return addr.hostAddress ?: ""
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取IP失败", e)
        }
        return "0.0.0.0"
    }
}
