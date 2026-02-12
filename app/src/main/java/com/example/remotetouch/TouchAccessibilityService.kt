// TouchAccessibilityService.kt
package com.example.remotetouch

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import android.util.Log

class TouchAccessibilityService : AccessibilityService(), TouchSimulator {

    companion object {
        private var instance: TouchAccessibilityService? = null

        fun getInstance(): TouchAccessibilityService? = instance

        fun isRunning(): Boolean = instance != null
    }

    private val TAG = "TouchAccessibility"

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.i(TAG, "无障碍服务已连接")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 不需要处理事件,仅用于触摸模拟
    }

    override fun onInterrupt() {
        Log.w(TAG, "服务被中断")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        Log.i(TAG, "服务已销毁")
    }

    override fun click(x: Int, y: Int) {
        Log.d(TAG, "点击: ($x, $y)")

        val path = Path().apply {
            moveTo(x.toFloat(), y.toFloat())
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
            .build()

        dispatchGesture(gesture, null, null)
    }

    override fun move(x: Int, y: Int) {
        Log.d(TAG, "移动: ($x, $y)")
        // 更新屏幕光标位置
        CursorOverlayService.updateCursorPosition(x, y)
    }

    override fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, durationMs: Long) {
        Log.d(TAG, "滑动: ($x1,$y1) -> ($x2,$y2)")

        val path = Path().apply {
            moveTo(x1.toFloat(), y1.toFloat())
            lineTo(x2.toFloat(), y2.toFloat())
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, durationMs))
            .build()

        dispatchGesture(gesture, null, null)
    }

    override fun longPress(x: Int, y: Int, durationMs: Long) {
        Log.d(TAG, "长按: ($x, $y)")

        val path = Path().apply {
            moveTo(x.toFloat(), y.toFloat())
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, durationMs))
            .build()

        dispatchGesture(gesture, null, null)
    }
}
