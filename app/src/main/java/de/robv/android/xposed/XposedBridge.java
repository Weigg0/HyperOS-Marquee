package de.robv.android.xposed;
import java.lang.reflect.Member;
public class XposedBridge {
    public static void log(String text) {}
    public static void hookMethod(Member method, XC_MethodHook callback) {}
}
