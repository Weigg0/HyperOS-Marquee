#!/bin/bash
# ============================================================
# HyperOS 跑马灯 — Termux 一键编译脚本
# 在 Termux 中执行: bash /sdcard/HyperOS-Marquee/build.sh
# ============================================================

set -e

echo "=== [1/5] 安装编译依赖 ==="
pkg update -y
pkg install -y openjdk-17 wget unzip

echo "=== [2/5] 下载 Android SDK 命令行工具 ==="
export ANDROID_HOME="$HOME/android-sdk"
mkdir -p "$ANDROID_HOME/cmdline-tools"

if [ ! -f "$ANDROID_HOME/cmdline-tools/bin/sdkmanager" ]; then
    # 下载最新命令行工具 (Mac/Linux 版，包含 aapt2 等)
    cd /tmp
    wget -q "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip" -O cmdtools.zip
    unzip -qo cmdtools.zip -d "$ANDROID_HOME/cmdline-tools/"
    mv "$ANDROID_HOME/cmdline-tools/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest"
    echo "命令行工具下载完成"
else
    echo "命令行工具已存在，跳过"
fi

echo "=== [3/5] 安装 Android SDK 组件 ==="
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"
yes | sdkmanager --licenses > /dev/null 2>&1 || true
sdkmanager "platforms;android-35" "build-tools;35.0.0" > /dev/null 2>&1

echo "=== [4/5] 检查 aapt2 可用性 ==="
AAPT2=$(find "$ANDROID_HOME" -name "aapt2" -type f 2>/dev/null | head -1)
if [ -z "$AAPT2" ]; then
    echo "aapt2 未找到，尝试使用 sdkmanager 安装..."
    sdkmanager "build-tools;35.0.0" --verbose
    AAPT2=$(find "$ANDROID_HOME" -name "aapt2" -type f 2>/dev/null | head -1)
fi

if [ -n "$AAPT2" ]; then
    chmod +x "$AAPT2"
    echo "aapt2 路径: $AAPT2"
    file "$AAPT2"
else
    echo "错误: 找不到 aapt2，请检查 Android SDK 安装"
    echo "可以尝试: sdkmanager --list 查看可用组件"
    exit 1
fi

echo "=== [5/5] 编译项目 ==="
cd /sdcard/HyperOS-Marquee

# 设置 Gradle 使用本地 SDK
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export ANDROID_HOME

# 配置 local.properties
echo "sdk.dir=$ANDROID_HOME" > local.properties

# 编译
chmod +x gradlew
./gradlew assembleDebug --no-daemon

# 查找输出
APK=$(find app/build/outputs/apk/debug/ -name "*.apk" 2>/dev/null | head -1)
if [ -n "$APK" ]; then
    echo ""
    echo "=========================================="
    echo "编译成功!"
    echo "APK 路径: $APK"
    echo "复制到下载目录..."
    cp "$APK" /sdcard/Download/HyperOS-Marquee-debug.apk
    echo "已复制到: /sdcard/Download/HyperOS-Marquee-debug.apk"
    echo "=========================================="
else
    echo "编译失败，请检查上方错误信息"
    exit 1
fi
