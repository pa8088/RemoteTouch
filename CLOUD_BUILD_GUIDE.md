# ☁️ GitHub Actions 云编译指南

## 🎯 优势

✅ **无需本地环境** - 不需要安装 Android Studio 或 JDK
✅ **自动编译** - 代码推送后自动构建 APK
✅ **永久下载** - APK 文件自动发布到 Release
✅ **完全免费** - GitHub Actions 对公开仓库免费

---

## 📋 使用步骤

### 步骤1: 创建 GitHub 仓库

1. 访问 https://github.com
2. 点击右上角 `+` → `New repository`
3. 填写仓库信息:
   - Repository name: `RemoteTouch`（或其他名称）
   - Description: `远程触摸控制 Android 应用`
   - 选择 `Public`（公开仓库）
4. 点击 `Create repository`

### 步骤2: 上传项目代码

**方法A: 使用 GitHub 网页上传（最简单）**

```bash
1. 在 GitHub 仓库页面点击 "uploading an existing file"
2. 将整个 RemoteTouch 文件夹拖拽到网页
3. 等待上传完成
4. 填写 Commit 信息: "Initial commit"
5. 点击 "Commit changes"
```

**方法B: 使用 Git 命令行**

```bash
# 1. 初始化 Git 仓库
cd RemoteTouch
git init

# 2. 添加所有文件
git add .

# 3. 提交
git commit -m "Initial commit"

# 4. 关联远程仓库（替换为你的仓库地址）
git remote add origin https://github.com/your-username/RemoteTouch.git

# 5. 推送到 GitHub
git branch -M main
git push -u origin main
```

### 步骤3: 触发自动编译

代码推送后，GitHub Actions 会**自动开始编译**：

1. 进入仓库页面
2. 点击 `Actions` 标签
3. 查看正在运行的工作流 "Build Android APK"
4. 等待编译完成（约 5-10 分钟）

### 步骤4: 下载编译好的 APK

**方式A: 从 Artifacts 下载（每次推送都有）**

```
1. 进入 Actions 页面
2. 点击最新的成功构建
3. 在页面底部 "Artifacts" 区域
4. 下载 "RemoteTouch-Debug" 压缩包
5. 解压得到 app-debug.apk
```

**方式B: 从 Releases 下载（推荐）**

```
1. 进入仓库主页
2. 点击右侧 "Releases"
3. 下载最新版本的 app-debug.apk
4. 直接安装到手机
```

---

## 🔧 配置说明

### 自动编译触发条件

GitHub Actions 会在以下情况自动编译：

- ✅ 推送代码到 `main` 或 `master` 分支
- ✅ 创建 Pull Request
- ✅ 手动触发（Actions 页面点击 "Run workflow"）

### 编译产物

每次成功编译会生成：

- **APK 文件**: `app-debug.apk` (约 2-3 MB)
- **Release 发布**: 自动创建带版本号的发布
- **Artifacts**: 保存 90 天

### 配置文件位置

```
.github/workflows/build-apk.yml  # GitHub Actions 配置
gradle/wrapper/                  # Gradle Wrapper
gradlew                          # Gradle 启动脚本
```

---

## 📊 查看编译日志

如果编译失败，可以查看详细日志：

```
1. 进入 Actions 页面
2. 点击失败的构建
3. 点击 "build" 任务
4. 展开失败的步骤查看错误信息
```

常见错误：
- ❌ Gradle 版本不兼容 → 检查 `gradle-wrapper.properties`
- ❌ 依赖下载失败 → 可能是网络问题，重新运行
- ❌ 编译错误 → 检查代码语法

---

## 🚀 进阶配置

### 修改编译配置

编辑 `.github/workflows/build-apk.yml` 可以自定义：

**1. 编译 Release 版本（需要签名）**

```yaml
- name: 构建 Release APK
  run: ./gradlew assembleRelease
```

**2. 运行测试**

```yaml
- name: 运行单元测试
  run: ./gradlew test
```

**3. 代码检查**

```yaml
- name: Lint 检查
  run: ./gradlew lint
```

### 添加签名配置

如果需要发布正式版本：

1. 生成签名密钥
2. 在 GitHub 仓库设置中添加 Secrets:
   - `KEYSTORE_FILE`: Base64 编码的 keystore 文件
   - `KEYSTORE_PASSWORD`: 密钥库密码
   - `KEY_ALIAS`: 密钥别名
   - `KEY_PASSWORD`: 密钥密码
3. 修改工作流使用这些 Secrets

---

## 💡 最佳实践

### 1. 使用语义化版本号

在 `app/build.gradle` 中更新版本：

```gradle
versionCode 2
versionName "1.1.0"
```

每次发布都会自动使用新版本号创建 Release。

### 2. 添加 Release Notes

编辑 `.github/workflows/build-apk.yml` 中的 `body` 字段，自定义发布说明。

### 3. 保持依赖更新

定期更新 `build.gradle` 中的依赖版本：

```gradle
implementation 'androidx.core:core-ktx:1.12.0'  // 最新版本
```

### 4. 缓存加速

GitHub Actions 已配置 Gradle 缓存，后续编译会更快。

---

## 📱 分享 APK

### 方法1: 直接分享 Release 链接

```
https://github.com/your-username/RemoteTouch/releases/latest
```

用户可以直接下载最新版本。

### 方法2: 使用短链接

使用 https://git.io 创建短链接：

```bash
curl -i https://git.io -F "url=https://github.com/your-username/RemoteTouch/releases/latest"
```

### 方法3: 生成二维码

使用在线工具（如 https://www.qr-code-generator.com）将 Release 链接转为二维码，方便手机扫描下载。

---

## ❓ 常见问题

### Q1: 编译太慢怎么办？

**A**: GitHub Actions 的速度取决于服务器负载。通常首次编译需要 5-10 分钟，后续会更快（有缓存）。

### Q2: 如何获得编译通知？

**A**: 在 GitHub 仓库设置中:
```
Settings → Notifications → Actions
勾选 "Send notifications for failed workflows"
```

### Q3: 可以编译多个变体吗？

**A**: 可以！修改工作流添加更多构建步骤：

```yaml
- name: 构建所有变体
  run: |
    ./gradlew assembleDebug
    ./gradlew assembleRelease
```

### Q4: 如何限制编译频率？

**A**: 添加路径过滤，只在特定文件改变时编译：

```yaml
on:
  push:
    paths:
      - 'app/**'
      - '*.gradle'
```

---

## 🎉 成功标志

当您看到以下内容时，说明云编译配置成功：

✅ Actions 页面显示绿色对勾
✅ Releases 页面有最新版本
✅ 可以下载并安装 APK
✅ 应用正常运行

---

## 🔗 相关链接

- [GitHub Actions 文档](https://docs.github.com/en/actions)
- [Gradle 构建工具](https://gradle.org/)
- [Android 开发者文档](https://developer.android.com/)

---

**享受云端自动编译带来的便利吧！** 🚀

每次代码更新，GitHub 都会自动为您编译最新的 APK！
