# SaltAmbience (椒盐白噪音)

一款遵循 **SaltUI 椒盐美学** 设计哲学、基于 **AndroidX Media3** 与 **Jetpack Compose** 构建的原生白噪音混音应用。

让连绵细雨、山涧清泉、夜风与林野鸟鸣在耳畔交织，助你专注沉浸、安抚身心、深度入眠。

> **致敬与独立声明 (Attribution & Disclaimer)**：  
> SaltAmbience（椒盐白噪音）为独立开发者开源作品，视觉设计深受 **Moriafly** 的 **SaltUI 椒盐美学** 设计哲学启发并采用其开源 UI 组件库开发。本项目与「椒盐音乐 (Salt Player)」及 Moriafly 官方团队无商业附属或隶属关系。感谢 Moriafly 为开源社区贡献的极致设计规范！

---

## 核心设计与特性

### 1. 椒盐美学交互 (SaltUI Aesthetic)
- **包豪斯声学极简矢量符号与常驻色彩体系**：全站废除拟物彩色 Emoji，由点、线、面与波形构成的包豪斯矢量符号组件（`BauhausSoundIcon` 与 `BauhausUiIcon`）统一接管；音效卡片未播放时呈现 48% 柔光呼吸微色，点亮激活跃迁至 100% 高饱和双色高光；系统功能图标与分类芯片常驻自然拟物双色，视觉质感先锋灵动。
- **包豪斯声学自适应图标**：以「包豪斯声学 · 琴弦点线面」(D-14) 为官方自适应图标体系，午夜蓝黑底座融合垂直声学微轴与三道利落琴弦声波；支持 Android 13+ Material You 壁纸动态取色，同步归档 Teenage Engineering 风格备选资源 (D-16)。
- **顶栏折叠吸顶与全场景常驻预设栏**：网格上滑时大标题随手势 1:1 动态收缩淡出；场景预设横滑栏稳固吸附在状态栏正下方，无论翻阅多深随时轻触切换；预设新建、恢复与导入操作单点归一收纳在横滑条末尾，兼顾极简呼吸感与高频操作效率。
- **全设备响应式自适应网格**：采用 `GridCells.Adaptive(minSize = 160.dp)` 配合全宽通栏布局；普通手机竖屏稳定呈现 2 列黄金比例，折叠屏展开态、平板或横屏模式下自动平滑延展为 3~4 列精致方格，杜绝拉伸畸变。
- **原生高斯虚化与物理弹簧抽屉**：全站弹窗统一为大圆角底部抽屉（`SaltBottomSheet`），配备 `dampingRatio = 0.82f` 的细腻进退场物理弹簧；抽屉展开时主屏背景平滑过渡至 `14.dp` 原生高斯失焦（Android 12+ GPU 硬件加速），质感通透纯净。
- **纯实色防叠字悬浮舱**：吸顶顶栏与底部常驻播控胶囊全面采用 100% 不透明实色表面（深色 `#121212` / `#1E2026`，浅色 `#FAFAFA` / `#F2F4F7`）搭配细微 1.dp 边缘高光描边与 130.dp 底部避让，彻底杜绝半透明背景导致的卡片文字穿透叠字；0 掉帧、0 额外能耗。

### 2. 专业多轨混音引擎 (Audio Architecture)
- **全量 22 款高品质无缝自然音**：雨水、自然、生活、纯噪四维分类胶囊（Filter Chips）即时无缝过滤，包含 CC0 慢波助眠棕色噪音 (Brown Noise) 及自研科学绿噪；全部音源均经过采样级正余弦等能量交叉淡化无缝循环处理。
- **防循环疲劳声学微动态**：引入「起播随机时间戳偏置」（打破固定开头记忆）与「±2% 自然微速差纯净重采样」（打破机械节拍公倍数与相位死锁），多轨混音相对相位持续滑动，长时聆听如大自然般永不单调。
- **零时延瞬发响应引擎**：定制 50ms 本地超低延迟缓冲控制（`lowLatencyLoadControl`），配合 UI 0ms 乐观翻转与音频焦点异步协程调度，消除体感起播与暂停延迟。
- **锁屏通知黑胶艺术大封面**：动态渲染 512×512 包豪斯黑胶声学艺术大封面注入系统锁屏与媒体通知中心，消灭粗糙拉伸白三角，实时联动当前播放主题。
- **轻量播放器池 (ExoPlayer Pool)**：每轨独立音量控制与静音开关，绕过硬件 DSP 单流 Offload 限制，走系统 PCM 纯净多路混音管线。
- **感知平滑淡出休眠**：休眠倒计时结束前采用对数/二次幂曲线平滑衰减至静音，贴合人耳感知，杜绝截断爆音与心惊。
- **前台保活与集中焦点调度**：基于 `MediaSessionService` 实现常驻通知栏快捷控制，耳机拔出自动暂停，统一监听音频焦点（来电暂停、短通知 Ducking 避让），超低功耗深度休眠。

### 3. 场景预设与口令分享 (Presets & Sharing)
- **方案自由管理**：内置「深夜暴雨」、「海边小木屋」等经典预设，支持一键将当前混音保存为自定义预设；自定义方案倒序置顶，支持删除二次确认防误触，提供「↺ 恢复默认」一键复原机制。
- **Base64 口令秒级互通**：长按预设触感复制分享口令；应用冷/热启动自动识别剪贴板预设口令并弹出轻量胶囊一键导入。

### 4. 极致隐私与功耗守护 (Privacy & Performance)
- **零网络权限**：代码与 Manifest 清单中 `0` 个网络权限，不申请 `INTERNET`，没有任何数据上传、埋点统计或广告组件，纯粹离线运行。
- **屏幕常亮守护**：设置中心内置床头/桌面看护开关，在播放中可按需保持屏幕微光常亮。

---

## 核心技术栈 (Tech Stack)

| 层次 / 模块 | 技术选型 | 说明 |
| :--- | :--- | :--- |
| **开发语言** | **Kotlin 2.0.21** | 官方 Compose 编译器插件，强类型协程与 Flow 驱动 |
| **构建体系** | **Gradle 8.9 + AGP 8.7.2** | Version Catalogs (`libs.versions.toml`) 集中版本管理 |
| **运行基线** | **JDK 21 LTS** / **Target API 35** | Min SDK 26 (Android 8.0), Compile & Target SDK 35 (Android 15) |
| **UI 视觉框架** | **SaltUI 3.x** (`io.github.moriafly:salt-ui`) | 椒盐设计规范、圆角抽屉、自适应深浅模式 |
| **音频引擎** | **AndroidX Media3 1.4.1** | `ExoPlayer` 多轨混音池，无缝循环，禁用 Audio Offload |
| **媒体控制** | **MediaSessionService** | 统一前台服务、锁屏通知栏与系统音频焦点调度 |
| **状态持久化** | **Jetpack DataStore Preferences** | 轻量化混音记忆与预设序列化存储 |
| **序列化引擎** | **Kotlinx Serialization JSON** | 强类型安全 JSON 序列化 |

---

## 音源资产与合规开源 (Sound Assets)

本项目自然声音频资产均经过采样级无缝循环处理，遵循各自的 CC0 / CC-BY / Pixabay 开源许可：

- 细雨 (`rain.ogg`) · [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/) (Blanket / Rafael Mardojai)
- 雷雨 (`storm.ogg`) · [CC BY 3.0](https://creativecommons.org/licenses/by/3.0/)
- 林风 (`wind.ogg`) · [CC0 1.0](https://creativecommons.org/publicdomain/zero/1.0/)
- 溪流 (`stream.ogg`) · [CC0 1.0](https://creativecommons.org/publicdomain/zero/1.0/)
- 篝火 (`fireplace.ogg`) · Public Domain
- 鸟鸣 (`birds.ogg`) · [CC0 1.0](https://creativecommons.org/publicdomain/zero/1.0/)
- 夏夜 (`summer_night.ogg`) · Public Domain
- 纯白噪 (`white_noise.ogg`) · [CC BY-SA 3.0](https://creativecommons.org/licenses/by-sa/3.0/)
- 海浪 (`waves.ogg`) · [CC BY 3.0](https://creativecommons.org/licenses/by/3.0/)
- 咖啡馆 (`coffee_shop.ogg`) · Public Domain
- 列车 (`train.ogg`) · [CC BY 3.0](https://creativecommons.org/licenses/by/3.0/)
- 小舟 (`boat.ogg`) · [CC0 1.0](https://creativecommons.org/publicdomain/zero/1.0/)
- 粉红噪 (`pink_noise.ogg`) · [CC BY-SA 3.0](https://creativecommons.org/licenses/by-sa/3.0/)
- 都市 (`city.ogg`) · [CC BY 3.0](https://creativecommons.org/licenses/by/3.0/)
- 棕色噪音 (`brown_noise.ogg`) · [CC BY-SA 3.0](https://creativecommons.org/licenses/by-sa/3.0/)
- 电风扇 (`fan.ogg`) · [Pixabay / CC0](https://github.com/remvze/moodist) (Moodist / MAZE)
- 钟表 (`clock.ogg`) · [Pixabay / CC0](https://github.com/remvze/moodist) (Moodist / MAZE)
- 机械键盘 (`keyboard.ogg`) · [Pixabay / CC0](https://github.com/remvze/moodist) (Moodist / MAZE)
- 风铃 (`wind_chimes.ogg`) · [Pixabay / CC0](https://github.com/remvze/moodist) (Moodist / MAZE)
- 雨打屋檐 (`rain_roof.ogg`) · [Pixabay / CC0](https://github.com/remvze/moodist) (Moodist / MAZE)
- 深海水声 (`underwater.ogg`) · [Pixabay / CC0](https://github.com/remvze/moodist) (Moodist / MAZE)
- 科学绿噪 (`green_noise.ogg`) · [CC0 1.0](https://github.com/Kulahala/SaltAmbience) (SaltAmbience 自研算法)

详细原作者署名、源链接与独立分发限制参见 [SOUNDS_LICENSING.md](SOUNDS_LICENSING.md)。

---

## 本地构建与运行 (Build & Run)

### 前置环境
- **JDK 21 LTS**
- **Android SDK**（API 35 平台与构建工具）

### 构建命令
```bash
# 运行单元测试
./gradlew testDebugUnitTest

# 编译 Release 安装包
./gradlew assembleRelease
```
产物位置：`app/build/outputs/apk/release/app-release.apk`

---

## 开源协议 (License)

本项目软件源码基于 [MIT License](LICENSE) 开源。  
媒体与音频资产保留各自独立的 Creative Commons、Public Domain 及 Pixabay 许可条款，详见 [SOUNDS_LICENSING.md](SOUNDS_LICENSING.md)。
