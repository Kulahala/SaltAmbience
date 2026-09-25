# SaltAmbience (椒盐白噪音)

一款遵循 SaltUI 椒盐美学规范、基于 AndroidX Media3 与 Jetpack Compose 开发的原生白噪音混音应用。完全离线运行，零广告，无网络权限。

> **致敬与说明**：  
> 本项目为个人开源作品，界面基于 **Moriafly** 的开源组件库 **SaltUI** 开发。本项目与「椒盐音乐 (Salt Player)」及 Moriafly 团队无商业附属关系，感谢 Moriafly 对开源社区的贡献。

---

## 主要特性

### 1. 界面与交互
- **纯几何矢量图标**：全站没有第三方 Emoji，所有音效和控制图标均由 Compose Canvas 直接绘制。
- **动态自适应布局**：采用自适应网格，手机竖屏为 2 列，平板、折叠屏或横屏自动扩展为 3 至 4 列。
- **预设栏吸顶**：上滑列表时大标题自动折叠，场景预设栏固定在顶栏下方，方便随时切换混音。
- **单手轻扫切换分类**：支持在屏幕中下部左右轻扫顺畅切换音效分类，分类胶囊具备 180ms 柔光呼吸变色与自动视口跟随。
- **分类磁吸平滑重排**：切换音效分类时，卡片采用非线性物理弹簧平滑重排与淡入淡出，告别生硬硬切。
- **深浅色主题**：支持跟随系统或手动切换浅色/深色模式。

### 2. 多轨混音与音频设计
- **卡片长按即调音**：无需进入二级菜单，长按正在播放的卡片 220ms 触发坚实微震，直接左右滑动即可精细调节单轨音量，右上角百分比气泡实时缩放反馈，松手即锁存。
- **30 款无缝自然音**：涵盖雨水、林风、篝火、海浪、风扇、键盘、钟表、车顶雨声、伞面雨声、猫咪呼噜、颂钵、雪地漫步、客机、纸张、黑胶及科学绿噪等，音频均已做采样级交叉淡化无缝循环处理。
- **防循环疲劳**：起播时带有微小的时间戳随机偏移，并施加 ±2% 的自然微速差重采样，避免长时播放产生明显的机械循环感。
- **多轨独立音量**：基于 Media3 ExoPlayer 实例池，每路音轨独立调节音量和静音，走系统 PCM 混音。
- **平滑淡出休眠**：休眠倒计时结束前几分钟采用平滑曲线淡出至静音，避免突然停播爆音。
- **前台保活与音频焦点**：通知栏常驻控制，耳机拔出自动暂停，来电或语音播报时自动避让。

### 3. 预设与口令分享
- **混音方案存储**：内置多种经典场景方案，支持将当前搭配保存为自定义预设，支持删除二次确认。
- **一键口令导入导出**：可将混音配置复制为 Base64 文本口令分享给他人，打开应用即可自动识别剪贴板一键载入。

### 4. 权限、保活与隐私
- **零网络权限**：Manifest 清单中未申请 `INTERNET` 权限，完全离线运行，不收集任何数据，无第三方 SDK。
- **主流系统保活指南**：内置小米澎湃、华为鸿蒙、OPPO ColorOS、vivo OriginOS 及原生系统专属防杀指引手风琴，配合包豪斯纯几何品牌矢量符号。
- **纯离线更新跳转**：在坚持零联网权限的前提下，通过系统 Intent 直接调起浏览器访问 GitHub Releases，兼顾纯净安全与版本获取。
- **屏幕常亮开关**：夜间放在床头或桌面时可选择保持屏幕常亮。

---

## 技术选型

| 层次 / 模块 | 技术栈 | 说明 |
| :--- | :--- | :--- |
| **开发语言** | Kotlin 2.0.21 | 官方 Compose 编译器插件，协程与 Flow |
| **构建系统** | Gradle 8.9 + AGP 8.7.2 | Version Catalogs 统一依赖版本管理 |
| **系统基线** | Android 8.0+ (Min SDK 26) | Target & Compile SDK 35 (Android 15) |
| **UI 框架** | SaltUI 3.x + Jetpack Compose | 椒盐设计风格，原生圆角抽屉 |
| **音频引擎** | AndroidX Media3 1.4.1 (ExoPlayer) | 多轨播放器池，禁用 Audio Offload 走系统混音 |
| **前台服务** | MediaSessionService | 锁屏媒体控制、通知栏与音频焦点 |
| **数据持久化** | Jetpack DataStore Preferences | 混音状态记忆与预设存储 |
| **JSON 解析** | Kotlinx Serialization | 轻量类型安全序列化 |

---

## 音源授权

全量 30 款自然音源来自 [Blanket](https://github.com/rafaelmardojai/blanket)、[Moodist](https://github.com/remvze/moodist)、Wikimedia Commons 及 Freesound 等开源与公有领域贡献。

详细的作者署名、原音频链接及 CC / Pixabay 许可条款见 [SOUNDS_LICENSING.md](SOUNDS_LICENSING.md)。

---

## 本地编译

需要提前配置 JDK 21 和 Android SDK (API 35)。

```bash
# 运行单元测试
./gradlew testDebugUnitTest

# 编译 Release 安装包
./gradlew assembleRelease
```

编译产物位于：`app/build/outputs/apk/release/app-release.apk`。

---

## 开源协议

本项目源码基于 [MIT License](LICENSE) 开源。  
音频资源保留各自独立的许可，详见 [SOUNDS_LICENSING.md](SOUNDS_LICENSING.md)。
