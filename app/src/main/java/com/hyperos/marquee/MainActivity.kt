package com.hyperos.marquee

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.app.Activity
import com.hyperos.marquee.service.EdgeLightingService

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        val title = TextView(this).apply {
            text = "跑马灯模块"
            textSize = 24f
            setPadding(0, 0, 0, 32)
        }
        layout.addView(title)

        val status = TextView(this).apply {
            text = "模块已安装，请在 LSPosed 中启用并勾选 SystemUI\n重启手机后生效"
            textSize = 16f
            setPadding(0, 0, 0, 32)
        }
        layout.addView(status)

        val swEnabled = Switch(this).apply {
            text = "启用跑马灯"
            isChecked = true
            textSize = 18f
            setPadding(0, 16, 0, 16)
        }
        layout.addView(swEnabled)

        val btnStop = Button(this).apply {
            text = "停止跑马灯服务"
            setOnClickListener {
                stopService(Intent(this@MainActivity, EdgeLightingService::class.java))
                status.text = "服务已停止"
            }
        }
        layout.addView(btnStop)

        val info = TextView(this).apply {
            text = "\n功能: 来消息时屏幕边缘显示跑马灯光效\n版本: 1.0.0\n适配: HyperOS 3 (Android 16)"
            textSize = 14f
            setPadding(0, 32, 0, 0)
        }
        layout.addView(info)

        setContentView(layout)
    }
}
