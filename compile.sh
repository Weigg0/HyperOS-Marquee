#!/bin/bash
# ============================================================
# HyperOS 跑马灯 — Termux 编译脚本 v3 (修复 /tmp)
# 用法: bash /sdcard/HyperOS-Marquee/compile.sh
# ============================================================

set -e

PROJECT="/sdcard/HyperOS-Marquee"
BUILD="$PROJECT/build_out"
SRC="$PROJECT/app/src/main"
RES="$SRC/res"
MANIFEST="$SRC/AndroidManifest.xml"
ASSETS="$SRC/assets"
JAVA_SRC="$SRC/java"
ANDROID_JAR="$HOME/android.jar"

echo "=========================================="
echo "  HyperOS 跑马灯 APK 编译 v3"
echo "=========================================="

# === [0] 检查环境 ===
echo ""
echo "[0/7] 检查环境..."
MISSING=""
for cmd in javac kotlinc aapt apksigner keytool zip d8; do
    if ! command -v "$cmd" &>/dev/null; then
        MISSING="$MISSING $cmd"
    fi
done
if [ -n "$MISSING" ]; then
    echo "错误: 缺少:$MISSING"
    echo "安装: pkg install openjdk-17 kotlin aapt apksigner zip d8"
    exit 1
fi
echo "  所有工具: OK"

# 下载 android.jar (国内镜像优先)
if [ ! -f "$ANDROID_JAR" ]; then
    echo "  [外网] 下载 android.jar (GitHub)..."
    wget -q --timeout=30 "https://mirror.ghproxy.com/https://github.com/niccokunzmann/AOSP-API/raw/master/android-34/android.jar" \
        -O "$ANDROID_JAR" 2>/dev/null || \
    wget -q --timeout=60 "https://github.com/niccokunzmann/AOSP-API/raw/master/android-34/android.jar" \
        -O "$ANDROID_JAR" 2>/dev/null || {
        echo "错误: 无法下载 android.jar"
        exit 1
    }
fi
echo "  android.jar: OK"

# === [1] 清理 ===
echo ""
echo "[1/7] 清理旧构建..."
rm -rf "$BUILD"
mkdir -p "$BUILD/gen" "$BUILD/obj" "$BUILD/dex"

# === [2] 编译资源 ===
echo ""
echo "[2/7] 编译资源文件..."
aapt package -f -m \
    -S "$RES" \
    -J "$BUILD/gen" \
    -M "$MANIFEST" \
    -I "$ANDROID_JAR" \
    --auto-add-overlay 2>&1 | head -5
echo "  资源编译完成"

# === [3] 编译 Kotlin ===
echo ""
echo "[3/7] 编译 Kotlin 源码..."
find "$JAVA_SRC" -name "*.kt" > "$BUILD/src_list.txt"
find "$BUILD/gen" -name "*.java" >> "$BUILD/src_list.txt"

XPOSED_JAR=""
if [ -f "$HOME/xposed-api-82.jar" ]; then
    XPOSED_JAR="$HOME/xposed-api-82.jar"
fi

CP="$ANDROID_JAR"
if [ -n "$XPOSED_JAR" ]; then
    CP="$CP:$XPOSED_JAR"
    echo "  Xposed API: OK"
else
    echo "  警告: Xposed API stub 未找到，编译可能失败"
    echo "  请先运行: bash /sdcard/HyperOS-Marquee/build_xposed_stub.sh"
fi

echo "  源文件数: $(wc -l < "$BUILD/src_list.txt")"
kotlinc \
    $(cat "$BUILD/src_list.txt" | tr '\n' ' ') \
    -classpath "$CP" \
    -jvm-target 17 \
    -d "$BUILD/obj" \
    2>&1 | tail -15

CLASS_COUNT=$(find "$BUILD/obj" -name "*.class" 2>/dev/null | wc -l)
echo "  编译完成，生成 $CLASS_COUNT 个 .class 文件"

if [ "$CLASS_COUNT" -eq 0 ]; then
    echo "错误: 没有生成 .class 文件"
    exit 1
fi

# === [4] 生成 DEX ===
echo ""
echo "[4/7] 生成 DEX..."
d8 --min-api 31 \
   --lib "$ANDROID_JAR" \
   --output "$BUILD/dex" \
   $(find "$BUILD/obj" -name "*.class" | tr '\n' ' ') \
   2>&1 | tail -5

if [ ! -f "$BUILD/dex/classes.dex" ]; then
    echo "错误: classes.dex 未生成"
    exit 1
fi
echo "  DEX 生成完成 ($(ls -lh "$BUILD/dex/classes.dex" | awk '{print $5}'))"

# === [5] 组装 APK ===
echo ""
echo "[5/7] 组装 APK..."
aapt package -f \
    -S "$RES" \
    -M "$MANIFEST" \
    -I "$ANDROID_JAR" \
    -F "$BUILD/app.unsigned.apk" \
    --auto-add-overlay 2>&1 | head -5

cd "$BUILD/dex"
zip -j "$BUILD/app.unsigned.apk" classes.dex
cd "$PROJECT"

if [ -d "$ASSETS" ] && [ "$(ls -A "$ASSETS" 2>/dev/null)" ]; then
    cd "$ASSETS"
    zip -r "$BUILD/app.unsigned.apk" .
    cd "$PROJECT"
fi
echo "  APK 组装完成"

# === [6] 签名 ===
echo ""
echo "[6/7] 签名 APK..."
KEYSTORE="$HOME/.android/debug.keystore"
if [ ! -f "$KEYSTORE" ]; then
    mkdir -p "$HOME/.android"
    keytool -genkeypair -v \
        -keystore "$KEYSTORE" \
        -alias debug \
        -keyalg RSA \
        -keysize 2048 \
        -validity 10000 \
        -storepass android \
        -keypass android \
        -dname "CN=Debug,O=Debug,C=US" 2>/dev/null
fi

if command -v zipalign &>/dev/null; then
    zipalign -f 4 "$BUILD/app.unsigned.apk" "$BUILD/app.aligned.apk" 2>/dev/null && \
    mv "$BUILD/app.aligned.apk" "$BUILD/app.unsigned.apk"
fi

apksigner sign \
    --ks "$KEYSTORE" \
    --ks-pass pass:android \
    --key-pass pass:android \
    --ks-key-alias debug \
    --out "$BUILD/HyperOS-Marquee.apk" \
    "$BUILD/app.unsigned.apk" 2>&1 | tail -3

# === 完成 ===
echo ""
echo "=========================================="
if [ -f "$BUILD/HyperOS-Marquee.apk" ]; then
    SIZE=$(ls -lh "$BUILD/HyperOS-Marquee.apk" | awk '{print $5}')
    echo "  编译成功! APK 大小: $SIZE"
    cp "$BUILD/HyperOS-Marquee.apk" /sdcard/Download/HyperOS-Marquee.apk
    echo "  已复制到: /sdcard/Download/HyperOS-Marquee.apk"
    echo ""
    echo "  安装步骤:"
    echo "  1. su -c 'pm install /sdcard/Download/HyperOS-Marquee.apk'"
    echo "  2. 打开 LSPosed → 模块 → 启用「跑马灯」"
    echo "  3. 勾选作用域: SystemUI"
    echo "  4. 重启手机"
else
    echo "  编译失败，请检查上方错误信息"
fi
echo "=========================================="
