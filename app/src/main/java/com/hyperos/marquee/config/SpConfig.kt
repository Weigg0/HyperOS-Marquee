package com.hyperos.marquee.config

import android.content.Context
import android.content.SharedPreferences

object SpConfig {
    private const val PREF_NAME = "marquee_config"
    const val KEY_ENABLED = "enabled"
    const val KEY_DISPLAY_MODE = "display_mode"
    const val KEY_DURATION = "duration"
    const val KEY_WIDTH = "width"
    const val KEY_HEIGHT = "height"
    const val KEY_OFFSET_H = "offset_h"
    const val KEY_OFFSET_V = "offset_v"
    const val KEY_STYLE = "style"
    const val KEY_LINE_WIDTH = "line_width"
    const val KEY_SPEED = "speed"
    const val KEY_COLOR = "color"
    const val KEY_UNIFY_CORNER = "unify_corner"
    const val KEY_CORNER_TL = "corner_tl"
    const val KEY_CORNER_TR = "corner_tr"
    const val KEY_CORNER_BL = "corner_bl"
    const val KEY_CORNER_BR = "corner_br"

    fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_WORLD_READABLE)

    fun isEnabled(ctx: Context) = prefs(ctx).getBoolean(KEY_ENABLED, true)
    fun getDisplayMode(ctx: Context) = prefs(ctx).getInt(KEY_DISPLAY_MODE, 1)
    fun getDuration(ctx: Context) = prefs(ctx).getInt(KEY_DURATION, 3)
    fun getWidth(ctx: Context) = prefs(ctx).getInt(KEY_WIDTH, 0)
    fun getHeight(ctx: Context) = prefs(ctx).getInt(KEY_HEIGHT, 0)
    fun getOffsetH(ctx: Context) = prefs(ctx).getInt(KEY_OFFSET_H, 0)
    fun getOffsetV(ctx: Context) = prefs(ctx).getInt(KEY_OFFSET_V, 0)
    fun getStyle(ctx: Context) = prefs(ctx).getString(KEY_STYLE, "single_dot") ?: "single_dot"
    fun getLineWidth(ctx: Context) = prefs(ctx).getFloat(KEY_LINE_WIDTH, 4f)
    fun getSpeed(ctx: Context) = prefs(ctx).getFloat(KEY_SPEED, 5f)
    fun getColor(ctx: Context) = prefs(ctx).getInt(KEY_COLOR, -0xFFE890)
    fun isUnifyCorner(ctx: Context) = prefs(ctx).getBoolean(KEY_UNIFY_CORNER, true)
    fun getCornerTL(ctx: Context) = prefs(ctx).getInt(KEY_CORNER_TL, 59)
    fun getCornerTR(ctx: Context) = prefs(ctx).getInt(KEY_CORNER_TR, 59)
    fun getCornerBL(ctx: Context) = prefs(ctx).getInt(KEY_CORNER_BL, 59)
    fun getCornerBR(ctx: Context) = prefs(ctx).getInt(KEY_CORNER_BR, 59)
}
