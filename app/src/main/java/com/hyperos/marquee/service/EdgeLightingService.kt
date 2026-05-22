package com.hyperos.marquee.service

import android.animation.ValueAnimator
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import com.hyperos.marquee.config.SpConfig
import com.hyperos.marquee.view.EdgeLightingView

class EdgeLightingService : Service() {
    companion object {
        const val ACTION_SHOW = "com.hyperos.marquee.SHOW"
        private const val CHANNEL_ID = "marquee_channel"
        private const val NOTIF_ID = 1001
    }

    private var windowManager: WindowManager? = null
    private var containerView: FrameLayout? = null
    private var lightingView: EdgeLightingView? = null
    private var hideAnimator: ValueAnimator? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_SHOW) {
            val pkg = intent.getStringExtra("package") ?: ""
            val text = intent.getStringExtra("text") ?: ""
            val duration = SpConfig.getDuration(this) * 1000L
            showLighting(duration)
        }
        return START_NOT_STICKY
    }

    private fun showLighting(duration: Long) {
        removeView()
        val ctx = this
        if (!SpConfig.isEnabled(ctx)) return

        containerView = FrameLayout(ctx)
        lightingView = EdgeLightingView(ctx).also { it.updateConfig() }

        val p = WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE
            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            gravity = Gravity.TOP or Gravity.START
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }

        containerView?.addView(lightingView)
        try {
            windowManager?.addView(containerView, p)
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }

        hideAnimator?.cancel()
        hideAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            this.duration = duration
            addUpdateListener { animation ->
                val v = animation.animatedValue as Float
                lightingView?.alpha = 1f - v
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    removeView()
                }
            })
            start()
        }
    }

    private fun removeView() {
        try {
            if (containerView?.isAttachedToWindow == true) {
                windowManager?.removeView(containerView)
            }
        } catch (_: Exception) {}
        containerView = null
        lightingView = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(CHANNEL_ID, "跑马灯", NotificationManager.IMPORTANCE_LOW)
            ch.setShowBadge(false)
            ch.setSound(null, null)
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(ch)
        }
    }

    private fun buildNotification(): Notification {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
                .setContentTitle("跑马灯运行中")
                .setContentText("来消息时显示边缘光效")
                .setOngoing(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
                .setContentTitle("跑马灯运行中")
                .setOngoing(true)
                .build()
        }
    }

    override fun onDestroy() {
        hideAnimator?.cancel()
        removeView()
        super.onDestroy()
    }
}
