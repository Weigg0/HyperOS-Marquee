#!/data/data/com.termux/files/usr/bin/bash
set -e
GRADLE_USER_HOME=${GRADLE_USER_HOME:-$HOME/.gradle}
DIST_DIR="$GRADLE_USER_HOME/wrapper/dists/gradle-8.11.1-bin"

if [ ! -d "$DIST_DIR" ]; then
    echo "下载Gradle 8.11.1..."
    mkdir -p "$DIST_DIR"
    cd "$DIST_DIR"
    wget -q "https://services.gradle.org/distributions/gradle-8.11.1-bin.zip" -O gradle.zip
    unzip -qo gradle.zip
    touch "$DIST_DIR/downloaded"
    cd -
fi

GRADLE_BIN=$(find "$DIST_DIR" -name "gradle" -type f -path "*/bin/*" 2>/dev/null | head -1)
if [ -z "$GRADLE_BIN" ]; then
    echo "错误: 找不到gradle二进制文件"
    exit 1
fi
exec "$GRADLE_BIN" "$@"
