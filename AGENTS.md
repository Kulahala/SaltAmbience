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

### 3.3 椒盐美学 UI 规范 (`SaltUI`)
- **视觉风格**：清爽克制、低饱和度、大圆角卡片、清晰的分组布局。
- **包豪斯声学极简矢量符号与拟物色彩语义**：全站废除彩色 Emoji，统一由纯几何点、线、面构成的 `BauhausSoundIcon` 接管；未激活时呈现 48% 柔光呼吸微色（`toIdlePalette(0.48f)`），激活时映射真实自然声学意象（溪流上白下水蓝、篝火烈焰橙红+火星金黄、雷雨高能电光黄+暴雨白、林风苍翠绿、夏夜月牙金+静谧夜紫、海浪深海蔚蓝、粉噪柔粉、棕噪大地暖褐等）并跃升至 100% 高饱和双色高光；底栏播放条做减法移除重复混音按钮，右侧独占动态倒计时胶囊。
- **全站 UI 核心系统图标包豪斯矢量化与常驻色彩契约 (`BauhausUiIcon`)**：全站播控（Play/Pause）、控制中心清空（Clear）、主题模式（ThemeSystem/Light/Dark）、定时胶囊（Timer）、添加/恢复（Add/Restore）、弹窗关闭与勾选（Close/Check）全面接管，杜绝任何系统 Emoji 与 Unicode 字符 Hack；接入专属 `BauhausUiTheme` 常驻双色语义，严禁全局强制染灰；控制中心停止按钮精准定名为「清空混音」，杜绝交互误导。
- **包豪斯声学生命力觉醒与非线性形态形变契约 (Bauhaus Acoustic Morphing Contract)**：
  - **矩阵素描与高光反差**：音效矩阵未激活音标与未选中分类胶囊常驻纯粹的素描黑白灰阶（Monochrome Idle），全局其他系统图标（Timer/Clear/Theme 等）保持专属常驻语义色彩；音效激活或分类选中时平滑跃升至 100% 拟物自然双色；
  - **速率变奏物理弹簧与生命觉醒**：音效点亮时由非线性变奏弹簧 `spring(dampingRatio = 0.58f, stiffness = 320f)` 驱动微缩放呼吸与 15 款音效几何形态专属展开插值（雨丝拉长滑落、闪电劈裂激射、风浪流动、溪流跃浪、火星升腾、声谱条跳跃等），赋予激活瞬间的有机生命力；
  - **播控无缝几何形态形变 (`BauhausPlayPauseMorphIcon`)**：底栏与控制中心播放/暂停按钮告别硬切，基于弹性物理弹簧驱动三角形（Play 锋利汇聚）与对称双矩形柱（Pause 垂直挺立）之间的实时无缝分裂与聚合插值。
- **顶部分类导航微光胶囊契约**：分类芯片（全部/雨水/自然/生活/纯噪）未选中时常驻中性素描灰阶，选中时跃迁至专属自然语义色（16% 微光背景与 55% 强调边框）；支持浅色模式高对比度深色阶映射（`getContentColor(isDark)`），强光直射清晰可辨。
- **色彩与层级契约 (Color Tokens Contract)**：
  - **主底色（Level 0 主屏幕背景）**：必须使用 `SaltTheme.colors.background`（浅色为纯白 `#FAFAFA`，深色为 `#121212`），严禁在根容器滥用 `subBackground` 导致全局发灰；
  - **容器底色（Level 1 卡片与抽屉）**：统一使用 `SaltTheme.colors.subBackground`（浅色为浅灰 `#F3F4F6`，深色为半透明白 `#FFFFFF14`），配合大圆角（`16.dp`~`20.dp`）；
  - **文字阶梯规范**：主标题/正文使用 `text`，次级信息使用 `text.copy(alpha = 0.65f)`，失焦提示使用 `text.copy(alpha = 0.40f)`；严禁在卡片上直接绘制 `subText` 造成灰底灰字；
  - **抽屉面板立体质感**：底部抽屉采用玄武岩冷炭黑（`#1B1D24`），搭配 `1.dp` 柔光描边（`Color.White.copy(0.08f)`）拉开纵深。
- **动效与虚化契约**：
  - **二级抽屉**：统一使用 `SaltBottomSheet`，进场采用细腻物理弹簧 `spring(dampingRatio = 0.82f, stiffness = 380f)`，退场采用敏捷加速淡出；
  - **原生背景高斯虚化 (Backdrop Blur)**：抽屉展开时主屏背景平滑失焦至 `14.dp`（Android 12+ 硬件加速，低版本平滑降级），彻底剔除造成掉帧与视觉突兀的多余缩放内凹。
- **大屏自适应网格契约**：
  - 音效矩阵采用 `GridCells.Adaptive(minSize = 160.dp)`；
  - 所有头部横幅与分类栏统一采用 `GridItemSpan(maxLineSpan)` 全宽跨度，自适应手机 2 列、大折叠屏/平板 3~4 列。
- **包豪斯折叠吸顶与防叠字纯实色悬浮舱契约 (Collapsible Sticky Header & Anti-Bleed Floating Dock Contract)**：
  - **大标题动态平滑折叠**：网格向上滚动时，可折叠大标题与场景方案行（90.dp）随网格滚动距离 1:1 动态收缩淡出至 0.dp，两相联动丝滑无突变；
  - **预设横滑栏永远稳固吸顶**：下部预设横滑栏（固定 54.dp）永远吸附在状态栏正下方（`statusBarsPadding()`），不设负 offset、不做外部截断，无论滚动到多深随时横滑切换混音；
  - **预设操作单点归一与官方库收纳**：主屏预设横滑栏做极致减法，彻底移除低频冗余的“恢复默认”胶囊，仅保留 `[方案卡片] ... [+ 存为预设] [📥 导入]`；将默认预设找回、单项恢复与官方方案库深度收纳进“导入”弹窗，已存在方案轻触提示“无需重复添加”，误删方案轻触即刻单项找回；
  - **纯实色防叠字悬浮舱**：顶栏与底栏播放条全面采用 100% 纯实色（`SaltTheme.colors.background` 与 `Color(0xFF1E2026)`），搭配 1.dp 精致边缘微描边与柔和阴影，彻底摒弃不稳定的外部 alpha 模糊库，杜绝字体重叠透底与崩溃闪退。
- **应用图标契约 (Adaptive Icon Contract)**：
  - 落地 Android 8.0+ 官方自适应矢量图标「包豪斯声学 · 琴弦点线面」(D-14) 为主图标（`res/drawable/` + `res/mipmap-anydpi-v26/`），配套 `ic_launcher_monochrome.xml` 支持 Android 13+ 壁纸动态取色；归档 D-16 备选。

### 3.4 状态持久化契约
- 使用轻量 **Jetpack DataStore Preferences** 记录用户退出前的音轨音量状态与自定义场景预设；
- 预设等复杂对象结构使用官方 `kotlinx.serialization` 进行 JSON 序列化；拒绝引入 SQLite/Room 增加无谓构建负担。

---

## 4. 阶段演进总账 (Stage Ledger)

### 4.1 历史基线归档 (Stages 1-7 Baseline Archive)

> **注**：Stage 1 至 Stage 7 历次迭代已全面通过验证并固化为项目基础设施，合并不赘述过程：

- **Stage 1 (脚手架基线)**：打通 Gradle 8.9 + Kotlin 2.0.21 + SaltUI 3.x 依赖，解决 `minCompileSdk=37` 与 Windows 短路径语法兼容，生成空壳 APK。
- **Stage 2 (引擎与服务)**：落地 `AudioMixerEngine`（ExoPlayer 多轨并发池、集中音频焦点、平滑对数淡出）与 `WhiteNoiseMediaService`（前台保活、常驻媒体通知卡片）。
- **Stage 3 (SaltUI 界面)**：打通 `HomeScreen`、`SoundCard`、`BottomPlayerBar`、`MainViewModel` 状态流。
- **Stage 4 (资产与预设)**：接入 Blanket 8 款无缝自然音 OGG，打通 DataStore 记忆混音与预设状态，修复首次订阅状态覆写竞态 Bug。
- **Stage 5 (验收与收官)**：通过 10 项核心单测，构建完整全量 APK，初始化 Git 仓库。
- **Stage 6 (P0 核心修复)**：阻断通知每秒重复推流（`distinctUntilChanged`）与休眠无效计算；修复暂停文案与音频焦点释放，22 项单测全绿。
- **Stage 7 (P1 抽屉重塑)**：全站弹窗统一为大圆角底部抽屉规范（`SaltBottomSheet`）并接入下拉拖拽手势；解决输入法软键盘挤压，25 项单测全绿。

---

### 4.2 最新演进记录 (Active Stages)

| 阶段 | 交付核心目标 | 状态 | 关键交付与验证标准 |
| :--- | :--- | :---: | :--- |
| **Stage 8** | 音效分类导航、棕噪扩充与预设全自由管理 (v1.6.0) | **[x] 已达成** | 自定义预设置顶与最新倒序排列；默认预设支持软删除与一键“↺ 恢复默认”；引入 CC0 慢波助眠「棕色噪音」扩充至 15 款音源；音效矩阵 5 维胶囊过滤芯片；33 项单测 100% 全绿。 |
| **Stage 9** | 视觉交互闭环、图标重塑与大屏自适应 (v1.6.1) | **[x] 已达成** | 落地包豪斯自适应图标 (D-14) 与 Android 13+ 动态取色；实色悬浮舱防叠字（130.dp 安全避让）；顶栏手势自然滚动；抽屉物理弹簧动效与玄武岩冷炭黑；移除多余缩放内凹并保留 14.dp 原生高斯模糊；顶栏极简减法（左上角微版本号触感入口）；全站多设备自适应 `GridCells.Adaptive(160.dp)` 配合 `GridItemSpan(maxLineSpan)`；33 项单测全绿。 |
| **Stage 10** | 锁屏通知包豪斯黑胶封面、声学极简矢量符号、低延迟瞬发引擎与防循环疲劳声学动态 (v1.7.0) | **[x] 已达成** | 彻底消除锁屏通知粗糙白三角，动态渲染注入 512x512 包豪斯黑胶声学艺术大封面与微小单色图标；15 款自然音全站废弃拟物 Emoji，由包豪斯声学极简矢量符号 (`BauhausSoundIcon`) 统一驱动；ExoPlayer 定制 50ms 缓冲策略 + 0ms 乐观响应 + 串行异步音频焦点治理消除体感半秒延迟；落地方案 A（起播随机时间戳偏置）与方案 B（±2% 自然微速差重采样），彻底消除长时播放循环疲劳；48 项单测 100% 全绿。 |
| **Stage 11** | 定时器闪退根治、拟物语义专属配色、播控条精简与系统原生倒计时联动 (v1.7.1) | **[x] 已达成** | 根除 `String.format` 字符百分号插值崩溃，抽取纯 Kotlin 安全倒计时工具；15 款音标与锁屏黑胶封面全量落地拟物语义配色（篝火烈焰红橙+金星、雷雨电光黄、林风苍翠绿等）；播控条移除冗余混音按钮并落地实时倒计时高亮胶囊；通知栏与锁屏接入 Android 原生 `Chronometer` 硬件级秒级倒计时；52 项单测 100% 全绿。 |
| **Stage 12** | LoadControl 独立实例工厂根治跨轨哑音、通知单轨直显与分钟级跳变驱动 (v1.7.2) | **[x] 已达成** | 彻底根除 Media3 DefaultLoadControl 单线程亲和性断言导致的并发哑音，采用工厂构建独立实例；通知副文本单轨直显音效名并消除锁屏截断折叠；通知栏精准分钟级跳变更新；56 项单测 100% 全绿。 |
| **Stage 13** | 控制中心文案重塑为「清空混音」、全站 UI 核心系统图标包豪斯矢量化 (v1.7.3) | **[x] 已达成** | 控制中心按钮彻底消除歧义重塑为「清空混音」与 Play/Pause；全站上线统一的 `BauhausUiIcon` 纯几何 Canvas 矢量体系（Play、Pause、Clear、ThemeSystem、ThemeLight、ThemeDark、Close、Check、Timer、Add、Restore、Import 等 12 类符号），彻底消除粗糙的系统 Emoji 与 Unicode 字符 Hack；63 项单测 100% 全绿。 |
| **Stage 14** | 包豪斯常驻语义色彩体系、呼吸微色 (Tinted Idle) 与分类自然微光芯片 (v1.7.4) | **[x] 已达成** | 彻底告别黑白线框灰暗感；UI 矢量图标全面接入专属常驻双色；顶部分类胶囊常驻自然原色与微光底色；15 款音效卡片落地 48% 呼吸微色；66 项单测 100% 全绿。 |
| **Stage 15** | 音效矩阵黑白回归、非线性变奏声学 Morphing 与播控形态切换 (v1.7.5) | **[x] 已达成** | 音效矩阵与分类芯片恢复未激活素描黑白灰阶、激活平滑跃升自然色彩；15 款音效落地非线性变奏物理弹簧 `spring` 驱动的呼吸与形态展开动画；实现 Play/Pause 纯几何实时无缝分裂与聚拢形态形变组件 (`BauhausPlayPauseMorphIcon`)；68 项单测 100% 全绿。 |
| **Stage 16** | 顶栏折叠吸顶、纯实色悬浮舱与方案库导入闭环 (v1.7.6) | **[x] 已达成** | 彻底根除外部 alpha 模糊库闪退与负 offset 截断 bug；大标题随手势 1:1 动态收缩折叠；预设横滑栏（固定 54.dp）永远稳固吸顶；主屏移除冗余“恢复默认”，将其与官方方案库深度收纳进导入弹窗（支持已存在提示与单项/全量找回）；顶栏与播控条落地 100% 纯实色防叠字悬浮舱；全站 69 项单测 100% 全绿。 |
| **Stage 17** | Ponytail 极简架构瘦身、剥离 Material3/Media3-UI 与抽屉容器收敛 (v1.7.7) | **[x] 已达成** | 彻底剔除 `material3`、`media3-ui`、`espresso` 等 4 个非必要依赖；自研轻量 `SaltHorizontalSlider` 替代 Material3 Slider；收敛 `MixerBottomSheet` 与 `SleepTimerBottomSheet` 到统一 `SaltBottomSheet`；净消减逾 600 行代码，全站测试与打包 100% 全绿。 |

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

---

## 6. Git 规范与交付标准 (Git & Commit Standards)

- **严禁全量盲推**：非干净工作树严禁 `git add -A`，仅暂存已完成清单内的文件。
- **提交信息格式**：
  `[Feature/Fix/Docs/Chore] 中文标题 (English Title)`
  正文清晰写明包含改动与验证依据，严禁虚假声称已通过未执行的测试。
- **发布与版本规范 (Release Workflow)**：
  - 严禁擅自刷版：发版前必须在对话中清晰列举自上一发布版本以来的所有改动清单，供用户逐项核对与确认；
  - 标准发版仅在用户明确回复同意后执行：更新版本号、打标签、推送并上传 Release 安装包。
