# 本地提交说明：Wiki Search Suggestions

日期：2026-07-30
用途：本地 commit 节点说明，不作为 PR 描述直接使用
状态：阶段性记录

## 这份文档和 PR 的区别

- 这份文档是给本地提交留档用的，重点是记录“这一批代码到底改了什么”
- PR 描述是给代码评审看的，通常会更短、更聚焦，也会强调验证方式和风险
- 后面如果要提 PR，可以从这份文档提炼出 `Why / What changed / How to test / Notes`

## 本次改动目标

为搜索页搭建第一版 Wikipedia 搜索推荐链路，先完成：

- 输入关键词
- 请求 Wikipedia 推荐接口
- 返回推荐列表
- 渲染标题、描述、摘要片段、缩略图

当前阶段暂不做：

- 点击推荐项进入详情页
- 收藏 / 历史 / 设置联动
- 旧搜索功能彻底删除

## 本次改动内容

### 1. Network 层

新增 Wikipedia 搜索推荐相关 DTO：

- `NetworkWikiSuggestionsResponse`
- `NetworkWikiSuggestionItem`
- `NetworkWikiThumbnail`

新增：

- `WikipediaNetworkDataSource`
- `RetrofitWikipediaNetwork`

并通过 `BuildConfig.WIKIPEDIA_BASE_URL` 读取 Wikipedia 基础地址。

### 2. Model 层

新增内部稳定模型：

- `WikiSuggestionItem`
- `WikiSuggestionsResult`

作用：

- 隔离 Wikipedia 原始 JSON
- 让上层 UI 不直接依赖网络返回结构

### 3. Data 层

新增 mapper：

- `NetworkWikiSuggestionItem -> WikiSuggestionItem`
- `NetworkWikiSuggestionsResponse -> WikiSuggestionsResult`

新增：

- `WikiSuggestionRepository`
- `DefaultWikiSuggestionRepository`

### 4. Domain 层

新增：

- `GetWikiSuggestionsUseCase`

当前 use case 只做基础 query 清洗（如 `trim()`），后续可继续承接：

- query 规范化
- 结果过滤
- 多数据源切换

### 5. Hilt 注入

新增 / 修改：

- `WikipediaNetworkModule`
- `DataModule` 中对 `WikiSuggestionRepository` 的绑定

打通依赖链：

- `WikipediaNetworkDataSource <- RetrofitWikipediaNetwork`
- `WikiSuggestionRepository <- DefaultWikiSuggestionRepository`

### 6. Search Feature

新增：

- `SearchSuggestionUiState`
- `SearchSuggestionViewModel`
- `SearchSuggestionsScreen`

并把推荐区块最小接入现有 `SearchScreen`。

### 7. 搜索触发策略

从“每个字符输入后立即发请求”改为：

- `onQueryChanged()` 只更新 query
- 由 ViewModel 监听 query 流
- 对 query 做 `trim`
- `distinctUntilChanged`
- `debounce(300ms)`
- 非空后再请求

同时取消了“至少 2 个字符”限制，以适配中文单字符搜索场景。

### 8. 缩略图处理

Wikipedia 返回的部分缩略图 URL 为协议相对地址：

- `//upload.wikimedia.org/...`

因此新增：

- `toAbsoluteWikiUrl()`

用于在 mapper 中将其转成：

- `https://upload.wikimedia.org/...`

从而让图片加载器能够正常请求。

### 9. Wikimedia User-Agent 处理

为外部 Wiki 请求补充了自定义 `User-Agent`，以满足 Wikimedia 的访问要求。

同时加了临时调试日志，用于验证：

- 请求是否真的发出
- `User-Agent` 是否附加
- suggestions 链路是否贯通

这些调试日志在连通性确认后应删除。

## 当前效果

已验证到的效果：

- 能发起 Wikipedia 搜索推荐请求
- 推荐结果可以展示
- 缩略图现在可以加载出来
- 输入时的闪烁已经因 debounce 明显减少

## 当前未完成项

- 推荐项点击行为仍为空实现
- 旧搜索链路只是临时隐藏，未彻底清理
- Suggestion 功能还未补测试
- 与正式搜索结果页的关系尚未重新梳理

## 风险和注意事项

- 当前链路仍带有临时调试日志
- 网络环境 / VPN / 设备访问 Wikipedia 的稳定性仍可能影响测试
- `SearchScreen` 目前是“旧搜索壳 + 新推荐区块”过渡态结构

## 后续建议

优先建议继续推进：

1. 给推荐项点击接一个最小行为
2. 明确正式搜索结果页和推荐页的职责边界
3. 清理临时日志
4. 补 ViewModel / repository 测试

## 第二轮调整（2026-07-30）

这一轮主要是在首次联调日志之后，对请求策略和图片展示问题做修正。

### 1. 输入触发策略调整

原先的推荐请求是“每输入一个字符就立即发请求”，实际联调时出现了明显闪烁，日志也证明每次键入都会触发一次请求。

本轮调整后：

- `onQueryChanged()` 只负责更新 query
- 真正请求放到 `query` 的流监听中触发
- 增加 `debounce(300ms)`
- 增加 `distinctUntilChanged()`
- 取消“至少 2 个字符”限制

取消最小长度限制的原因：

- 中文单字符搜索场景很常见
- Wiki 条目检索不应过度沿用英文搜索习惯

本轮调整的结果：

- 输入过程中的请求次数显著减少
- 搜索推荐闪烁问题得到缓解
- 支持单字符中文搜索

### 2. 图片展示修正

本轮图片相关问题实际分成两层：

#### 数据层修正

Wikipedia 返回的部分缩略图地址是协议相对 URL，例如：

- `//upload.wikimedia.org/...`

这类 URL 在 App 内不会像浏览器那样自动补协议，因此新增：

- `toAbsoluteWikiUrl()`

用于在 mapper 中将其转换为：

- `https://upload.wikimedia.org/...`

从而保证图片请求地址可被图片加载器正常识别。

#### UI 层修正

缩略图最初即使请求成功，也会显得非常小。排查后确认原因是：

- 外层容器尺寸增大了
- 但 `DynamicAsyncImage` 内部的 `Image` 没有占满容器

因此对 `DynamicAsyncImage` 做了调整：

- 给内部 `Image` 增加 `Modifier.fillMaxSize()`

这一步的作用是：

- 让图片真正铺满外层容器
- 避免仅仅放大容器但图片实际显示尺寸不变

结合推荐项卡片尺寸调整后，缩略图显示效果已经明显改善。

### 3. 当前阶段结论

经过这一轮调整后：

- 搜索推荐链路已经能够稳定返回并展示结果
- 缩略图能够正确加载
- 推荐区的输入体验比初始版本更平滑

目前剩余的主要问题已经从“链路打不通”转向：

- 推荐点击后的行为定义
- 正式搜索结果页与推荐区的职责拆分
- 临时日志和调试代码清理
