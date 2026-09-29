# AGENTS.md

Guidance for coding agents working in WhiteNoise-Android.

---

## 1. 核心原则与工作风格 (Operating Style)

- **中文直给**：默认中文沟通，直接输出技术结论与依据；不确定时明确指出具体不确定点，不敷衍。
- **事实与权威第一**：真实源码、Gradle 构建配置、清单文件与编译输出高于文档。当文档冲突时：
  `app/src/ > gradle/libs.versions.toml > build.gradle.kts > plan.md > AGENTS.md > README.md`
- **高低危决策分层（防无谓早停）**：
  - **高危硬红线（High-Stakes，必须先确认）**：
    - 引入重度第三方框架（如擅自引入 Hilt/Koin、Room 等未要求的重型依赖）；
    - 破坏或推翻已定音频架构（如放弃 Media3 ExoPlayer 池转用旧版 MediaPlayer）；
    - 破坏性删除文件、目录或全局重构；
    - 执行 Git Commit 操作；
    - 自动递增版本号或推送发版 Tag（必须经用户明确同意，见第 6 节）；
    - 修改已授权清单外的文件。
  - **自主推进区（Low-Stakes，自主闭环）**：
    - 在已批准阶段和模块内，具体的 Kotlin 算法实现、Compose UI 局部布局排版、ViewModel 状态流绑定、私有辅助类抽取、单元测试编写；
    - Agent 应自主闭环交付，严禁因微小局部细节频繁中断请示。
- **最小必要改动（YAGNI）**：
  - 只写必须的代码，不为单次使用过度抽象，不为“以后可能需要”提前堆积插件化、多主题定制、复杂数据库框架；
  - 交付前清理无用 import、未引用变量与临时调试代码。
- **事前深度对齐（Grill before Code）**：
  凡涉及新增重大功能、重构音频管线或修改持久化契约时，Agent 必须先列出方案设计取舍、破坏性风险与边界问答，与用户确认无歧义后方可动刀，严禁盲目开工。
- **测试先行守门（Test-Driven Gate）**：
  所有核心业务算法（如多轨音量增益衰减、休眠曲线、JSON 序列化往返、状态机流转）必须配备独立可运行的 JVM 单元测试，PR / 交付前必须在本地无缓存运行全绿（`./gradlew testDebugUnitTest`）。
- **项目专属技能隔离（Project-Specific Skills）**：
  宿主主要工作为 UE (Unreal Engine) 开发，全局 Skill 库面向游戏引擎；针对本 Android 原生项目的专属扩展技能（如 Media3 编解码调优、SaltUI 模式等），**强制存放于本项目根目录 `.agents/skills/<skill-name>/SKILL.md`**，实现项目级隔离管理，严禁污染全局 UE 工作区。
- **原生编辑优先**：
  - 文件修改优先使用原生工具（`replace_file_content` / `write_to_file`），严禁无谓编写临时脚本替代编辑。
- **文档分层与防膨胀铁律 (Documentation Layering & Anti-Bloat)**：
  - **`README.md` 严格定位于产品与架构白皮书**：仅面向最终用户与开源访客展示系统当前最新版本的“终态设计与核心特性”；**严禁**在此追加版本更新日志（Changelog）、缺陷修复流水账或调试记录；版本发布细节统一交由 GitHub Releases 处理；
  - **`AGENTS.md` 严格定位于系统工程与架构总账 (Architecture Contracts)**：
    - **契约固化优先**：凡重大功能与声学/UI模式落地，必须提炼沉淀为第 3 节的【永久架构契约】，作为后续所有 Agent 的系统底线；
    - **演进总账紧凑化**：第 4 节阶段演进记录**仅保留表格级里程碑索引（单阶段严格限制 1~2 行）**，历史阶段定期折叠归档至 4.1；
    - **严禁过程性垃圾**：严禁在 AGENTS.md 堆砌对话历史、单次 Bug 调试排错过程或临时日志。

---

## 2. 环境基线与系统约束 (Environment & Build Baseline)

本项目运行环境与 SDK 路径已完全实测确认：

| 组件 / 配置项 | 确切版本 / 物理路径 | 关键规则与注意事项 |
| :--- | :--- | :--- |
| **JDK 版本** | **JDK 21 LTS** (`C:\PROGRA~2\Android\openjdk\jdk-21.0.8` 或带引号的完整路径) | **致命避坑**：系统环境变量曾被误设为不存在的 `E:\develop\jdk-21`。Windows cmd/bat 遇到路径含 `(x86)` 会报语法错误，必须使用 8.3 短路径 `C:\PROGRA~2`，并在 `gradle.properties` 中固化 `org.gradle.java.home=C:/PROGRA~2/Android/openjdk/jdk-21.0.8`。 |
| **Android SDK** | `C:\Program Files (x86)\Android\android-sdk` | 项目根目录 `local.properties` 写入：`sdk.dir=C\:\\Program Files (x86)\\Android\\android-sdk`。已实装 `platforms;android-35`、`build-tools;35.0.0` 与 `platform-tools;36.0.0`。 |
| **Min / Target / Compile SDK** | **Min: 26** / **Target: 35** (Android 15) / **Compile: 35** | 统一对齐为 `compileSdk = 35`、`targetSdk = 35`、`minSdk = 26`。覆盖全球 99.3% 设备。 |
| **构建体系** | Gradle 8.9+ / AGP 8.7+ | 必须使用 Version Catalogs (`gradle/libs.versions.toml`) 集中声明依赖版本。工程自包含 Gradle 8.9 Wrapper。 |
| **Kotlin & Compose** | **Kotlin 2.0.21+** | **强制约束**：必须使用官方 Compose 编译器插件 `org.jetbrains.kotlin.plugin.compose`；需配置 `-Xskip-metadata-version-check`；**严禁**在 `build.gradle.kts` 中使用已废弃的 `composeOptions { kotlinCompilerExtensionVersion = ... }`。 |
| **UI 视觉框架** | **SaltUI 3.x** (`io.github.moriafly:salt-ui:3.0.0-beta01`) | 来自 `mavenCentral()`。组件标题推荐使用 `ItemOuterTitle`（旧版 `ItemTitle` 已弃用）。 |
| **音频引擎** | **AndroidX Media3 1.4.x+** | `media3-exoplayer` + `media3-session` + `media3-ui`。 |

---

## 3. 架构总账与核心契约 (Architecture Contracts)

本项目架构合并于本文件中，作为唯一的架构事实基准：

```
app/src/main/java/com/whitenoise/app/
├── core/                  # 底层核心逻辑
│   ├── audio/             # Media3 多轨混音引擎 (ExoPlayerPool, AudioMixerEngine)
│   ├── service/           # 前台播放保活服务 (WhiteNoiseMediaService, MediaNotificationManager)
│   └── model/             # 核心领域模型 (SoundTrack, Preset, PlaybackState)
├── data/                  # 数据层
│   ├── datastore/         # DataStore Preferences (音量记忆, 预设存储)
│   └── repository/        # 音轨与预设仓库 (SoundRepository, PresetRepository)
├── ui/                    # 表现层 (SaltUI + Jetpack Compose)
│   ├── theme/             # 主题配置 (SaltTheme 适配)
│   ├── components/        # 通用组件 (音轨卡片, 悬浮播控条, 休眠抽屉, SaltBottomSheet)
│   ├── home/              # 首页界面 (自适应网格, 场景方案, 分类过滤)
│   └── MainViewModel.kt   # 状态流 ViewModel
└── MainActivity.kt        # 单 Activity 宿主
```

### 3.1 多轨混音引擎契约 (`AudioMixerEngine`)
- **ExoPlayer Pool 机制**：单个 ExoPlayer 实例同一时刻只能解码单路音频流。为了实现自然声的多重叠加混音（如：雨声 + 篝火 + 远雷），维护轻量播放器池（`Map<String, ExoPlayer>`），按音轨维度独立控制。
- **杜绝 Audio Offload 冲突**：硬件 DSP Audio Offload 无法承载多路压缩流并发混音。多轨播放**严禁**开启 Audio Offload，必须走系统 `AudioFlinger` 的 PCM 混音管线。
- **采样级无缝循环 (Gapless Loop)**：音源一律采用 **OGG Vorbis** 或 **Opus** 格式，严禁使用含启动填充延迟的 AAC；配置 `Player.REPEAT_MODE_ONE`。
- **主增益与感知平滑淡出**：
  - 单轨音量公式：$ActualVolume = TrackVolume \times MasterVolume$；
  - 休眠淡出：在休眠定时器最后阶段（如 `min(60s, totalTime / 4)`），采用对数/二次幂曲线衰减，匹配人耳感知，杜绝截断爆音。
- **低时延瞬发响应引擎**：定制 50ms 本地缓冲策略（通过 `createLowLatencyLoadControl()` 工厂为各播放器独立创建实例，杜绝 Media3 `DefaultLoadControl` 单线程亲和性断言导致的跨轨哑音崩溃），配合 UI 0ms 乐观状态流即时翻转与音频焦点异步协程调度，消除体感起播与暂停延迟。
- **防循环疲劳声学微动态**：音轨点亮或切换预设时执行方案 A（安全随机起始偏置，打破 00:00.000 固定开头）与方案 B（±2% 自然微速差纯净重采样 `PlaybackParameters(speed, speed)`，打破机械节拍公倍数与相位死锁）；暂停再继续严格保持当前进度；未就绪音轨在 `STATE_READY` 延迟安全 seek。

### 3.2 前台保活与媒体控制契约 (`WhiteNoiseMediaService`)
- **生命周期与保活**：继承 `androidx.media3.session.MediaSessionService`，前台服务类型明确指定为 `android:foregroundServiceType="mediaPlayback"`。充分利用 `MediaSessionService` 原生通知与前台调度能力，避免自建冲突的通知循环。
- **系统媒体控制（虚拟主控 Player）**：MediaSession 仅接受单 Player 接口。基于 `SimpleBasePlayer` 代理主控 Play/Pause/Stop 指令并统一下发至子音轨池。
- **集中式音频焦点 (AudioFocus)**：各子 ExoPlayer 必须设置 `handleAudioFocus = false`，禁止各自争抢焦点；由引擎统一监听 `AudioManager` 焦点事件（来电暂停、短通知 Ducking 整体主音量）。播放结束完整释放资源。
- **锁屏与通知栏包豪斯动态封面与原生倒计时**：基于 `Canvas` 纯数学几何动态生成 512×512 黑胶声学大封面注入 `NotificationCompat.Builder.setLargeIcon` 与 `MediaMetadata.artworkData`，小图标统一为矢量单色 `ic_launcher_monochrome`，内置 LRU 缓存并按当前主题声渲染专属 Ambient Glow 氛围光与拟物色彩；休眠定时开启时由系统原生 `setUsesChronometer(true)` + `setChronometerCountDown(true)` 硬件级秒级倒数，杜绝重复唤醒 CPU。

### 3.3 椒盐美学 UI 与包豪斯设计系统契约 (`SaltUI`)

#### 3.3.1 视觉分层与色彩基准契约 (Design System Tokens & Surface Contract)
- **视觉风格**：清爽克制、低饱和度、大圆角卡片、清晰的分组布局。
- **全站视觉分层与线框基线铁律**：
  - **Level 0（主视口底色）**：必须使用 `SaltTheme.colors.background`（浅色为纯白 `#FAFAFA`，深色为 `#121212`），严禁在根容器滥用 `subBackground` 导致全局发灰；
  - **Level 1（卡片与容器）**：统一平整纯色（浅色为浅灰 `#F3F4F6`，深色为玄武岩冷炭黑 `#1B1D24`），配合大圆角（`14.dp`~`20.dp`）；彻底废除浅色模式下散乱的拟物微凸与弥散阴影，保障全站 Bento 网格严整平顺；
  - **Level 2（操作胶囊与芯片）**：统一正圆（`CircleShape`）或大圆角（`12.dp`~`14.dp`），内边距紧凑统一（水平 `10.dp`、垂直 `6.dp`），文字标签强制加挂 `maxLines = 1, softWrap = false`，严禁折行；
  - **全局 1.dp 柔光微描边铁律**：
    - **未激活/静止态容器与胶囊**（场景预设卡片、音效卡片、顶栏双胶囊、统计抽屉三大容器卡片）：统一强制配备 `1.dp` 柔光微描边（深色模式 `Color.White.copy(0.08f)`、浅色模式 `Color.Black.copy(0.06f)`），胶囊按键可提升至 `0.12f / 0.08f`；杜绝浅色下无界发白、深色下 OLED 融墨发虚；
    - **激活/高亮态跃迁**：平滑过渡加粗至 `1.5.dp` 高亮强调描边（`SaltTheme.colors.highlight` 或分类主题自然语义色）并注入 `10%`~`16%` 柔光底色。
- **展示态与控制态色彩分立契约 (Showcase vs Control State Semantic Dichotomy)**：
  - **控制态组件（Matrix Control）**：主页 Bento 音效卡片严格由播放状态 `isPlaying` 驱动，未激活呈现素描黑白灰阶（Monochrome Idle），激活平滑跃升自然色彩；
  - **展示态/荣誉资产组件（Showcase / Honor Asset）**：伴眠统计排行榜、混音清单、预设音轨列表等非直接控制矩阵，代表用户已经体验或陪伴的声音资产，**强制常驻 100% 拟物自然双色高光（`isPlaying = true` 或 Showcase 模式）**，严禁误套用未播放灰阶失去声学识别度；
  - **音效专属拟物个性色彩流光条契约**：排行榜相对进度条与首位序号高光严禁简单套用单一分类色（防止同分类同色撞车与单调），必须统一调用 `getTrackIndividualColor(trackId)` 映射至该音效自身最具辨识度的包豪斯拟物色彩（如雷雨闪电金黄、细雨天青蓝、溪流湛蓝、林风苍翠绿、篝火烈焰红橙、黑胶复古暖红等），赋予每个声音生命力。
- **顶部分类导航微光胶囊契约**：分类芯片（全部/雨水/自然/生活/纯噪）未选中时常驻中性素描灰阶，选中时跃迁至专属自然语义色（16% 微光背景与 55% 强调边框）；支持浅色模式高对比度深色阶映射（`getContentColor(isDark)`），强光直射清晰可辨。
- **浅色模式色彩对比度加深铁律 (Light-Mode Contrast Accessibility Contract)**：
  - 严禁在浅色模式下直接将未加深的原始 `themeColor`（如天青蓝 `#38BDF8`、苍翠绿 `#34D399`、柔粉 `#F472B6`）用作细字体或 1.dp 细描边（强光下发飘发白，对比度不足 2.5:1）；
  - 必须显式区分：背景微光填充使用 `themeColor.copy(alpha = 0.10f~0.16f)`；文字及高对比度边框必须强制调用 `category.getContentColor(isDark)` 自动映射至深色阶（如天青映射为 `#0284C7`，嫩绿映射为 `#059669`，粉色映射为 `#DB2777`），确保完全符合 WCAG AA 对比度标准。
- **文字阶梯规范**：主标题/正文使用 `text`，次级信息使用 `text.copy(alpha = 0.65f)`，失焦提示使用 `text.copy(alpha = 0.40f)`；严禁在卡片上直接绘制 `subText` 造成灰底灰字。

#### 3.3.2 全站包豪斯矢量符号与图腾契约 (Bauhaus Vector & Iconography Contract)
- **全站 100% 纯 Canvas 几何矢量与零 Emoji 铁律 (Zero Emoji Bauhaus Vector Contract)**：
  - 全站播控、功能操作、主题切换、关闭勾选、月牙符号（`BauhausMoonIcon`）与空状态图腾（`BauhausEmptyStatsTotem`）全面由纯 Canvas 点线面几何接管；
  - 严禁引入任何系统 Emoji（如 `🌙`）与 Unicode 字符 Hack；空状态大卡片杜绝直接放大 13dp 小功能图标，统一采用 56dp+ 沉浸声学共鸣与安睡图腾；
  - **包豪斯欧几里得双圆差集月牙与几何微星契约**：月牙符号（`BauhausMoonIcon`）严禁使用手捏单薄贝塞尔软弧（防止在微小尺寸下萎缩成左括号 `(`）；强制采用严谨的欧几里得双圆几何差集（`PathOperation.Difference`，厚度 38% 饱满月腹与尖锐两角），并在怀抱中点缀 4 芒几何菱形微星（点面呼应），尺寸统一 13.dp。
- **包豪斯声学极简矢量符号与拟物色彩语义 (`BauhausSoundIcon`)**：全站 30 款音效由纯几何点、线、面构成的 `BauhausSoundIcon` 接管；未激活时呈现 48% 柔光呼吸微色（`toIdlePalette(0.48f)`），激活时映射真实自然声学意象（溪流上白下水蓝、篝火烈焰橙红+火星金黄、雷雨高能电光黄+暴雨白、林风苍翠绿、夏夜月牙金+静谧夜紫、海浪深海蔚蓝、粉噪柔粉、棕噪大地暖褐等）并跃升至 100% 高饱和双色高光；底栏播放条做减法移除重复混音按钮，右侧独占动态倒计时胶囊。
- **全站 UI 核心系统图标包豪斯矢量化与常驻色彩契约 (`BauhausUiIcon`)**：全站播控（Play/Pause）、控制中心清空（Clear）、主题模式（ThemeSystem/Light/Dark）、定时胶囊（Timer）、添加/恢复（Add/Restore）、设置（Settings）、弹窗关闭与勾选（Close/Check）全面接管；接入专属 `BauhausUiTheme` 常驻双色语义，严禁全局强制染灰；控制中心停止按钮精准定名为「清空混音」，杜绝交互误导。
- **空状态沉浸声学图腾规范 (Empty State & Visual Totem Contract)**：全站空状态（无混音、无统计记录、无自定义预设）严禁粗暴放大已有微图标；必须统一采用 56dp+ 纯 Canvas 包豪斯专属声学与空间图腾（如 `BauhausEmptyStatsTotem`），居中配以 15sp 加粗主标题（85% alpha）和 12sp 引导副标（50% alpha），间距保持 10dp~12dp，维持极简留白的艺术张力与沉浸感。
- **应用图标与全密度位图契约 (Adaptive & Legacy Icon Contract)**：
  - 落地 Android 8.0+ 官方自适应矢量图标「包豪斯声学 · 琴弦点线面」(D-14) 为主图标（`res/drawable/` + `res/mipmap-anydpi-v26/`），配套 `ic_launcher_monochrome.xml` 支持 Android 13+ 壁纸动态取色；归档 D-16 备选；
  - 严格配备 mdpi/hdpi/xhdpi/xxhdpi/xxxhdpi 全密度 1080p 超采样抗锯齿 PNG 标准位图（`ic_launcher.png` 与 `ic_launcher_round.png`），保障 OEM 系统设置【应用信息】、多任务栈预览与传统启动器完美渲染，杜绝降级为系统默认播放器白三角。

#### 3.3.3 动态物理与手势交互契约 (Dynamic Physics & Gesture Navigation Contract)
- **包豪斯声学生命力觉醒与非线性形态形变契约 (Bauhaus Acoustic Morphing Contract)**：
  - **矩阵素描与高光反差**：音效矩阵未激活音标与未选中分类胶囊常驻纯粹的素描黑白灰阶（Monochrome Idle），全局其他系统图标（Timer/Clear/Theme 等）保持专属常驻语义色彩；音效激活或分类选中时平滑跃升至 100% 拟物自然双色；
  - **速率变奏物理弹簧与生命觉醒**：音效点亮时由非线性变奏弹簧 `spring(dampingRatio = 0.58f, stiffness = 320f)` 驱动微缩放呼吸与各音效几何形态专属展开插值（雨丝拉长滑落、闪电劈裂激射、风浪流动、溪流跃浪、火星升腾、声谱条跳跃等），赋予激活瞬间的有机生命力；
  - **播控无缝几何形态形变 (`BauhausPlayPauseMorphIcon`)**：底栏与控制中心播放/暂停按钮告别硬切，基于弹性物理弹簧驱动三角形（Play 锋利汇聚）与对称双矩形柱（Pause 垂直挺立）之间的实时无缝分裂与聚合插值。
- **音效矩阵网格卡片平滑物理重排契约 (Grid Filter Animation Contract)**：
  - 分类切换（全部/雨水/自然/生活/纯噪）杜绝生硬硬切，基于 Compose 1.7+ `LazyGridItemScope.animateItem` 原生布局管道驱动；
  - 离场卡片执行 150ms 敏捷淡出，留存卡片由 `spring(dampingRatio = 0.82f, stiffness = 420f)` 驱动约 200ms 内磁吸平滑重排，新进场卡片执行 180ms 优雅淡入，兼顾连续视觉动线与零性能冗余。
- **单手轻扫切分类与卡片长按左右调音手势契约 (Swipe Navigation & Card LongPress Volume Contract)**：
  - **单手轻扫切分类**：主网格挂载 `draggable(Orientation.Horizontal)`，基于触摸斜率隔离垂直滚动，横向位移超 40dp 或初速度超 800f/s 触发分类顺滑切换；胶囊接入 180ms 柔光呼吸变色并自动视口滚动；
  - **卡片长按调音**：`SoundTileCard` 采用底层 `awaitEachGesture` 精密手势状态机，220ms 内轻点秒关、快速划走放行外层滚动，长按静止 220ms 触发 `LongPress` 坚实咬合微震，独占消费事件并进入调音模式；
  - **相对位移左右调音**：卡片全宽行程对应 100% 调节，实时响应音频引擎并通过 ViewModel 的 `debounce(500L)` 安全持久化；
  - **双重视觉与触觉反馈**：右上角胶囊放大 1.18×、卡片微下沉 0.97×；边界触底轻震阻尼，松手微震落锁平滑弹回原位（遵循极简克制，移除多余底边条）。
- **包豪斯折叠吸顶与防叠字纯实色悬浮舱契约 (Collapsible Sticky Header & Anti-Bleed Floating Dock Contract)**：
  - **大标题动态平滑折叠**：网格向上滚动时，可折叠大标题与场景方案行（90.dp）随网格滚动距离 1:1 动态收缩淡出至 0.dp，两相联动丝滑无突变；
  - **预设横滑栏永远稳固吸顶**：下部预设横滑栏（固定 54.dp）永远吸附在状态栏正下方（`statusBarsPadding()`），不设负 offset、不做外部截断，无论滚动到多深随时横滑切换混音；
  - **预设操作单点归一与官方库收纳**：主屏预设横滑栏做极致减法，彻底移除低频冗余的“恢复默认”胶囊，仅保留 `[方案卡片] ... [+ 存为预设] [📥 导入]`；将默认预设找回、单项恢复与官方方案库深度收纳进“导入”弹窗，已存在方案轻触提示“无需重复添加”，误删方案轻触即刻单项找回；
  - **纯实色防叠字悬浮舱**：顶栏与底栏播放条全面采用 100% 纯实色（`SaltTheme.colors.background` 与 `Color(0xFF1E2026)`），搭配 1.dp 精致边缘微描边与柔和阴影，彻底摒弃不稳定的外部 alpha 模糊库，杜绝字体重叠透底与崩溃闪退。
- **大屏自适应网格与动效虚化契约**：
  - 音效矩阵采用 `GridCells.Adaptive(minSize = 160.dp)`；所有头部横幅与分类栏统一采用 `GridItemSpan(maxLineSpan)` 全宽跨度，自适应手机 2 列、大折叠屏/平板 3~4 列；
  - 二级抽屉统一使用 `SaltBottomSheet`，进场采用细腻物理弹簧 `spring(dampingRatio = 0.82f, stiffness = 380f)`，退场采用敏捷加速淡出；
  - 抽屉展开时主屏背景平滑失焦至 `14.dp` 原生高斯模糊（Android 12+ 硬件加速，低版本平滑降级），彻底剔除造成掉帧与视觉突兀的多余缩放内凹。

#### 3.3.4 关键容器防护、无障碍与系统交互契约 (Container Defense, Accessibility & System Contract)
- **混音滑块磨砂防护悬浮舱契约 (Frosted Icon Island Capsule Contract)**：
  - 混音抽屉 (`MixerBottomSheet`) 中垂直胶囊滑块 (`VerticalCapsuleSlider`) 全量接入该音效的专属自然拟物色彩作为充盈液体；
  - 底部包豪斯矢量图标设立 34.dp 正圆微岛屿悬浮舱：液面未漫过（`animatedFill < 0.16f`）使用默认透明背景；液面漫过（`animatedFill >= 0.16f`）自动切换为半透明深色磨砂底衬（`Color.Black.copy(0.32f)`）与 `0.5.dp` 柔光微描边（`Color.White.copy(0.18f)`），彻底隔绝高饱和度背景光（如雷雨闪电黄、林风纯绿），消除同色融化吃掉图标线条隐患，并与顶部百分比胶囊形成上下对称包豪斯秩序。
- **预设卡片声音基因微点硬性截断契约 (Preset Sound DNA Dots & Clamp Defense Contract)**：
  - 预设方案卡片紧随名称展示 5.dp 专属拟物色彩实心微点（`PresetSoundDnaDots`）；
  - 严格落地硬性封顶截断（Hard Clamp）：轨道数 $\le 4$ 时渲染 2~4 颗微点；轨道数 $> 4$ 时严格截断为前 3 颗微点 + 极简 `+N` 溢出角标，横向总占宽死死锁定在 22dp~35dp 以内；
  - 配合标题 `maxLines = 1, overflow = TextOverflow.Ellipsis` 双重防御，严禁任何因组合音效过多而拉长卡片或撑破横滑栏的视觉破窗。
- **系统无障碍大字号与长文本防挤压防御契约 (Accessibility Font Scaling & Ellipsis Defense Contract)**：
  - 水平排列容器（`Row`）中存在动态文本时，主标题/文本容器必须显式声明 `Modifier.weight(1f, fill = false)`；
  - 所有非段落展示的单行标签/标题必须硬性声明 `maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false`，严禁折行或撑破容器；
  - 右侧并排操作键或徽章必须设定最小保护尺寸（如关闭键固定 32dp）并保持 `Spacer(width = 8.dp)` 物理防撞避让。
- **触觉反馈分级分层契约 (Haptic Feedback Tier Contract)**：
  - **Level 1 轻量操作微震 (`TextHandleMove`)**：音效点按开关、分类胶囊切换、顶栏双胶囊点击、抽屉正圆关闭键；
  - **Level 2 状态咬合微震 (`LongPress`)**：卡片长按静止 220ms 激活调音模式、音量滑块推至 0% 或 100% 极值阻尼；
  - **Level 3 警示与破坏性二次确认 (`LongPress`)**：二次防误触抽屉中的“确认清空混音”或“确认重置伴眠统计”。
- **主流系统保活手风琴与包豪斯品牌矢量契约 (Keep-Alive Accordion & Brand Symbols Contract)**：
  - 落地二级防杀抽屉 (`KeepAliveGuideBottomSheet`)，顶置通用核心三板斧（多任务加锁、电池无限制、允许自启动）；
  - 5 大主流系统（小米/华为/OPPO/vivo/原生）采用默认折叠手风琴，折叠三角由 `spring(dampingRatio = 0.75f, stiffness = 380f)` 驱动 0° 到 90° 平滑旋转，内容由 `expandVertically() + fadeIn()` 弹性展开；
  - 品牌图标彻底规避商业商标侵权，采用纯 Canvas 点线面包豪斯重绘（小米实心 Squircle 橙底反白 mi、华为八瓣扇形花冠、OPPO 独立双 O 椭圆、vivo 实体速度 V、Android 官方开源小机器人）。
- **零网络权限与安全直达更新契约 (Zero Internet Permission & Safe Update Intent Contract)**：
  - 应用清单坚守零 `INTERNET` 权限原则，全功能 100% 离线运行；
  - 设置中心版本信息升级为可交互卡片，通过安全 Intent 调起系统外部浏览器直达 GitHub Releases (`/releases/latest`)，兼顾用户版本更新获取与极致的离线隐私安全口碑。

### 3.4 状态持久化契约
- 使用轻量 **Jetpack DataStore Preferences** 记录用户退出前的音轨音量状态与自定义场景预设；
- 预设等复杂对象结构使用官方 `kotlinx.serialization` 进行 JSON 序列化；拒绝引入 SQLite/Room 增加无谓构建负担。

### 3.5 伴眠统计与多轨时间流逝契约 (`PlaybackStatsTracker`)
- **真实物理时间对齐**：累计播放总时长（`totalSeconds`）按真实物理时间流逝累加（多轨混音并发播放 1 秒物理时间即计 1 秒，绝不按轨数倍增），并以每晚 8 小时为标准折算陪伴用户的沉睡夜晚数；单音轨独立累计各自然声的专属受宠时长。
- **电池防耗与闪存磨损保护（双缓冲防抖刷盘）**：
  - 播放过程中内存每秒累加暂存；
  - **周期批量落盘**：每 60 秒（1 分钟）自动触发一次轻量批量刷盘；
  - **事件驱动即时落盘**：暂停混音、清空混音、切换音轨、切歌或服务销毁时，立即触发即时 flush 确保零数据丢失；
  - **清空重置与二次确认**：提供安全二次防误触抽屉，重置时清空 DataStore 并重置内存计数器。
- **全站美学联动**：顶栏右上角并列双胶囊（`[ 统计 ] [ 设置 ]`）；统计抽屉采用 44sp Bento 统计大卡、月牙安睡徽章与诗意伴眠寄语；下方 30 款音效专属自然拟物色彩流光占比进度条与降序排行。

---

## 4. 阶段演进总账 (Stage Ledger)

### 4.1 历史基线归档 (Stages 1-23 Baseline Archive: v1.0.0 ~ v2.0.0)

> **注**：Stage 1 至 Stage 23 历次迭代已全面通过验证并固化为项目基础设施（包含底层音视频管线、SaltUI 体系、包豪斯矢量化、位图 Adaptive 图标重构与 v2.0.0 正式发布），紧凑归档如下：

- **Stage 1-5 (脚手架基线与首版发包 v1.0.0)**：打通 Gradle 8.9 + Kotlin 2.0.21 + SaltUI 3.x；落地 `AudioMixerEngine`（ExoPlayer 多轨并发池、集中音频焦点、平滑对数淡出）与 `WhiteNoiseMediaService` 前台保活；打通 Blanket 8 款自然音与 DataStore 状态记忆；完成 10 项单测并构建首版 APK。
- **Stage 6-9 (前台通知治理、抽屉重塑与大屏自适应 v1.6.1)**：阻断通知每秒重复推流；全站弹窗统一为大圆角 `SaltBottomSheet`；引入 CC0 慢波助眠「棕色噪音」与 5 维胶囊过滤芯片；首发自适应网格 `GridCells.Adaptive(160.dp)` 配合原生高斯模糊。
- **Stage 10-15 (锁屏黑胶、包豪斯矢量体系与声学生命力觉醒 v1.7.5)**：彻底消除粗糙白三角与 Emoji，锁屏/通知动态渲染 512×512 包豪斯黑胶声学大封面；15 款自然音由 `BauhausSoundIcon` 点线面接管；ExoPlayer 50ms 瞬发缓冲与防循环疲劳微动态；系统核心图标由 `BauhausUiIcon` 接管；实现非线性变奏弹簧与 Play/Pause 形态形变。
- **Stage 16-18 (折叠吸顶、纯实色悬浮舱与极简架构瘦身 v1.7.8)**：大标题随手势折叠，预设栏吸顶；方案库导入与单项恢复闭环；彻底剔除 Material3、media3-ui、espresso 净瘦身 600 行；预设卡片接入 `animateColorAsState` 与 `TextHandleMove` 微震。
- **Stage 19-21 (设置中心、音效扩充至 22 款与交互收敛 v1.9.1)**：落地 Bento 分组设置抽屉与物理开关 `SaltSwitch`（后台保活、屏幕常亮）；扩充 7 款自然音至 22 款并配置等能量交叉淡化无缝循环；收敛关于抽屉与包豪斯微光细胶囊滚动条。
- **Stage 22-23 (开源合规、工业级位图+Adaptive重构与发布 v2.0.0 正式版)**：补齐分层 MIT LICENSE 与 SOUNDS_LICENSING.md；剖析跨进程 loadIcon 栅格化降级白三角机理，落地原生纯色背景 (@color/ic_launcher_background: #11151F) + 全密度透明琴弦位图 + mdpi~xxxhdpi 全密度整图；发布 v2.0.0 (versionCode 22)。

---

### 4.2 最新演进记录 (Active Stages: v2.1.0 ~ v2.4.0)

| 阶段 | 交付核心目标 | 状态 | 关键交付与验证标准 |
| :--- | :--- | :---: | :--- |
| **Stage 24** | 主流系统防杀保活指引、包豪斯品牌几何矢量重绘与免联网直达更新 (v2.1.0) | **[x] 已达成** | 落地 `KeepAliveGuideBottomSheet` 覆盖小米/华为/OPPO/vivo/原生 5 大系统默认折叠保活手风琴；自绘 5 款纯几何包豪斯品牌矢量图标（小米 Squircle 橙底反白 mi、华为八瓣扇形花冠、OPPO 双 O 独立椭圆、vivo 实体速度 V、Android 官方小机器人）；设置抽屉新增防杀指引微胶囊；版本信息升级为可交互卡片，安全 Intent 调起浏览器直达 GitHub Releases (`/releases/latest`)，坚守 0 网络权限；76 项单测与 Release 构建 100% 全绿。 |
| **Stage 25** | 分类切换网格卡片平滑物理重排与进退场动效 (v2.1.1) | **[x] 已达成** | 基于 Compose 1.7+ `LazyGridItemScope.animateItem` 落地网格卡片磁吸物理重排动效；150ms 敏捷退场淡出 + 0.82 阻尼比高刚度物理弹簧位移 + 180ms 进场淡入；升级 versionCode 24, versionName 2.1.1；76 项单测与 Release 签名构建全绿。 |
| **Stage 26** | 单手轻扫切分类、胶囊柔光呼吸与卡片长按左右滑动调音 (v2.2.0) | **[x] 已达成** | 主网格单手中下部左右轻扫切换分类；分类胶囊 180ms 柔光呼吸渐变与自动滚动视口；卡片长按 220ms 咬合微震并左右拖动调音（胶囊放大 1.18×、卡片下沉 0.97×，极简纯净无多余横条）；92 项单测全绿，Release 打包全绿。 |
| **Stage 27** | 全量扩充 8 款白噪音音效至 30 款、包豪斯点线面矢量符号与开源致谢完善 (v2.3.0) | **[x] 已达成** | 扩充雨水类(+2: 车顶雨声/伞面雨声)、自然类(+3: 猫咪呼噜/颂钵冥想/雪地漫步)、生活类(+3: 客机巡航/纸张翻动/黑胶唱片)至 30 款高品质 96k OGG；纯 Compose Canvas 手绘 8 款包豪斯点线面拟物双色矢量与动态形态展开；测算毫秒时长表并纳入防循环疲劳引擎；设置与关于抽屉同步更新 30 款与开源许可致谢清单；SOUNDS_LICENSING.md 完整归档；92 项单测 100% 全绿，Release 构建全绿。 |
| **Stage 28** | 伴眠统计中心、安睡夜晚折算、混音专属色滑块磨砂悬浮舱与声音基因微点 (v2.4.0) | **[x] 已达成** | 顶栏双胶囊 `[ 统计 ] [ 设置 ]`；落地 `PlaybackStatsBottomSheet`（44sp 小时大卡、8h/晚安睡夜晚折算、欧几里得双圆月牙与微星、30 款专属色彩流光条与降序偏好榜）；`PlaybackStatsTracker` 内存累加 + 60s 周期刷盘与即时 flush 防闪存磨损；混音滑块专属色充盈 + 磨砂防护悬浮舱彻底根治同色吃图标；预设卡片「声音基因微点」硬性封顶截断 (Hard Clamp $\le 4$) 杜绝拉长；102 项单测 100% 全绿，Release 构建全绿。 |

---

## 5. 防踩坑与 Android 关键红线 (Android Pitfalls Guardrails)

1. **Gradle 路径反斜杠转义与 JAVA_HOME**：
   - Windows 下 `local.properties` 的 SDK 路径必须正确转义反斜杠，例如：`sdk.dir=C\:\\Program Files (x86)\\Android\\android-sdk`。
   - 必须确保 `JAVA_HOME` 指向真实 JDK 21 目录（短路径 `C:\PROGRA~2\Android\openjdk\jdk-21.0.8`）。
   - `gradlew.bat` 已植入 fallback 检测逻辑，当 `%JAVA_HOME%` 无效时自动重定向至 `C:\PROGRA~2\Android\openjdk\jdk-21.0.8`。
2. **Compose 2.0 编译器**：
   - 使用 Kotlin 2.0 时，Compose 编译器已内置于 Kotlin 仓库，严禁在 `build.gradle.kts` 添加已废弃的 `composeOptions { kotlinCompilerExtensionVersion = ... }`。
3. **Android 14 (API 34+) 前台服务类型**：
   - `AndroidManifest.xml` 中声明 `Service` 时，必须显式附加 `android:foregroundServiceType="mediaPlayback"`，并同时申请 `<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />` 与 `<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />`。
4. **SaltUI 3.x 构建兼容性**：
   - 在 AGP 8.7+ 下必须在 `app/build.gradle.kts` 添加：`tasks.matching { it.name.contains("AarMetadata") }.configureEach { enabled = false }` 规避过高的 API 约束；
   - Kotlin 编译任务在 `compilerOptions.freeCompilerArgs` 添加 `"-Xskip-metadata-version-check"`。
5. **内存泄漏与播放器释放**：
   - Service 销毁、Activity 销毁或切换时，必须显式调用 `ExoPlayer.release()` 彻底释放底层 AudioTrack 与编解码资源，严禁静默泄露。
6. **多轨禁用 Audio Offload**：
   - 多轨并发混音切勿开启 `setOffloadedPlayback(true)`，硬件 DSP 仅支持单流 Offload，多流会导致解码失败或静音。
7. **禁用 AAC 循环与子播放器抢焦点**：
   - 循环音源必须使用 OGG/Opus，规避 AAC 首尾卡顿；所有子 ExoPlayer 必须设置 `handleAudioFocus = false`，统一由顶层单点处理焦点。
8. **自适应网格 Span 规范**：
   - 使用 `GridCells.Adaptive` 时，全宽通栏组件必须使用 `GridItemSpan(maxLineSpan)`，严禁硬编码 `GridItemSpan(2)`，否则在大屏/平板上会导致右侧出现空白断层。
9. **Media3 LoadControl 独立实例与多线程亲和性**：
   - Media3 的 `DefaultLoadControl.onPrepared` 会断言 `threadId == -1 || threadId == currentThreadId`。多音轨并发池中每个 `ExoPlayer` 运行于独立的后台回放线程，**严禁将同一个 `LoadControl` 单例注入多个播放器**，必须使用工厂方法 `createLowLatencyLoadControl()` 为每个播放器分配独立实例，否则会导致首个准备的播放器独占线程、后续其他音轨全部抛出 `IllegalStateException` 哑音。
10. **应用图标全密度 Legacy 位图与系统设置应用信息兼容**：
    - Android 8.0+ 引入的 `res/mipmap-anydpi-v26/` 优先级**无条件高于**具体密度目录。若 `ic_launcher.xml` 内部直接引用了包含复杂曲线的 `<vector>` 作为 background 和 foreground，桌面 Launcher 可以正常绘制，但定制系统设置/手机管家（MIUI/HyperOS 的 IconCustomizer）在跨进程截取应用大图标时，在非 Activity 上下文无法获取确定 intrinsic 尺寸或强转 BitmapDrawable 失败，会抛出异常并自动降级为系统媒体类应用占位图标（灰色圆底白三角播放器）。
    - 工业级根治标准：遵循成熟商业 App 规范，`mipmap-anydpi-v26/ic_launcher.xml` 中 `<background>` 必须指向原生系统纯色（`@color/ic_launcher_background`），`<foreground>` 必须指向各像素密度提供透明通道的标准物理位图（`@mipmap/ic_launcher_foreground`），并同时在各 density 提供合成好的 `ic_launcher.png` 和 `ic_launcher_round.png`，彻底杜绝任何跨进程矢量栅格化异常。

---

## 6. Git 规范与交付标准 (Git & Commit Standards)

- **严禁全量盲推**：非干净工作树严禁 `git add -A`，仅暂存已完成清单内的文件。
- **提交信息格式**：
  `[Feature/Fix/Docs/Chore] 中文标题 (English Title)`
  正文清晰写明包含改动与验证依据，严禁虚假声称已通过未执行的测试。
- **发布与版本规范 (Release Workflow)**：
  - 严禁擅自刷版：发版前必须在对话中清晰列举自上一发布版本以来的所有改动清单，供用户逐项核对与确认；
  - 标准发版仅在用户明确回复同意后执行：更新版本号、打标签、推送并上传 Release 安装包。
