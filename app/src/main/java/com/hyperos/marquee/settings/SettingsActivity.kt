package com.hyperos.marquee.settings

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.hyperos.marquee.R
import com.hyperos.marquee.config.SpConfig
import com.hyperos.marquee.service.EdgeLightingService

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val ctx = this
        val swEnabled = findViewById<Switch>(R.id.swEnabled)
        val rbAlways = findViewById<RadioButton>(R.id.rbAlways)
        val rbNotify = findViewById<RadioButton>(R.id.rbNotify)
        val seekDuration = findViewById<SeekBar>(R.id.seekDuration)
        val tvDur = findViewById<TextView>(R.id.tvDur)
        val seekW = findViewById<SeekBar>(R.id.seekW)
        val tvW = findViewById<TextView>(R.id.tvW)
        val seekH = findViewById<SeekBar>(R.id.seekH)
        val tvH = findViewById<TextView>(R.id.tvH)
        val seekOH = findViewById<SeekBar>(R.id.seekOH)
        val tvOH = findViewById<TextView>(R.id.tvOH)
        val seekOV = findViewById<SeekBar>(R.id.seekOV)
        val tvOV = findViewById<TextView>(R.id.tvOV)
        val spinnerStyle = findViewById<Spinner>(R.id.spinnerStyle)
        val seekLW = findViewById<SeekBar>(R.id.seekLW)
        val tvLW = findViewById<TextView>(R.id.tvLW)
        val seekSpeed = findViewById<SeekBar>(R.id.seekSpeed)
        val tvSpd = findViewById<TextView>(R.id.tvSpd)
        val swUnify = findViewById<Switch>(R.id.swUnify)
        val seekCTL = findViewById<SeekBar>(R.id.seekCTL)
        val tvCTL = findViewById<TextView>(R.id.tvCTL)
        val seekCTR = findViewById<SeekBar>(R.id.seekCTR)
        val tvCTR = findViewById<TextView>(R.id.tvCTR)
        val seekCBL = findViewById<SeekBar>(R.id.seekCBL)
        val tvCBL = findViewById<TextView>(R.id.tvCBL)
        val seekCBR = findViewById<SeekBar>(R.id.seekCBR)
        val tvCBR = findViewById<TextView>(R.id.tvCBR)

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

        val seekListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, from: Boolean) {
                when (s?.id) {
                    R.id.seekDuration -> tvDur.text = "${p}s"
                    R.id.seekW -> tvW.text = if (p == 0) "全屏" else "$p"
                    R.id.seekH -> tvH.text = if (p == 0) "全屏" else "$p"
                    R.id.seekOH -> tvOH.text = "${p - 300}"
                    R.id.seekOV -> tvOV.text = "${p - 300}"
                    R.id.seekLW -> tvLW.text = String.format("%.1fdp", p / 2f)
                    R.id.seekSpeed -> tvSpd.text = String.format("%.1f", p / 2f)
                    R.id.seekCTL -> tvCTL.text = "${p}dp"
                    R.id.seekCTR -> tvCTR.text = "${p}dp"
                    R.id.seekCBL -> tvCBL.text = "${p}dp"
                    R.id.seekCBR -> tvCBR.text = "${p}dp"
                }
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        }
        listOf(seekDuration, seekW, seekH, seekOH, seekOV, seekLW, seekSpeed,
            seekCTL, seekCTR, seekCBL, seekCBR).forEach { it.setOnSeekBarChangeListener(seekListener) }

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            val ed = SpConfig.prefs(ctx).edit()
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
            Toast.makeText(ctx, "配置已保存", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnPreview).setOnClickListener {
            val intent = Intent(ctx, EdgeLightingService::class.java)
            intent.action = EdgeLightingService.ACTION_SHOW
            intent.putExtra("package", "com.tencent.mm")
            intent.putExtra("text", "这是一条测试消息")
            startService(intent)
        }
    }
}
