package com.hyperos.marquee.xposed

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.service.notification.StatusBarNotification
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HookEntry : IXposedHookLoadPackage {

    companion object {
        private const val TAG = "[Marquee]"
        private const val PKG_SYSTEMUI = "com.android.systemui"
        private const val ACTION_SHOW = "com.hyperos.marquee.SHOW"
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != PKG_SYSTEMUI) return
        val classLoader = lpparam.classLoader
        var hooked = false
        hooked = tryHookXiaomi(classLoader)
        if (!hooked) hooked = tryHookAosp(classLoader)
        if (!hooked) hooked = tryHookServiceCommand(classLoader)
        XposedBridge.log("$TAG init done, hooked=$hooked")
    }

    private fun tryHookXiaomi(classLoader: ClassLoader): Boolean {
        val candidates = listOf(
            "com.android.systemui.statusbar.notification.collection.coordinator.MiuiNotifCoordinator",
            "com.android.systemui.statusbar.notification.collection.MiuiNotifCoordinator",
            "com.android.systemui.statusbar.notification.collection.MiuiNotifCollection"
        )
        for (clsName in candidates) {
            try {
                val cls = XposedHelpers.findClass(clsName, classLoader) ?: continue
                val method = findEntryMethod(cls) ?: continue
                XposedBridge.hookMethod(method, object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            val entry: Any = param.args[0]
                            val notification: StatusBarNotification = getNotifFromEntry(entry) ?: return
                            processNotification(param.thisObject as? Context, notification)
                        } catch (e: Throwable) {
                            XposedBridge.log("$TAG xiaomi error: ${e.message}")
                        }
                    }
                })
                XposedBridge.log("$TAG hooked $clsName.${method.name}")
                return true
            } catch (_: Throwable) { }
        }
        return false
    }

    private fun findEntryMethod(cls: Class<*>): java.lang.reflect.Method? {
        for (name in listOf("onEntryAdded", "addEntry")) {
            for (m in cls.declaredMethods) {
                if (m.name == name && m.parameterTypes.size == 1) {
                    m.isAccessible = true
                    return m
                }
            }
        }
        return null
    }

    private fun getNotifFromEntry(entry: Any?): StatusBarNotification? {
        try {
            val ranking = entry?.javaClass?.getMethod("getRanking")?.invoke(entry)
            val sbn = ranking?.javaClass?.getMethod("getSbn")?.invoke(ranking)
            if (sbn is StatusBarNotification) return sbn
        } catch (_: Throwable) { }
        try {
            val field = entry?.javaClass?.getDeclaredField("mSbn")
            field?.isAccessible = true
            val sbn = field?.get(entry)
            if (sbn is StatusBarNotification) return sbn
        } catch (_: Throwable) { }
        return null
    }

    private fun tryHookAosp(classLoader: ClassLoader): Boolean {
        try {
            val wrapperCls = XposedHelpers.findClass(
                "android.service.notification.NotificationListenerService\$NotificationListenerWrapper",
                classLoader
            )
            val method = XposedHelpers.findMethodExact(
                wrapperCls, "onNotificationPosted",
                StatusBarNotification::class.java
            )
            XposedBridge.hookMethod(method, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    try {
                        val sbn = param.args[0] as StatusBarNotification
                        processNotification(null, sbn)
                    } catch (e: Throwable) {
                        XposedBridge.log("$TAG aosp error: ${e.message}")
                    }
                }
            })
            XposedBridge.log("$TAG hooked AOSP NotificationListenerWrapper")
            return true
        } catch (_: Throwable) { return false }
    }

    private fun tryHookServiceCommand(classLoader: ClassLoader): Boolean {
        try {
            val cls = XposedHelpers.findClass(
                "android.service.notification.NotificationListenerService", classLoader
            )
            val method = XposedHelpers.findMethodExact(
                cls, "onStartCommand",
                Intent::class.java,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType
            )
            XposedBridge.hookMethod(method, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    XposedBridge.log("$TAG service.onStartCommand (fallback)")
                }
            })
            XposedBridge.log("$TAG hooked NotificationListenerService.onStartCommand")
            return true
        } catch (_: Throwable) { return false }
    }

    private fun processNotification(ctx: Context?, sbn: StatusBarNotification) {
        val pkg = sbn.packageName ?: return
        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val content = when {
            title.isNotEmpty() && text.isNotEmpty() -> "$title: $text"
            text.isNotEmpty() -> text
            title.isNotEmpty() -> title
            else -> return
        }
        val context = ctx ?: sbn.context
        try {
            val intent = Intent(ACTION_SHOW)
            intent.setPackage("com.hyperos.marquee")
            intent.putExtra("package", pkg)
            intent.putExtra("text", content)
            context.sendBroadcast(intent)
        } catch (e: Throwable) {
            XposedBridge.log("$TAG broadcast error: ${e.message}")
        }
    }
}
