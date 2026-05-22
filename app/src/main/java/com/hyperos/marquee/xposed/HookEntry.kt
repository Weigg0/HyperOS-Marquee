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
                            val pkg: String = notification.packageName ?: return
                            val extras = notification.extras ?: return
                            val title: String = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
                            val text: String = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
                            val content: String = if (title.isNotEmpty() && text.isNotEmpty()) "$title: $text"
                                else if (text.isNotEmpty()) text else title
                            if (content.isNotEmpty()) {
                                val ctx: Context? = param.thisObject as? Context
                                sendShow(ctx, pkg, content)
                            }
                        } catch (e: Throwable) {
                            XposedBridge.log("$TAG xiaomi hook error: ${e.message}")
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
            val rankingMethod = entry?.javaClass?.getMethod("getRanking")
            val ranking = rankingMethod?.invoke(entry)
            val sbnMethod = ranking?.javaClass?.getMethod("getSbn")
            val sbn = sbnMethod?.invoke(ranking)
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
                        val sbn: StatusBarNotification = param.args[0] as StatusBarNotification
                        val pkg: String = sbn.packageName ?: return
                        val extras = sbn.notification?.extras ?: return
                        val title: String = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
                        val text: String = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
                        val content: String = if (title.isNotEmpty() && text.isNotEmpty()) "$title: $text"
                            else if (text.isNotEmpty()) text else title
                        if (content.isNotEmpty()) {
                            @Suppress("UNCHECKED_CAST")
                            val ctx: Context? = XposedHelpers.callMethod(sbn, "getContext") as? Context
                            sendShow(ctx, pkg, content)
                        }
                    } catch (e: Throwable) {
                        XposedBridge.log("$TAG aosp hook error: ${e.message}")
                    }
                }
            })
            XposedBridge.log("$TAG hooked AOSP NotificationListenerWrapper.onNotificationPosted")
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
                    XposedBridge.log("$TAG service.onStartCommand triggered (fallback)")
                }
            })
            XposedBridge.log("$TAG hooked NotificationListenerService.onStartCommand (fallback)")
            return true
        } catch (_: Throwable) { return false }
    }

    private fun sendShow(ctx: Context?, pkg: String, text: String) {
        if (ctx == null) return
        try {
            val intent = Intent(ACTION_SHOW)
            intent.setPackage("com.hyperos.marquee")
            intent.putExtra("package", pkg)
            intent.putExtra("text", text)
            ctx.sendBroadcast(intent)
        } catch (e: Throwable) {
            XposedBridge.log("$TAG send broadcast error: ${e.message}")
        }
    }
}
