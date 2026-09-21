# SaltAmbience (椒盐白噪音)

一款遵循 **SaltUI 椒盐美学** 设计哲学、基于 **AndroidX Media3** 与 **Jetpack Compose** 构建的优雅原生白噪音混音应用。

让连绵细雨、山涧清泉、微风与林野鸟鸣在耳畔交织，助你沉浸专注、安抚身心、深度入眠。

---

## 视觉与交互特性 (SaltUI Aesthetic)

- **克制呼吸感**：低饱和度色彩体系，温润克制，消除视觉焦虑。
- **圆角卡片矩阵**：每一道自然声均拥有独立音量滑块、静音开关与状态指示。
- **悬浮多功能播控**：底部胶囊式播控条，实时显示当前混音音轨数、总音量及休眠定时状态。
- **平滑对数淡出**：休眠定时器到期前，音量按自然对数/二次幂曲线平滑衰减，杜绝突兀中断或截断爆音。

---

## 核心技术栈 (Tech Stack)

| 层次 / 模块 | 技术选型 | 说明 |
| :--- | :--- | :--- |
| **开发语言** | **Kotlin 2.0.21** | 官方 Compose 编译器插件，强类型协程与 Flow |
| **构建系统** | **Gradle 8.9 + AGP 8.7.2** | Version Catalogs (`libs.versions.toml`) 集中依赖管理 |
| **运行基线** | **JDK 21 LTS** / **Target API 35** | Min SDK 26, Compile SDK 35 (Android 15) |
| **UI 视觉框架** | **SaltUI 3.x** (`io.github.moriafly:salt-ui`) | 统一的椒盐风格组件、主题配色与圆角布局 |
| **音频混音引擎** | **AndroidX Media3 1.4.1** | ExoPlayer 多轨混音池，独立音量增益，禁用 Audio Offload |
| **系统媒体控制** | **MediaSessionService** | 虚拟主控协调器，统一前台服务、常驻通知栏与耳机拔出监听 |
| **数据持久化** | **Jetpack DataStore Preferences** | 轻量化状态记忆，自动还原上次音轨音量与自定义预设 |
| **序列化引擎** | **Kotlinx Serialization JSON** | 强类型无缝序列化场景预设与状态 |

---

## 音源资产与合规开源 (Sound Assets)

本项目所有自然声音频资产均来源于开源项目 [Blanket](https://github.com/rafaelmardojai/blanket) 以及 Freesound / Wikimedia Commons，格式全部为 **OGG Vorbis**，经过无缝交叉淡化循环处理：

- 细雨 (`rain.ogg`) · CC BY 4.0
- 雷雨 (`storm.ogg`) · CC BY 3.0
- 林风 (`wind.ogg`) · CC0 1.0
- 溪流 (`stream.ogg`) · CC0 1.0
- 篝火 (`fireplace.ogg`) · Public Domain
- 鸟鸣 (`birds.ogg`) · CC0 1.0
- 夏夜 (`summer_night.ogg`) · Public Domain
- 纯白噪 (`white_noise.ogg`) · CC BY-SA 3.0

详细版权与原作者署名参见 [SOUNDS_LICENSING.md](SOUNDS_LICENSING.md)。

---

## 本地构建与运行 (Build & Run)

### 前置要求
1. **JDK 21 LTS**（已配置或通过 `gradle.properties` 指定 `org.gradle.java.home`）
2. **Android SDK**（API 35 平台与构建工具）

### 构建步骤
```bash
# 1. 克隆仓库
git clone https://github.com/Kulahala/SaltAmbience.git
cd SaltAmbience

# 2. 配置 local.properties（指定 SDK 路径）
echo "sdk.dir=C\:\\Program Files (x86)\\Android\\android-sdk" > local.properties

# 3. 运行单元测试
./gradlew testDebugUnitTest

# 4. 编译 Debug APK
./gradlew assembleDebug
```

编译成功后，APK 产物位于：
`app/build/outputs/apk/debug/app-debug.apk`

---

## 开源协议

本项目代码遵循 MIT 协议开源。音频资源版权归属原作者，遵循各自的 CC0 / CC-BY 协议。
