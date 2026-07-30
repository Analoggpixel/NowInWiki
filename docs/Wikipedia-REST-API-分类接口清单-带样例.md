# Wikipedia / MediaWiki REST API 分类接口清单

生成日期：2026-07-27

本文档按常见分类整理：

- `Search`
- `Pages`
- `Transform`
- `Media files`
- `History`

目标：

1. 说明每类接口主要是干什么的
2. 列出做百科客户端时常用的接口
3. 补充几个真实返回样例，帮助理解数据格式

主要参考：

- [MediaWiki REST API 总览](https://www.mediawiki.org/wiki/API:REST_API/en)
- [MediaWiki REST API Reference](https://www.mediawiki.org/wiki/API:REST_API/Reference/en)
- [MediaWiki Action API Query](https://www.mediawiki.org/wiki/API:Query)

说明：

- 下文路径以 `https://en.wikipedia.org/w/rest.php/v1` 为例。
- `summary` 接口来自 Wikipedia Page Content Service，单独使用 `https://en.wikipedia.org/api/rest_v1/...`

---

## 1. Search

### 这一类是干什么的

`Search` 用来找页面，适合：

- 搜索框联想
- 搜索结果列表
- 关键词查条目

### 常用接口

| 接口 | 方法 | 获取的数据 | 典型用途 |
|---|---|---|---|
| `/search/page?q={query}` | `GET` | 页面搜索结果，包含标题、摘要片段、描述、缩略图等 | 搜索结果页 |
| `/search/title?q={query}` | `GET` | 标题前缀匹配结果 | 输入联想 |

### 真实返回样例

接口：

`GET /search/page?q=Kotlin`

返回大致长这样：

```json
{
  "pages": [
    {
      "id": 41819039,
      "key": "Kotlin",
      "title": "Kotlin",
      "excerpt": "<span class=\"searchmatch\">Kotlin</span> ...",
      "matched_title": null,
      "anchor": null,
      "description": "General-purpose programming language",
      "thumbnail": null
    },
    {
      "id": 763139,
      "key": "Kotlin_Island",
      "title": "Kotlin Island",
      "excerpt": "...",
      "description": "Russian island in the Gulf of Finland",
      "thumbnail": {
        "mimetype": "image/png",
        "width": 60,
        "height": 42,
        "url": "//upload.wikimedia.org/..."
      }
    }
  ]
}
```

重点字段：

- `pages`: 搜索结果数组
- `id/key/title`: 页面标识
- `excerpt`: 命中的摘要片段
- `description`: 一句话描述
- `thumbnail`: 缩略图

---

## 2. Pages

### 这一类是干什么的

`Pages` 用来获取页面本体，适合：

- 条目详情
- 页面源码
- 页面 HTML 正文
- 页面元数据
- 语言链接、媒体链接

### 常用接口

| 接口 | 方法 | 获取的数据 | 典型用途 |
|---|---|---|---|
| `/page/{title}` | `GET` | 页面源码内容 + 基础元数据 | 源码查看、编辑准备、内容分析 |
| `/page/{title}/bare` | `GET` | 基础元数据，不含完整正文 | 判断页面是否存在、取标题和基本信息 |
| `/page/{title}/html` | `GET` | HTML 正文 | 条目详情页渲染 |
| `/page/{title}/with_html` | `GET` | 元数据 + HTML 正文 | 一次请求拿页面主体内容 |
| `/page/{title}/links/language` | `GET` | 跨语言版本链接 | 多语言切换 |
| `/page/{title}/links/media` | `GET` | 页面中使用到的媒体文件列表 | 图库、页面资源提取 |

### 真实返回样例 1：页面源码

接口：

`GET /page/Kotlin`

返回大致长这样：

```json
{
  "id": 41819039,
  "key": "Kotlin",
  "title": "Kotlin",
  "latest": {
    "id": 1365036992,
    "timestamp": "2026-07-19T22:15:49Z"
  },
  "content_model": "wikitext",
  "license": {
    "url": "https://creativecommons.org/licenses/by-sa/4.0/deed.en",
    "title": "Creative Commons Attribution-Share Alike 4.0"
  },
  "source": "{{Short description|General-purpose programming language}}\n{{Infobox programming language\n| name = Kotlin\n| designer = Andrey Breslav, [[JetBrains]]\n...}}"
}
```

重点字段：

- `latest`: 当前最新 revision
- `content_model`: 内容格式，Wikipedia 常见是 `wikitext`
- `source`: 页面原始源码

### 真实返回样例 2：页面 HTML

接口：

`GET /page/Kotlin/with_html`

返回大致长这样：

```json
{
  "id": 41819039,
  "key": "Kotlin",
  "title": "Kotlin",
  "latest": {
    "id": 1365036992,
    "timestamp": "2026-07-19T22:15:49Z"
  },
  "content_model": "wikitext",
  "license": {
    "url": "https://creativecommons.org/licenses/by-sa/4.0/deed.en",
    "title": "Creative Commons Attribution-Share Alike 4.0"
  },
  "html": "<!DOCTYPE html><html ...><body ...>...</body></html>"
}
```

重点字段：

- `html`: 大段可展示 HTML
- 适合直接作为阅读页正文来源

### 真实返回样例 3：summary

虽然不在 `/w/rest.php/v1/page/...` 下，但它也是页面阅读场景最常用的接口。

接口：

`GET https://en.wikipedia.org/api/rest_v1/page/summary/Kotlin_(programming_language)`

返回大致长这样：

```json
{
  "type": "standard",
  "title": "Kotlin",
  "displaytitle": "<span ...>Kotlin</span>",
  "namespace": {
    "id": 0,
    "text": ""
  },
  "wikibase_item": "Q3816639",
  "titles": {
    "canonical": "Kotlin",
    "normalized": "Kotlin",
    "display": "<span ...>Kotlin</span>"
  },
  "pageid": 41819039,
  "lang": "en",
  "dir": "ltr",
  "revision": "1365036992",
  "timestamp": "2026-07-19T22:15:49Z",
  "description": "General-purpose programming language",
  "content_urls": {
    "desktop": {
      "page": "https://en.wikipedia.org/wiki/Kotlin"
    }
  },
  "extract": "Kotlin is a cross-platform..."
}
```

重点字段：

- `description`: 简短描述
- `extract`: 摘要
- `content_urls`: 页面 URL

---

## 3. Transform

### 这一类是干什么的

`Transform` 用来临时转换内容格式，适合：

- 把 `wikitext` 转成 HTML
- 把 HTML 转回 `wikitext`
- 做编辑器预览
- 做 lint 校验

### 常用接口

| 接口 | 方法 | 获取的数据 | 典型用途 |
|---|---|---|---|
| `/transform/wikitext/to/html/{title}` | `POST` | wikitext 转 HTML | 编辑预览、片段预览 |
| `/transform/html/to/wikitext/{title}` | `POST` | HTML 转 wikitext | 可视化编辑器回写 |
| `/transform/wikitext/to/lint/{title}` | `POST` | wikitext lint 结果 | 编辑校验 |

### 返回格式怎么理解

`Transform` 的返回通常不是搜索那种列表，而更像：

- 一段转换后的 `html`
- 或一段转换后的 `wikitext`
- 或 lint 错误数组

它更像“转换服务”，不是“读取现成页面”。

---

## 4. Media files

### 这一类是干什么的

`Media files` 用来处理页面关联的图片、音频、视频和文件资源，适合：

- 页面头图
- 图库
- 文件详情

### 常用接口

| 接口 | 方法 | 获取的数据 | 典型用途 |
|---|---|---|---|
| `/page/{title}/links/media` | `GET` | 页面使用到的媒体文件列表 | 页面插图、图库入口 |
| `/file/{title}` | `GET` | 文件页面的基础信息 | 单个文件详情 |
| `/file/{title}/download` | `GET` | 文件原始下载地址或下载流 | 下载原图、音频、视频 |

### 真实返回样例

接口：

`GET /page/Kotlin_(programming_language)/links/media`

我实际查到的一个返回是：

```json
{
  "files": []
}
```

也就是说这类接口的顶层通常是：

```json
{
  "files": [
    {
      "title": "File:xxx.png",
      "...": "..."
    }
  ]
}
```

重点字段：

- `files`: 媒体文件数组
- 每项通常代表一个图片/音频/视频文件

---

## 5. History

### 这一类是干什么的

`History` 用来查看页面的修订历史，适合：

- 最近编辑
- 历史版本
- 修订记录

### 常用接口

| 接口 | 方法 | 获取的数据 | 典型用途 |
|---|---|---|---|
| `/page/{title}/history` | `GET` | 页面修订历史列表 | 历史版本页 |
| `/page/{title}/history/{revision}` | `GET` | 指定 revision 的上下文 | 某次具体修订 |
| `/page/{title}/history/counts/{type}` | `GET` | 历史统计数据 | 统计分析 |

### 真实返回样例

接口：

`GET /page/Kotlin_(programming_language)/history`

返回大致长这样：

```json
{
  "revisions": [
    {
      "id": 1318981456,
      "timestamp": "2025-10-27T03:24:01Z",
      "minor": false,
      "size": 53,
      "comment": "Changed redirect target ...",
      "user": {
        "id": 41849648,
        "name": "Jeffrey34555"
      },
      "delta": -17
    },
    {
      "id": 1318981394,
      "timestamp": "2025-10-27T03:23:41Z",
      "minor": true,
      "size": 70,
      "comment": "...",
      "user": {
        "id": 41849648,
        "name": "Jeffrey34555"
      },
      "delta": 0
    }
  ]
}
```

重点字段：

- `revisions`: 修订记录数组
- `timestamp`: 时间
- `comment`: 编辑摘要
- `user`: 编辑者
- `delta`: 本次改动大小

---

## 最小可用接口组合

如果你现在只是想做一个 `Wikipedia MVP`，建议第一批只接：

### 搜索

- `/search/title?q={query}`
- `/search/page?q={query}`

### 页面

- `/page/{title}/with_html`
- `summary`

### 资源

- `/page/{title}/links/media`

### 历史

- `/page/{title}/history`

### 多语言

- `/page/{title}/links/language`

---

## 推荐理解方式

一句话记住：

- `Search`：负责找
- `Pages`：负责读
- `Transform`：负责转换
- `Media files`：负责资源
- `History`：负责版本

