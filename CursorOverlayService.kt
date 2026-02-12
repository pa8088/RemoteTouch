// CursorOverlayService.kt
package com.example.remotetouch

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
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
        
        cursorView = ImageView(this).apply {
            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.RED)
                setStroke(3, Color.WHITE)
            }
            setImageDrawable(drawable)
            alpha = 0.8f
        }
        
        val params = WindowManager.LayoutParams(
            50,
            50,
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
    
    fun moveCursor(x: Int, y: Int) {
        cursorView?.let { view ->
            val params = view.layoutParams as WindowManager.LayoutParams
            params.x = x - 25
            params.y = y - 25
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
