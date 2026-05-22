#!/bin/bash
# 编译 Xposed API stub jar (离线，不需要 android.jar)
set -e

STUBS="/sdcard/HyperOS-Marquee/xposed-stubs"
OUT="$HOME/xposed-stubs"

echo "=== 编译 Xposed API stub ==="
rm -rf "$OUT"
mkdir -p "$OUT"

# 先编译无依赖的类
javac -source 17 -target 17 \
    -d "$OUT" \
    "$STUBS/de/robv/android/xposed/callbacks/XC_LoadPackage.java" \
    "$STUBS/de/robv/android/xposed/XC_MethodHook.java" \
    "$STUBS/de/robv/android/xposed/XposedHelpers.java" \
    2>&1

# 再编译依赖上面的类
javac -source 17 -target 17 \
    -classpath "$OUT" \
    -d "$OUT" \
    "$STUBS/de/robv/android/xposed/XposedBridge.java" \
    "$STUBS/de/robv/android/xposed/IXposedHookLoadPackage.java" \
    2>&1

# 打包
cd "$OUT"
jar cf "$HOME/xposed-api-82.jar" \
    de/robv/android/xposed/ \
    2>&1

if [ -f "$HOME/xposed-api-82.jar" ]; then
    echo "OK: ~/xposed-api-82.jar"
    ls -lh "$HOME/xposed-api-82.jar"
else
    echo "失败"
    exit 1
fi
