package com.hyperos.marquee.view

import android.graphics.PointF

data class MarqueePath(val segments: List<Segment>, val totalLength: Float)

sealed class Segment(val length: Float) {
    class Line(val sx: Float, val sy: Float, val ex: Float, val ey: Float, len: Float) : Segment(len)
    class Arc(val cx: Float, val cy: Float, val r: Float, val startAngle: Float, len: Float) : Segment(len)
}

object PathCalculator {
    fun compute(w: Float, h: Float, tl: Float, tr: Float, br: Float, bl: Float): MarqueePath {
        val s = mutableListOf<Segment>()
        s.add(Segment.Line(tl, 0f, w - tr, 0f, w - tl - tr))
        s.add(Segment.Arc(w - tr, tr, tr, 270f, (Math.PI / 2 * tr).toFloat()))
        s.add(Segment.Line(w, tr, w, h - br, h - tr - br))
        s.add(Segment.Arc(w - br, h - br, br, 0f, (Math.PI / 2 * br).toFloat()))
        s.add(Segment.Line(w - br, h, bl, h, w - br - bl))
        s.add(Segment.Arc(bl, h - bl, bl, 90f, (Math.PI / 2 * bl).toFloat()))
        s.add(Segment.Line(0f, h - bl, 0f, tl, h - bl - tl))
        s.add(Segment.Arc(tl, tl, tl, 180f, (Math.PI / 2 * tl).toFloat()))
        val total = s.sumOf { it.length.toDouble() }.toFloat()
        return MarqueePath(s, total)
    }

    fun getPoint(mp: MarqueePath, t: Float): PointF {
        val target = t.coerceIn(0f, 1f) * mp.totalLength
        var acc = 0f
        for (seg in mp.segments) {
            if (acc + seg.length >= target) {
                val r = if (seg.length > 0) (target - acc) / seg.length else 0f
                return when (seg) {
                    is Segment.Line -> PointF(seg.sx + (seg.ex - seg.sx) * r, seg.sy + (seg.ey - seg.sy) * r)
                    is Segment.Arc -> {
                        val a = Math.toRadians((seg.startAngle + r * 90).toDouble())
                        PointF(seg.cx + seg.r * Math.cos(a).toFloat(), seg.cy + seg.r * Math.sin(a).toFloat())
                    }
                }
            }
            acc += seg.length
        }
        return PointF(0f, 0f)
    }
}
