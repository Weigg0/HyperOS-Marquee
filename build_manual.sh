#!/bin/bash
# ============================================================
# HyperOS 跑马灯 — 纯手动编译 (无需 Gradle)
# 在 Termux 中执行:
#   pkg install openjdk-17 aapt d8 apksigner
#   bash /sdcard/HyperOS-Marquee/build_manual.sh
# ============================================================

set -e
cd /sdcard/HyperOS-Marquee

echo "=== [1/6] 清理旧构建 ==="
rm -rf build_manual
mkdir -p build_manual/gen build_manual/obj build_manual/dex build_manual/apk

echo "=== [2/6] 编译资源 (aapt) ==="
# 找 aapt 路径
AAPT=$(which aapt 2>/dev/null || echo "")
if [ -z "$AAPT" ]; then
    echo "错误: 未找到 aapt，请先运行: pkg install aapt"
    exit 1
fi
echo "aapt 路径: $AAPT"

# 找 android.jar (用于编译)
ANDROID_JAR=""
for sdk in "$HOME/android-sdk" "/usr/local/android-sdk" "$ANDROID_HOME"; do
    if [ -d "$sdk/platforms" ]; then
        ANDROID_JAR=$(find "$sdk/platforms" -name "android.jar" -type f 2>/dev/null | sort -V | tail -1)
        if [ -n "$ANDROID_JAR" ]; then break; fi
    fi
done

if [ -z "$ANDROID_JAR" ]; then
    echo "未找到 Android SDK，尝试下载 android.jar..."
    # 使用 API 34 的 android.jar (最小兼容)
    mkdir -p /tmp/android-jar
    cd /tmp/android-jar
    wget -q "https://raw.githubusercontent.com/niccokunzmann/AOSP-API/master/android-34/android.jar" -O android.jar 2>/dev/null || \
    wget -q "https://github.com/niccokunzmann/AOSP-API/raw/master/android-34/android.jar" -O android.jar 2>/dev/null
    if [ -f "android.jar" ]; then
        ANDROID_JAR="/tmp/android-jar/android.jar"
        echo "android.jar 下载成功: $ANDROID_JAR"
    else
        echo "错误: 无法获取 android.jar"
        echo "请安装 Android SDK: pkg install android-sdk"
        exit 1
    fi
    cd /sdcard/HyperOS-Marquee
fi
echo "android.jar: $ANDROID_JAR"

$AAPT package -f -m \
    -S app/src/main/res \
    -J build_manual/gen \
    -M app/src/main/AndroidManifest.xml \
    -I "$ANDROID_JAR" \
    --auto-add-overlay \
    2>&1 | head -20

echo "=== [3/6] 编译 Kotlin/Java 源码 ==="
# 收集所有源文件
find app/src/main/java -name "*.kt" -o -name "*.java" > build_manual/sources.txt
find build_manual/gen -name "*.java" >> build_manual/sources.txt

# 检查 kotlinc
if command -v kotlinc &>/dev/null; then
    echo "使用 kotlinc 编译..."
    kotlinc $(cat build_manual/sources.txt | tr '\n' ' ') \
        -classpath "$ANDROID_JAR" \
        -jvm-target 17 \
        -d build_manual/obj \
        2>&1 | tail -10
else
    echo "错误: 未找到 kotlinc"
    echo "请先运行: pkg install kotlin"
    exit 1
fi

echo "=== [4/6] 转换为 DEX ==="
# 找 d8
D8=$(which d8 2>/dev/null || echo "")
if [ -z "$D8" ]; then
    echo "错误: 未找到 d8，请先运行: pkg install d8"
    exit 1
fi

# 收集 .class 文件
find build_manual/obj -name "*.class" > build_manual/classes.txt
if [ ! -s build_manual/classes.txt ]; then
    echo "错误: 没有找到 .class 文件，编译可能失败"
    exit 1
fi

$D8 --min-api 31 --output build_manual/dex $(cat build_manual/classes.txt | tr '\n' ' ') \
    2>&1 | tail -5

echo "=== [5/6] 打包 APK ==="
# 复制资源到 apk 目录
$AAPT package -f \
    -S app/src/main/res \
    -M app/src/main/AndroidManifest.xml \
    -I "$ANDROID_JAR" \
    -F build_manual/app.unsigned.apk \
    --auto-add-overlay \
    2>&1 | head -10

# 添加 dex
cd build_manual/dex
zip -j ../app.unsigned.apk classes.dex
cd /sdcard/HyperOS-Marquee

# 添加 assets
if [ -d "app/src/main/assets" ]; then
    cd app/src/main/assets
    zip -r /sdcard/HyperOS-Marquee/build_manual/app.unsigned.apk .
    cd /sdcard/HyperOS-Marquee
fi

# 添加签名信息
$AAPT add build_manual/app.unsigned.apk app/src/main/assets/xposed_init 2>/dev/null || true

echo "=== [6/6] 签名 APK ==="
# 生成密钥（如果不存在）
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
        -dname "CN=Debug,O=Debug,C=US" \
        2>/dev/null
    echo "debug.keystore 已生成"
fi

# 签名
apksigner sign \
    --ks "$KEYSTORE" \
    --ks-pass pass:android \
    --key-pass pass:android \
    --ks-key-alias debug \
    --out build_manual/HyperOS-Marquee.apk \
    build_manual/app.unsigned.apk

echo ""
echo "=========================================="
echo "编译成功!"
APK_PATH="build_manual/HyperOS-Marquee.apk"
ls -lh "$APK_PATH"
cp "$APK_PATH" /sdcard/Download/HyperOS-Marquee.apk
echo "APK 已复制到: /sdcard/Download/HyperOS-Marquee.apk"
echo "=========================================="
