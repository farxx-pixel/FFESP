package com.esp.ffindia

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.*
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat

class OverlayService : Service() {

    companion object {
        private const val PKG = "com.dts.freefireth"   // Free Fire India package
        private const val CHANNEL_ID = "esp_shizuku"
    }

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: OverlayView
    private val reader = MemoryReader()
    private val handler = Handler(Looper.getMainLooper())
    private var running = true
    private var moduleBase = 0L

    // ========== OFFSETS (update after every Free Fire India OB) ==========
    private val OFF_GAME_FACADE   = 0x0L
    private val OFF_MATCH         = 0x90L
    private val OFF_LOCAL_PLAYER  = 0x58L
    private val OFF_PLAYER_LIST   = 0xA8L
    private val OFF_PLAYER_COUNT  = 0x18L
    private val OFF_POS           = 0x1C0L
    private val OFF_HEALTH        = 0x1E8L
    private val OFF_NAME_PTR      = 0x2A0L
    private val OFF_TEAM          = 0x2D0L
    private val OFF_IS_DEAD       = 0x1F4L
    private val OFF_VIEW_MATRIX   = 0xD8L
    // ====================================================================

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, buildNotification())
        setupOverlay()
        startLoop()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "ESP", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ESP Active (Shizuku)")
            .setContentText("Non-root external overlay")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .build()
    }

    private fun setupOverlay() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayView = OverlayView(this)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        windowManager.addView(overlayView, params)
    }

    private fun startLoop() {
        handler.post(object : Runnable {
            override fun run() {
                if (!running) return
                try {
                    if (reader.attach(PKG)) {
                        if (moduleBase == 0L) {
                            val pid = ShizukuHelper.getPid(PKG)
                            moduleBase = ShizukuHelper.getModuleBase(pid, "libil2cpp.so")
                        }
                        val frame = readEspFrame()
                        overlayView.update(frame)
                    }
                } catch (_: Exception) {}
                handler.postDelayed(this, 16)
            }
        })
    }

    private fun readEspFrame(): EspFrame {
        // Walk entities using current offsets + NativeBridge.worldToScreen
        // Returns empty until you put real offsets from Il2CppDumper
        return EspFrame(emptyList())
    }

    override fun onDestroy() {
        running = false
        reader.close()
        try { windowManager.removeView(overlayView) } catch (_: Exception) {}
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private class OverlayView(context: android.content.Context) : View(context) {
        private var frame: EspFrame? = null
        private val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.GREEN
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        }
        private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.RED
            strokeWidth = 1.8f
        }
        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 28f
            setShadowLayer(3f, 1f, 1f, Color.BLACK)
        }

        fun update(newFrame: EspFrame) {
            frame = newFrame
            postInvalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val f = frame ?: return
            val cx = width / 2f
            val cy = height.toFloat()

            for (p in f.players) {
                if (!p.isEnemy) continue
                canvas.drawLine(cx, cy, p.screenX, p.screenY, linePaint)
                val left = p.screenX - p.boxW / 2
                val top = p.screenY - p.boxH
                canvas.drawRect(left, top, left + p.boxW, p.screenY, boxPaint)
                canvas.drawText("${p.name} ${p.distance.toInt()}m", left, top - 8f, textPaint)
            }
        }
    }
}
