# ⚡ 快速开始 - 云编译方式

## 🎯 3分钟获得APK文件

### 第1步: 创建GitHub仓库（1分钟）

1. 访问 https://github.com/new
2. 输入仓库名: `RemoteTouch`
3. 选择 `Public`（公开）
4. 点击 `Create repository`

---

### 第2步: 上传代码（1分钟）

在GitHub仓库页面点击 `uploading an existing file`

**将以下文件/文件夹拖拽上传**：
```
✅ .github/          # GitHub Actions配置（重要！）
✅ app/              # Android应用源码
✅ gradle/           # Gradle Wrapper
✅ .gitignore        # Git忽略文件
✅ build.gradle      # 项目构建配置
✅ gradle.properties # Gradle属性
✅ gradlew           # Gradle启动脚本（Linux/Mac）
✅ settings.gradle   # Gradle设置
✅ README.md         # 使用文档
✅ remote_touch_client.py  # Python客户端
```

**📌 重要提示**：
- 必须上传 `.github` 文件夹（包含自动编译配置）
- 必须上传 `gradle` 文件夹和 `gradlew` 文件

填写提交信息：`Initial commit`，点击提交。

---

### 第3步: 等待自动编译（5-10分钟）

代码上传后：

1. 点击仓库顶部的 **`Actions`** 标签
2. 查看正在运行的工作流：**Build Android APK**
3. 等待状态变为 ✅ 绿色对勾

**编译过程可视化**：
```
⏳ 正在编译...
   └─ 检出代码
   └─ 设置 JDK 11
   └─ 下载依赖
   └─ 构建 APK
   └─ 上传 APK
✅ 编译成功！
```

---

### 第4步: 下载APK（30秒）

**方法A: 从 Releases 下载（推荐）**
```
1. 点击仓库右侧 "Releases"
2. 点击最新版本
3. 下载 app-debug.apk
4. 传输到手机安装
```

**方法B: 从 Artifacts 下载**
```
1. 在 Actions 页面点击成功的构建
2. 下拉到页面底部 "Artifacts"
3. 下载 "RemoteTouch-Debug.zip"
4. 解压得到 app-debug.apk
```

---

## 📱 安装和使用

### 手机端（一次性设置）

```
1. 安装 app-debug.apk
2. 设置 → 无障碍 → 远程触摸控制 → 开启
3. 设置 → 电池 → 电池优化 → 远程触摸控制 → 不优化
4. 打开应用，点击"启动服务"
5. 记录显示的IP地址，如: 192.168.1.100:8888
6. 最近任务界面 → 下拉应用 → 锁定
```

### 电脑端使用

```bash
# 1. 安装Python依赖
pip install pynput

# 2. 下载客户端
# 在GitHub仓库下载 remote_touch_client.py

# 3. 运行（替换为手机显示的IP）
python remote_touch_client.py start 192.168.1.100

# 4. 开始使用
# 移动鼠标 → 手机同步
# 点击鼠标 → 手机点击
```

---

## 🔄 后续更新

当您修改代码后：

```bash
1. 在GitHub网页上传修改后的文件（覆盖）
2. GitHub Actions 自动重新编译
3. 新的APK会自动发布到 Releases
4. 下载新版本安装（会覆盖旧版本）
```

---

## ❓ 常见问题

### Q: 编译失败怎么办？

点击 Actions → 失败的构建 → 查看错误日志。常见原因：
- 文件上传不完整（缺少 .github 或 gradle 文件夹）
- 网络问题导致依赖下载失败（重新运行即可）

### Q: 如何手动触发编译？

```
Actions → Build Android APK → Run workflow → Run workflow
```

### Q: APK在哪里？

```
方法1: Releases → 最新版本 → 下载 app-debug.apk
方法2: Actions → 构建详情 → Artifacts → RemoteTouch-Debug
```

---

## 📚 详细文档

- **完整使用说明**: [README.md](README.md)
- **云编译详解**: [CLOUD_BUILD_GUIDE.md](CLOUD_BUILD_GUIDE.md)
- **本地编译方法**: [BUILD_GUIDE.md](BUILD_GUIDE.md)

---

## ✅ 成功标志

当您看到：
- ✅ Actions 显示绿色对勾
- ✅ Releases 有最新版本
- ✅ 可以下载 app-debug.apk
- ✅ 应用安装并正常运行

**恭喜！云编译配置成功！** 🎉
