package com.hyperos.marquee.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.hyperos.marquee.R
import com.hyperos.marquee.config.ConfigProvider
import com.hyperos.marquee.config.SpConfig
import com.hyperos.marquee.service.EdgeLightingService

class SettingsActivity : AppCompatActivity() {
    private lateinit var swEnabled: Switch
    private lateinit var rbAlways: RadioButton
    private lateinit var rbNotify: RadioButton
    private lateinit var seekDuration: SeekBar
    private lateinit var tvDur: TextView
    private lateinit var seekW: SeekBar
    private lateinit var tvW: TextView
    private lateinit var seekH: SeekBar
    private lateinit var tvH: TextView
    private lateinit var seekOH: SeekBar
    private lateinit var tvOH: TextView
    private lateinit var seekOV: SeekBar
    private lateinit var tvOV: TextView
    private lateinit var spinnerStyle: Spinner
    private lateinit var seekLW: SeekBar
    private lateinit var tvLW: TextView
    private lateinit var seekSpeed: SeekBar
    private lateinit var tvSpd: TextView
    private lateinit var swUnify: Switch
    private lateinit var seekCTL: SeekBar
    private lateinit var tvCTL: TextView
    private lateinit var seekCTR: SeekBar
    private lateinit var tvCTR: TextView
    private lateinit var seekCBL: SeekBar
    private lateinit var tvCBL: TextView
    private lateinit var seekCBR: SeekBar
    private lateinit var tvCBR: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        initViews()
        loadConfig()
        setupListeners()
    }

    private fun initViews() {
        swEnabled = findViewById(R.id.swEnabled)
        rbAlways = findViewById(R.id.rbAlways)
        rbNotify = findViewById(R.id.rbNotify)
        seekDuration = findViewById(R.id.seekDuration)
        tvDur = findViewById(R.id.tvDur)
        seekW = findViewById(R.id.seekW)
        tvW = findViewById(R.id.tvW)
        seekH = findViewById(R.id.seekH)
        tvH = findViewById(R.id.tvH)
        seekOH = findViewById(R.id.seekOH)
        tvOH = findViewById(R.id.tvOH)
        seekOV = findViewById(R.id.seekOV)
        tvOV = findViewById(R.id.tvOV)
        spinnerStyle = findViewById(R.id.spinnerStyle)
        seekLW = findViewById(R.id.seekLW)
        tvLW = findViewById(R.id.tvLW)
        seekSpeed = findViewById(R.id.seekSpeed)
        tvSpd = findViewById(R.id.tvSpd)
        swUnify = findViewById(R.id.swUnify)
        seekCTL = findViewById(R.id.seekCTL)
        tvCTL = findViewById(R.id.tvCTL)
        seekCTR = findViewById(R.id.seekCTR)
        tvCTR = findViewById(R.id.tvCTR)
        seekCBL = findViewById(R.id.seekCBL)
        tvCBL = findViewById(R.id.tvCBL)
        seekCBR = findViewById(R.id.seekCBR)
        tvCBR = findViewById(R.id.tvCBR)

        findViewById<Button>(R.id.btnSave).setOnClickListener { saveAndApply() }
        findViewById<Button>(R.id.btnPreview).setOnClickListener {
            startService(Intent(this, EdgeLightingService::class.java).apply {
                action = EdgeLightingService.ACTION_SHOW
                putExtra("package", "com.tencent.mm")
                putExtra("text", "这是一条测试消息")
            })
        }
    }

    private fun loadConfig() {
        val ctx = this
        swEnabled.isChecked = SpConfig.isEnabled(ctx)
        if (SpConfig.getDisplayMode(ctx) == 0) rbAlways.isChecked = true else rbNotify.isChecked = true
        seekDuration.progress = SpConfig.getDuration(ctx)
        tvDur.text = "${SpConfig.getDuration(ctx)}s"
        seekW.progress = SpConfig.getWidth(ctx)
        tvW.text = if (SpConfig.getWidth(ctx) == 0) "全屏" else "${SpConfig.getWidth(ctx)}"
        seekH.progress = SpConfig.getHeight(ctx)
        tvH.text = if (SpConfig.getHeight(ctx) == 0) "全屏" else "${SpConfig.getHeight(ctx)}"
        seekOH.progress = SpConfig.getOffsetH(ctx) + 300
        tvOH.text = "${SpConfig.getOffsetH(ctx)}"
        seekOV.progress = SpConfig.getOffsetV(ctx) + 300
        tvOV.text = "${SpConfig.getOffsetV(ctx)}"
        val styles = resources.getStringArray(R.array.style_values)
        spinnerStyle.setSelection(styles.indexOf(SpConfig.getStyle(ctx)).coerceAtLeast(0))
        seekLW.progress = (SpConfig.getLineWidth(ctx) * 2).toInt()
        tvLW.text = String.format("%.1fdp", SpConfig.getLineWidth(ctx))
        seekSpeed.progress = (SpConfig.getSpeed(ctx) * 2).toInt()
        tvSpd.text = String.format("%.1f", SpConfig.getSpeed(ctx))
        swUnify.isChecked = SpConfig.isUnifyCorner(ctx)
        seekCTL.progress = SpConfig.getCornerTL(ctx)
        tvCTL.text = "${SpConfig.getCornerTL(ctx)}dp"
        seekCTR.progress = SpConfig.getCornerTR(ctx)
        tvCTR.text = "${SpConfig.getCornerTR(ctx)}dp"
        seekCBL.progress = SpConfig.getCornerBL(ctx)
        tvCBL.text = "${SpConfig.getCornerBL(ctx)}dp"
        seekCBR.progress = SpConfig.getCornerBR(ctx)
        tvCBR.text = "${SpConfig.getCornerBR(ctx)}dp"
    }

    private fun setupListeners() {
        seekDuration.setOnSeekBarChangeListener(object : SimpleSeekBar() {
            override fun onProgressChanged(s: SeekBar?, p: Int, from: Boolean) {
                tvDur.text = "${p}s"
            }
        })
        seekW.setOnSeekBarChangeListener(object : SimpleSeekBar() {
            override fun onProgressChanged(s: SeekBar?, p: Int, from: Boolean) {
                tvW.text = if (p == 0) "全屏" else "$p"
            }
        })
        seekH.setOnSeekBarChangeListener(object : SimpleSeekBar() {
            override fun onProgressChanged(s: SeekBar?, p: Int, from: Boolean) {
                tvH.text = if (p == 0) "全屏" else "$p"
            }
        })
        seekOH.setOnSeekBarChangeListener(object : SimpleSeekBar() {
            override fun onProgressChanged(s: SeekBar?, p: Int, from: Boolean) {
                tvOH.text = "${p - 300}"
            }
        })
        seekOV.setOnSeekBarChangeListener(object : SimpleSeekBar() {
            override fun onProgressChanged(s: SeekBar?, p: Int, from: Boolean) {
                tvOV.text = "${p - 300}"
            }
        })
        seekLW.setOnSeekBarChangeListener(object : SimpleSeekBar() {
            override fun onProgressChanged(s: SeekBar?, p: Int, from: Boolean) {
                tvLW.text = String.format("%.1fdp", p / 2f)
            }
        })
        seekSpeed.setOnSeekBarChangeListener(object : SimpleSeekBar() {
            override fun onProgressChanged(s: SeekBar?, p: Int, from: Boolean) {
                tvSpd.text = String.format("%.1f", p / 2f)
            }
        })
        seekCTL.setOnSeekBarChangeListener(object : SimpleSeekBar() {
            override fun onProgressChanged(s: SeekBar?, p: Int, from: Boolean) { tvCTL.text = "${p}dp" }
        })
        seekCTR.setOnSeekBarChangeListener(object : SimpleSeekBar() {
            override fun onProgressChanged(s: SeekBar?, p: Int, from: Boolean) { tvCTR.text = "${p}dp" }
        })
        seekCBL.setOnSeekBarChangeListener(object : SimpleSeekBar() {
            override fun onProgressChanged(s: SeekBar?, p: Int, from: Boolean) { tvCBL.text = "${p}dp" }
        })
        seekCBR.setOnSeekBarChangeListener(object : SimpleSeekBar() {
            override fun onProgressChanged(s: SeekBar?, p: Int, from: Boolean) { tvCBR.text = "${p}dp" }
        })
    }

    private fun saveAndApply() {
        val ed = SpConfig.prefs(this).edit()
        ed.putBoolean(SpConfig.KEY_ENABLED, swEnabled.isChecked)
        ed.putInt(SpConfig.KEY_DISPLAY_MODE, if (rbAlways.isChecked) 0 else 1)
        ed.putInt(SpConfig.KEY_DURATION, seekDuration.progress)
        ed.putInt(SpConfig.KEY_WIDTH, seekW.progress)
        ed.putInt(SpConfig.KEY_HEIGHT, seekH.progress)
        ed.putInt(SpConfig.KEY_OFFSET_H, seekOH.progress - 300)
        ed.putInt(SpConfig.KEY_OFFSET_V, seekOV.progress - 300)
        ed.putString(SpConfig.KEY_STYLE, resources.getStringArray(R.array.style_values)[spinnerStyle.selectedItemPosition])
        ed.putFloat(SpConfig.KEY_LINE_WIDTH, seekLW.progress / 2f)
        ed.putFloat(SpConfig.KEY_SPEED, seekSpeed.progress / 2f)
        ed.putBoolean(SpConfig.KEY_UNIFY_CORNER, swUnify.isChecked)
        ed.putInt(SpConfig.KEY_CORNER_TL, seekCTL.progress)
        ed.putInt(SpConfig.KEY_CORNER_TR, seekCTR.progress)
        ed.putInt(SpConfig.KEY_CORNER_BL, seekCBL.progress)
        ed.putInt(SpConfig.KEY_CORNER_BR, seekCBR.progress)
        ed.apply()
        ConfigProvider.notifyChange(this)
        Toast.makeText(this, "配置已保存", Toast.LENGTH_SHORT).show()
    }

    private open class SimpleSeekBar : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(s: SeekBar?, p: Int, from: Boolean) {}
        override fun onStartTrackingTouch(s: SeekBar?) {}
        override fun onStopTrackingTouch(s: SeekBar?) {}
    }
}
