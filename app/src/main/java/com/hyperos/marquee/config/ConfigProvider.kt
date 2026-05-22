package com.hyperos.marquee.config

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle

class ConfigProvider : ContentProvider() {
    override fun onCreate(): Boolean = true
    override fun call(method: String, arg: String?, extras: Bundle?): Bundle {
        val ctx = context ?: return Bundle()
        val result = Bundle()
        val p = SpConfig.prefs(ctx)
        when (method) {
            "getAll" -> {
                val b = Bundle()
                p.all.forEach { (k, v) ->
                    when (v) {
                        is Boolean -> b.putBoolean(k, v)
                        is Int -> b.putInt(k, v)
                        is Float -> b.putFloat(k, v)
                        is String -> b.putString(k, v)
                    }
                }
                result.putBundle("prefs", b)
            }
        }
        return result
    }
    override fun query(u: Uri, p: Array<String>?, s: String?, a: Array<String>?, o: String?): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, s: String?, a: Array<String>?) = 0
    override fun update(uri: Uri, values: ContentValues?, s: String?, a: Array<String>?) = 0
}
