#!/bin/bash
# 用 credential 方式推送（不把 token 放在 URL 里）
cd /sdcard/HyperOS-Marquee

# 用普通 URL
git remote set-url origin https://github.com/weigg0/HyperOS-Marquee.git

# 启用 credential 存储
git config --global credential.helper store

# 推送（会提示输入 Username 和 Password）
# Username: weigg0
# Password: 粘贴你的 Personal Access Token
git push -f origin main
