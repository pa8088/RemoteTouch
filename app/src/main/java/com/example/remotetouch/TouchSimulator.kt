// TouchSimulator.kt
package com.example.remotetouch

interface TouchSimulator {
    fun click(x: Int, y: Int)
    fun move(x: Int, y: Int)
    fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, durationMs: Long)
    fun longPress(x: Int, y: Int, durationMs: Long)
}
