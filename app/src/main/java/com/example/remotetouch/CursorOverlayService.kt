// CursorOverlayService.kt
package com.example.remotetouch

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.ImageView

class CursorOverlayService : Service() {
    
    private var windowManager: WindowManager? = null
    private var cursorView: ImageView? = null
    
    companion object {
        private var instance: CursorOverlayService? = null
        
        fun getInstance(): CursorOverlayService? = instance
        
        fun updateCursorPosition(x: Int, y: Int) {
            instance?.moveCursor(x, y)
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        createCursor()
    }
    
    private fun createCursor() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        // 创建光标视图（红色圆点）
        cursorView = ImageView(this).apply {
            setBackgroundColor(Color.RED)
            alpha = 0.7f
        }
        
        // 设置窗口参数
        val params = WindowManager.LayoutParams(
            40, // 宽度（圆点大小）
            40, // 高度
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 0
        
        windowManager?.addView(cursorView, params)
    }
    
    private fun moveCursor(x: Int, y: Int) {
        cursorView?.let { view ->
            val params = view.layoutParams as WindowManager.LayoutParams
            params.x = x - 20 // 居中显示（圆点大小的一半）
            params.y = y - 20
            windowManager?.updateViewLayout(view, params)
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        cursorView?.let { windowManager?.removeView(it) }
        instance = null
    }
}
