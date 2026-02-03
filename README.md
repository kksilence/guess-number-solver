# 猜数字求解器 Android App

一个简单的 Android 应用，用于求解猜数字游戏（Mastermind 变种）。

## 游戏规则

- 猜一个 4 位数字密码（使用 0-5 共 6 个数字）
- 每个数字最多出现 2 次
- 反馈格式：`1`=位置正确，`2`=数字正确位置错误，`0`=错误

例如：猜测 `0123`，答案 `5021`，反馈 `1120`

## 功能特点

- 逐步输入猜测历史
- 自动计算下一步最优猜测
- 显示剩余可能密码数量
- 剩余密码 ≤15 个时显示具体列表
- 使用预计算数据，前两步查询速度极快

## 编译和安装

### 方法 1：使用 Android Studio（推荐）

1. 打开 Android Studio
2. 选择 `Open` → 选择 `GuessNumberSolver` 文件夹
3. 等待 Gradle 同步完成
4. 点击 `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
5. APK 文件位置：`app/build/outputs/apk/debug/app-debug.apk`
6. 将 APK 传输到手机安装即可

### 方法 2：使用命令行（需要安装 SDK）

```bash
# Windows (Gradle)
cd D:\working\猜数字\GuessNumberSolver
gradlew.bat assembleDebug

# APK 位置：app\build\outputs\apk\debug\app-debug.apk
```

### 方法 3：使用在线构建（无需安装开发环境）

访问以下网站，将整个项目文件夹上传即可在线构建：

- [AppVeyor](https://www.appveyor.com/)
- [GitHub Actions](https://github.com/)（上传到 GitHub 后启用 Actions）

## 使用方法

1. 打开 App
2. 输入你的猜测（4 位，0-5）
3. 输入游戏反馈（4 位，0/1/2）
4. 点击"添加"
5. 重复步骤 2-4
6. 点击"计算下一步"获取建议

## 项目结构

```
GuessNumberSolver/
├── app/
│   ├── src/main/
│   │   ├── java/com/guessnumber/solver/
│   │   │   ├── MainActivity.kt          # 主界面
│   │   │   ├── SolverEngine.kt          # 求解引擎（核心算法）
│   │   │   ├── GuessHistory.kt          # 数据类
│   │   │   └── HistoryAdapter.kt        # 列表适配器
│   │   ├── res/
│   │   │   ├── layout/                  # 界面布局
│   │   │   └── values/                  # 资源值
│   │   └── assets/
│   │       └── precomputed_depth2.json  # 预计算数据
│   └── build.gradle.kts                 # 应用构建配置
└── build.gradle.kts                     # 项目构建配置
```

## 算法说明

- **前两步**：使用预计算数据查询（< 0.01 秒）
- **后续步骤**：使用 Minimax 算法实时计算
- 性能：平均 4.38 次猜中，最多 6 次

## 注意事项

- 首次安装需要在手机设置中允许"未知来源"应用
- 个人使用版本，未申请任何危险权限
