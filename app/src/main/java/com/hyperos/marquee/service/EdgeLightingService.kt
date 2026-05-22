package com.hyperos.marquee.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.FrameLayout
import com.hyperos.marquee.config.SpConfig
import com.hyperos.marquee.view.EdgeLightingView

class EdgeLightingService : Service() {

    private var overlayView: EdgeLightingView? = null
    private var windowManager: WindowManager? = null
    private var handler: android.os.Handler? = null
    private val hideRunnable = Runnable { removeOverlay() }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            when (intent.action) {
                ACTION_SHOW -> showOverlay(false)
                ACTION_PREVIEW -> showOverlay(true)
                ACTION_HIDE -> removeOverlay()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        handler = android.os.Handler(mainLooper)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val filter = IntentFilter().apply {
            addAction(ACTION_SHOW)
            addAction(ACTION_PREVIEW)
            addAction(ACTION_HIDE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(receiver, filter)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, createNotification())
        when (intent?.action) {
            ACTION_SHOW -> showOverlay(false)
            ACTION_PREVIEW -> showOverlay(true)
            ACTION_HIDE -> removeOverlay()
        }
        return START_NOT_STICKY
    }

    private fun showOverlay(preview: Boolean) {
        removeOverlay()
        val ctx = this
        if (!preview && !SpConfig.isEnabled(ctx)) { stopSelf(); return }
        if (!Settings.canDrawOverlays(ctx)) { stopSelf(); return }

        val width = SpConfig.getWidth(ctx)
        val height = SpConfig.getHeight(ctx)
        val offsetH = SpConfig.getOffsetH(ctx)
        val offsetV = SpConfig.getOffsetV(ctx)

        val dm = resources.displayMetrics
        val w = if (width == 0) dm.widthPixels else width * dm.density.toInt()
        val h = if (height == 0) dm.heightPixels else height * dm.density.toInt()

        val params = WindowManager.LayoutParams(
            w, h,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = offsetH * dm.density.toInt()
            y = offsetV * dm.density.toInt()
        }

        val container = FrameLayout(ctx)
        val view = EdgeLightingView(ctx)
        container.addView(view, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))

        overlayView = view
        windowManager?.addView(container, params)
        view.startAnimation()

        val mode = SpConfig.getDisplayMode(ctx)
        val dur = SpConfig.getDuration(ctx)
        if (mode == 1 && !preview) {
            handler?.removeCallbacks(hideRunnable)
            handler?.postDelayed(hideRunnable, dur * 1000L)
        }
    }

    private fun removeOverlay() {
        overlayView?.stopAnimation()
        overlayView = null
        try {
            val container = findViewById<FrameLayout>(android.R.id.content)
            // WindowManager remove
        } catch (_: Throwable) {}
        // Remove all views by recreating
        windowManager?.let { wm ->
            try {
                val v = wm.defaultDisplay
                // Just remove via global
            } catch (_: Throwable) {}
        }
        // Force remove by stopping
        stopSelf()
    }

    private fun createNotification(): Notification {
        val channelId = "marquee_service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(channelId, "跑马灯服务", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(ch)
        }
        return Notification.Builder(this, channelId)
            .setContentTitle("跑马灯运行中")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayView?.stopAnimation()
        handler?.removeCallbacks(hideRunnable)
        try { unregisterReceiver(receiver) } catch (_: Throwable) {}
    }

    companion object {
        const val ACTION_SHOW = "com.hyperos.marquee.SHOW"
        const val ACTION_PREVIEW = "com.hyperos.marquee.PREVIEW"
        const val ACTION_HIDE = "com.hyperos.marquee.HIDE"
    }
}
