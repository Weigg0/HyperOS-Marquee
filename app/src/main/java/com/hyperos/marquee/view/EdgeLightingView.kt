package com.hyperos.marquee.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.animation.ValueAnimator
import com.hyperos.marquee.config.SpConfig

class EdgeLightingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var mpath: MarqueePath? = null
    private var animT = 0f
    private var animator: ValueAnimator? = null
    private var duration = 3000L

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.OUTER)
    }

    fun updateConfig() {
        duration = SpConfig.getDuration(context) * 1000L
        paint.strokeWidth = SpConfig.getLineWidth(context)
        glowPaint.strokeWidth = SpConfig.getLineWidth(context) * 3
        requestLayout()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val ctx = context
        val unify = SpConfig.isUnifyCorner(ctx)
        val corner = SpConfig.getCornerTL(ctx).toFloat()
        val tl = if (unify) corner else SpConfig.getCornerTL(ctx).toFloat()
        val tr = if (unify) corner else SpConfig.getCornerTR(ctx).toFloat()
        val br = if (unify) corner else SpConfig.getCornerBR(ctx).toFloat()
        val bl = if (unify) corner else SpConfig.getCornerBL(ctx).toFloat()
        mpath = PathCalculator.compute(w.toFloat(), h.toFloat(), tl, tr, br, bl)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val mp = mpath ?: return
        val ctx = context
        val color = SpConfig.getColor(ctx)
        val style = SpConfig.getStyle(ctx)
        val speed = SpConfig.getSpeed(ctx)

        when (style) {
            "single_dot" -> drawSingleDot(canvas, mp, color, speed)
            "rainbow_fixed" -> drawRainbowFixed(canvas, mp, speed)
            "rainbow_cw" -> drawRainbowFlowing(canvas, mp, color, speed, 1f)
            "rainbow_ccw" -> drawRainbowFlowing(canvas, mp, color, speed, -1f)
            "star_trail" -> drawStarTrail(canvas, mp, color, speed)
            "dashed_flow" -> drawDashedFlow(canvas, mp, color, speed)
            "dot_matrix" -> drawDotMatrix(canvas, mp, color, speed)
            "ripple" -> drawRipple(canvas, mp, color, speed)
            "starlight" -> drawStarlight(canvas, mp, color, speed)
            "dual_color" -> drawDualColor(canvas, mp, color, speed)
            "dynamic_glow" -> drawDynamicGlow(canvas, mp, color, speed)
            else -> drawSingleDot(canvas, mp, color, speed)
        }
    }

    private fun drawSingleDot(canvas: Canvas, mp: MarqueePath, color: Int, speed: Float) {
        val pos = PathCalculator.getPoint(mp, animT)
        paint.style = Paint.Style.FILL
        paint.color = color
        glowPaint.color = color
        glowPaint.style = Paint.Style.FILL
        canvas.drawCircle(pos.x, pos.y, paint.strokeWidth * 2, glowPaint)
        canvas.drawCircle(pos.x, pos.y, paint.strokeWidth, paint)
        paint.style = Paint.Style.STROKE
    }

    private fun drawRainbowFixed(canvas: Canvas, mp: MarqueePath, speed: Float) {
        val colors = intArrayOf(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)
        drawColorSegments(canvas, mp, colors, animT, animT)
    }

    private fun drawRainbowFlowing(canvas: Canvas, mp: MarqueePath, color: Int, speed: Float, dir: Float) {
        val colors = intArrayOf(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)
        drawColorSegments(canvas, mp, colors, animT, animT + 0.3f)
    }

    private fun drawColorSegments(canvas: Canvas, mp: MarqueePath, colors: IntArray, from: Float, to: Float) {
        val segCount = 20
        for (i in 0 until segCount) {
            val t1 = (from + i.toFloat() / segCount) % 1f
            val t2 = (from + (i + 1).toFloat() / segCount) % 1f
            val p1 = PathCalculator.getPoint(mp, t1)
            val p2 = PathCalculator.getPoint(mp, t2)
            paint.color = colors[i % colors.size]
            paint.strokeWidth = SpConfig.getLineWidth(context)
            canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint)
        }
    }

    private fun drawStarTrail(canvas: Canvas, mp: MarqueePath, color: Int, speed: Float) {
        val trailLen = 0.15f
        for (i in 0..20) {
            val t = animT - trailLen * i / 20f
            val tWrapped = ((t % 1f) + 1f) % 1f
            val pos = PathCalculator.getPoint(mp, tWrapped)
            val alpha = (255 * (1f - i / 20f)).toInt()
            val size = paint.strokeWidth * (1f - i / 25f)
            paint.style = Paint.Style.FILL
            paint.color = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
            canvas.drawCircle(pos.x, pos.y, size, paint)
        }
        paint.style = Paint.Style.STROKE
    }

    private fun drawDashedFlow(canvas: Canvas, mp: MarqueePath, color: Int, speed: Float) {
        val dashCount = 30
        val dashLen = 0.02f
        for (i in 0 until dashCount) {
            val t1 = (animT + i.toFloat() / dashCount) % 1f
            val t2 = (animT + i.toFloat() / dashCount + dashLen) % 1f
            val p1 = PathCalculator.getPoint(mp, t1)
            val p2 = PathCalculator.getPoint(mp, t2)
            val alpha = 128 + (127 * ((i % 2).toFloat())).toInt()
            paint.color = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
            paint.strokeWidth = SpConfig.getLineWidth(context)
            canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint)
        }
    }

    private fun drawDotMatrix(canvas: Canvas, mp: MarqueePath, color: Int, speed: Float) {
        val dotCount = 40
        for (i in 0 until dotCount) {
            val t = (animT + i.toFloat() / dotCount) % 1f
            val pos = PathCalculator.getPoint(mp, t)
            val phase = ((animT * 10 + i) % 4) / 4f
            val size = SpConfig.getLineWidth(context) * (0.3f + 0.7f * Math.abs(Math.sin(Math.PI * phase)).toFloat())
            paint.style = Paint.Style.FILL
            paint.color = color
            canvas.drawCircle(pos.x, pos.y, size, paint)
        }
        paint.style = Paint.Style.STROKE
    }

    private fun drawRipple(canvas: Canvas, mp: MarqueePath, color: Int, speed: Float) {
        val rippleCount = 5
        for (i in 0 until rippleCount) {
            val phase = ((animT * rippleCount + i) % rippleCount) / rippleCount
            val pos = PathCalculator.getPoint(mp, phase)
            val radius = paint.strokeWidth * 5 * phase
            val alpha = (255 * (1f - phase)).toInt()
            paint.style = Paint.Style.STROKE
            paint.color = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
            paint.strokeWidth = 2f
            canvas.drawCircle(pos.x, pos.y, radius, paint)
        }
    }

    private fun drawStarlight(canvas: Canvas, mp: MarqueePath, color: Int, speed: Float) {
        val starCount = 8
        for (i in 0 until starCount) {
            val t = (animT * 3 + i * 0.127f) % 1f
            val pos = PathCalculator.getPoint(mp, t)
            val phase = Math.sin(Math.PI * 2 * animT * 2 + i).toFloat()
            val alpha = ((phase + 1) / 2 * 255).toInt()
            val size = SpConfig.getLineWidth(context) * (0.5f + phase * 0.5f)
            paint.style = Paint.Style.FILL
            paint.color = Color.argb(alpha.coerceIn(0, 255), Color.red(color), Color.green(color), Color.blue(color))
            canvas.drawCircle(pos.x, pos.y, size.coerceAtLeast(1f), paint)
        }
        paint.style = Paint.Style.STROKE
    }

    private fun drawDualColor(canvas: Canvas, mp: MarqueePath, color: Int, speed: Float) {
        val half = SpConfig.getLineWidth(context) * 0.5f
        for (i in 0..15) {
            val t1 = (animT + i * 0.01f) % 1f
            val t2 = ((1f - animT) + i * 0.01f) % 1f
            val p1 = PathCalculator.getPoint(mp, t1)
            val p2 = PathCalculator.getPoint(mp, t2)
            paint.style = Paint.Style.FILL
            paint.color = color
            canvas.drawCircle(p1.x, p1.y, half, paint)
            paint.color = invertColor(color)
            canvas.drawCircle(p2.x, p2.y, half, paint)
        }
        paint.style = Paint.Style.STROKE
    }

    private fun drawDynamicGlow(canvas: Canvas, mp: MarqueePath, color: Int, speed: Float) {
        val glow = (Math.sin(Math.PI * 2 * animT * 3).toFloat() + 1f) / 2f
        val alpha = (80 + 175 * glow).toInt()
        val width = SpConfig.getLineWidth(context) * (1f + glow)
        val path = android.graphics.Path()
        val first = PathCalculator.getPoint(mp, 0f)
        path.moveTo(first.x, first.y)
        for (i in 1..100) {
            val t = i / 100f
            val p = PathCalculator.getPoint(mp, t)
            path.lineTo(p.x, p.y)
        }
        path.close()
        glowPaint.color = Color.argb((alpha * 0.5f).toInt(), Color.red(color), Color.green(color), Color.blue(color))
        glowPaint.strokeWidth = width * 4
        canvas.drawPath(path, glowPaint)
        paint.color = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
        paint.strokeWidth = width
        canvas.drawPath(path, paint)
    }

    private fun invertColor(c: Int): Int = Color.rgb(255 - Color.red(c), 255 - Color.green(c), 255 - Color.blue(c))

    fun startAnimation() {
        stopAnimation()
        updateConfig()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2000L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                animT = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    fun stopAnimation() {
        animator?.cancel()
        animator = null
    }
}
