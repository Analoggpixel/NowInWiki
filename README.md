# NowInWiki（基于 Now in Android）

非官方、只读的 Wikipedia / MediaWiki 客户端，版本 **0.1.0**。

本仓库：https://github.com/Analoggpixel/NowInWiki  

本仓库由 [Now in Android](https://github.com/android/nowinandroid) 改造而来，用于学习 Android 架构，并验证「搜索 → 阅读 → 收藏 → 历史」最小闭环。**与 Wikimedia Foundation、维基百科无任何隶属、背书或赞助关系。**

> English: An unofficial, read-only Wikipedia client forked/adapted from Now in Android. Not affiliated with Wikimedia.

---

## 功能（v0.1.0）

- 按关键词搜索 Wikipedia 条目（多语言偏好）
- 打开并阅读词条正文（WebView）
- 收藏与文件夹
- 最近浏览记录
- 设置：主题、动态色、正文字号、默认语言
- 关于页：非官方声明、内容许可、隐私说明、开源说明

## 不是什么

- 不是 Wikipedia / Wikimedia 官方应用
- 不是可编辑、讨论、登录的社区客户端
- 不是完整的 Now in Android 新闻示例（Topics / News / Sync 等已移除）

---

## 环境与运行

- Android Studio（建议最新稳定版）
- JDK 17+
- 可访问 Wikipedia / Wikimedia API 的网络环境

导入本仓库后，选择模块 `app`，推荐运行变体：

```text
demoDebug
```

命令行示例：

```bash
./gradlew :app:assembleDemoDebug
```

> 说明：项目仍保留 NiA 时期的 flavor / 模块结构；日常开发以 `demoDebug` 为准。部分旧截图测试、benchmark 可能仍带有 NiA 痕迹，不作为本客户端功能保证。

---

## 技术概要

- Kotlin、Jetpack Compose、Material 3
- Hilt、Navigation 3、Room、DataStore、Retrofit / OkHttp、Coil
- 模块化结构继承自 Now in Android（详见上游文档）

上游学习文档（架构参考，描述的是原 NiA，不完全等同于本客户端）：

- [Architecture learning journey](docs/ArchitectureLearningJourney.md)
- [Modularization learning journey](docs/ModularizationLearningJourney.md)

产品向说明见：[Wiki App V1 需求文档](docs/Wiki-App-V1-需求文档.md)

---

## 数据与协议

### 运行时内容（词条正文等）

展示内容来自各语言维基百科 / MediaWiki，通常适用
[CC BY-SA](https://creativecommons.org/licenses/by-sa/4.0/deed.zh)
（以各页面标注为准）。使用本应用不改变内容本身的许可；请按许可要求署名。

应用内「关于 → 内容许可」有对应说明。网络请求遵循 [Wikimedia User-Agent 政策](https://foundation.wikimedia.org/wiki/Policy:Wikimedia_Foundation_User-Agent_Policy)：使用可识别的应用名 + **公开联系 URL**（不要用个人邮箱）。

默认联系 URL（Wikimedia User-Agent）在 `gradle.properties` 的 `wiki.contact.url`，当前为：

https://github.com/Analoggpixel/NowInWiki/issues

也可在本机 `local.properties`（勿提交）覆盖：

```properties
WIKI_CONTACT_URL=https://github.com/Analoggpixel/NowInWiki/issues
```

### 本仓库源代码

源代码以 [Apache License 2.0](LICENSE) 分发。基于 Now in Android 的部分请保留其版权与许可声明。

「Wikipedia」「Wikimedia」等为相关权利人商标。

---

## 已知限制（初版）

- 搜索结果分页 / Action API 增强等尚未完善
- 部分 UI 文案与模块命名仍带有 NiA 历史痕迹
- `applicationId` 暂仍为上游包名，后续可能调整
- README / 文档中的旧 NiA 截图不代表当前界面
- 联系与反馈：https://github.com/Analoggpixel/NowInWiki/issues

---

## 致谢

- [Now in Android](https://github.com/android/nowinandroid) — Android 官方示例应用（Apache-2.0）
- [Wikipedia / MediaWiki](https://www.mediawiki.org/) — 公开内容与 API
- 所有按开放许可贡献百科内容的作者与社区

---

## License

```text
Copyright 2022 The Android Open Source Project
Additional changes for this Wikipedia reader adaptation.

Licensed under the Apache License, Version 2.0.
See LICENSE for the full text.
```
