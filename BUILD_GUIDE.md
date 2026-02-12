# Android APK 编译指南

## 方法一：使用 Android Studio（最简单，推荐）

### 1. 安装 Android Studio

下载地址：https://developer.android.com/studio

- Windows: 下载 .exe 安装包
- macOS: 下载 .dmg 文件
- Linux: 下载 .tar.gz 文件

### 2. 打开项目

```
1. 启动 Android Studio
2. 选择 "Open" 或 "Open an Existing Project"
3. 导航到 RemoteTouch 文件夹
4. 点击 "OK"
```

### 3. 等待 Gradle 同步

首次打开项目时，Android Studio 会自动：
- 下载 Gradle Wrapper
- 下载项目依赖
- 配置 Android SDK

**这个过程可能需要 5-15 分钟，请耐心等待。**

### 4. 编译 APK

**方式A: 使用菜单**
```
Build → Build Bundle(s) / APK(s) → Build APK(s)
```

**方式B: 使用终端**
```bash
# 在 Android Studio 底部打开 Terminal
./gradlew assembleDebug
```

### 5. 查找生成的 APK

编译成功后，APK 位于：
```
RemoteTouch/app/build/outputs/apk/debug/app-debug.apk
```

---

## 方法二：命令行编译（适合高级用户）

### 前置要求

1. **安装 JDK 11 或更高版本**

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-11-jdk

# macOS (使用 Homebrew)
brew install openjdk@11

# Windows
# 下载 JDK: https://adoptium.net/
```

验证安装：
```bash
java -version
```

2. **下载 Android SDK 命令行工具**

下载地址：https://developer.android.com/studio#command-tools

```bash
# Linux 示例
cd ~
wget https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip
unzip commandlinetools-linux-9477386_latest.zip
mkdir -p Android/Sdk/cmdline-tools
mv cmdline-tools Android/Sdk/cmdline-tools/latest
```

3. **设置环境变量**

```bash
# Linux/macOS - 添加到 ~/.bashrc 或 ~/.zshrc
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
export PATH=$PATH:$ANDROID_HOME/platform-tools

# Windows - 系统环境变量
# ANDROID_HOME=C:\Users\YourName\AppData\Local\Android\Sdk
# Path 添加 %ANDROID_HOME%\cmdline-tools\latest\bin
```

4. **安装必要的 SDK 组件**

```bash
sdkmanager --install "platform-tools" "platforms;android-34" "build-tools;34.0.0"
```

### 编译步骤

```bash
# 1. 进入项目目录
cd RemoteTouch

# 2. 初始化 Gradle Wrapper（首次需要）
# 如果系统没有 gradle 命令，Android Studio 会自动生成 gradlew

# 3. 赋予执行权限（Linux/macOS）
chmod +x gradlew

# 4. 编译 Debug 版本
./gradlew assembleDebug

# 5. 编译 Release 版本（未签名）
./gradlew assembleRelease

# 6. 查看编译结果
ls -lh app/build/outputs/apk/debug/
```

---

## 方法三：在线编译服务（无需本地环境）

### GitHub Actions（需要 GitHub 账号）

1. 将项目上传到 GitHub
2. 创建 `.github/workflows/build.yml`:

```yaml
name: Android CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - name: Set up JDK 11
      uses: actions/setup-java@v3
      with:
        java-version: '11'
        distribution: 'temurin'
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    - name: Build with Gradle
      run: ./gradlew assembleDebug
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk
```

3. Push 代码，GitHub Actions 会自动编译
4. 在 Actions 页面下载编译好的 APK

---

## 常见问题

### Q1: Gradle 下载太慢

**解决方案：使用国内镜像**

编辑 `build.gradle`，修改仓库配置：

```gradle
allprojects {
    repositories {
        maven { url 'https://maven.aliyun.com/repository/google' }
        maven { url 'https://maven.aliyun.com/repository/public' }
        google()
        mavenCentral()
    }
}
```

### Q2: SDK 许可证未接受

```bash
# 接受所有许可证
yes | sdkmanager --licenses
```

### Q3: Gradle 版本不兼容

如果遇到版本问题，创建 `gradle/wrapper/gradle-wrapper.properties`:

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.0-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

### Q4: 内存不足

编辑 `gradle.properties`，增加堆内存：

```properties
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
```

### Q5: 编译成功但无法安装

```bash
# 检查签名
jarsigner -verify -verbose -certs app-debug.apk

# 重新签名（如果需要）
jarsigner -keystore ~/.android/debug.keystore app-debug.apk androiddebugkey
```

---

## 安装到手机

### 使用 ADB

```bash
# 1. 启用 USB 调试（手机设置）
# 设置 → 关于手机 → 连续点击 "版本号" 7次 → 返回 → 开发者选项 → USB调试

# 2. 连接手机到电脑
adb devices

# 3. 安装 APK
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 4. 如果安装失败，尝试卸载旧版本
adb uninstall com.example.remotetouch
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 使用文件传输

```
1. 将 app-debug.apk 复制到手机
2. 在手机上打开文件管理器
3. 找到 APK 文件并点击安装
4. 允许"未知来源"的安装（如果需要）
```

---

## 编译清单检查

在编译前，确保以下文件存在：

```
✅ RemoteTouch/
├── ✅ app/
│   ├── ✅ build.gradle
│   └── ✅ src/main/
│       ├── ✅ AndroidManifest.xml
│       ├── ✅ java/com/example/remotetouch/
│       │   ├── ✅ MainActivity.kt
│       │   ├── ✅ RemoteTouchForegroundService.kt
│       │   ├── ✅ TouchAccessibilityService.kt
│       │   ├── ✅ TouchSimulator.kt
│       │   └── ✅ WiFiTouchServer.kt
│       └── ✅ res/
│           ├── ✅ layout/activity_main.xml
│           ├── ✅ values/strings.xml
│           └── ✅ xml/accessibility_service_config.xml
├── ✅ build.gradle
├── ✅ settings.gradle
└── ✅ gradle.properties
```

所有文件都已创建完成！ ✅

---

## 预期编译时间

- **首次编译**: 10-20 分钟（下载依赖）
- **后续编译**: 1-3 分钟
- **增量编译**: 10-30 秒

---

## 成功标志

编译成功后会看到：

```
BUILD SUCCESSFUL in 2m 15s
45 actionable tasks: 45 executed
```

APK 文件大小约为 **2-3 MB**。
