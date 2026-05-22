#!/bin/bash
# ============================================================
# 推送到 GitHub 并触发 Actions 编译
# 在 Termux 中执行
# ============================================================

echo "=== 推送 HyperOS-Marquee 到 GitHub ==="
echo ""
echo "步骤 1: 在 GitHub 上创建仓库"
echo "  打开 GitHub App → 点 + → New repository"
echo "  仓库名: HyperOS-Marquee"
echo "  选 Public（免费 Actions 限制）"
echo "  不要勾选 README/gitignore（我们已经有了）"
echo "  记下你的用户名，比如 myusername"
echo ""

read -p "你的 GitHub 用户名: " GITHUB_USER
if [ -z "$GITHUB_USER" ]; then
    echo "未输入用户名，退出"
    exit 1
fi

cd /sdcard/HyperOS-Marquee

# 初始化 git
git init
git add -A
git commit -m "feat: HyperOS 3 跑马灯模块 v1.0"

# 设置远程仓库
git branch -M main
git remote add origin "https://github.com/$GITHUB_USER/HyperOS-Marquee.git"
git push -f origin main

echo ""
echo "=========================================="
echo "推送完成!"
echo ""
echo "接下来:"
echo "1. 打开 GitHub App → HyperOS-Marquee 仓库"
echo "2. 点 Actions 标签 → 等待编译完成（约3-5分钟）"
echo "3. 编译成功后点进 workflow → 下载 APK artifact"
echo "4. 安装 APK:"
echo "   su -c 'pm install 下载的APK路径'"
echo "5. LSPosed 启用模块 → 勾选 SystemUI → 重启"
echo "=========================================="
