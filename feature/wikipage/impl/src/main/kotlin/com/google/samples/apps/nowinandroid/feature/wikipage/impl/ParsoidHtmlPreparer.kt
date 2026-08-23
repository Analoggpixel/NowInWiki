/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.apps.nowinandroid.feature.wikipage.impl

import android.net.Uri
import android.util.Log
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage

private const val WIKI_PAGE_NAV_TAG = "WikiPageNav"

/**
 * Prepares PCS `mobile-html` for [android.webkit.WebView.loadDataWithBaseURL] by rewriting
 * protocol-relative URLs and injecting CSS/JS from `mobile-html-offline-resources`.
 */
internal fun prepareMobileHtmlForWebView(
    html: String,
    resourceUrls: List<String>,
): String {
    var prepared = rewriteProtocolRelativeUrls(html)
    prepared = ensureOfflineResourceTags(prepared, resourceUrls)
    return prepared
}

/**
 * Parses a same-language Wikipedia article title from a clicked URL, or null if the URL
 * should not be handled via the in-app page loader (external / non-article).
 */
internal fun parseInternalWikiArticleTitle(
    uri: Uri,
    language: WikiLanguage,
): String? {
    val host = uri.host?.lowercase()
    if (host == null) {
        Log.d(WIKI_PAGE_NAV_TAG, "parse skip: host=null uri=$uri")
        return null
    }
    val allowedHosts = setOf(
        "${language.hostCode}.wikipedia.org",
        "${language.hostCode}.m.wikipedia.org",
    )
    if (host !in allowedHosts) {
        Log.d(
            WIKI_PAGE_NAV_TAG,
            "parse skip: host=$host not in $allowedHosts uri=$uri scheme=${uri.scheme}",
        )
        return null
    }

    val path = uri.path
    if (path == null || !path.startsWith("/wiki/")) {
        Log.d(WIKI_PAGE_NAV_TAG, "parse skip: path=$path (need /wiki/...) uri=$uri")
        return null
    }

    return decodeWikiTitleSegment(
        encodedTitle = path.removePrefix("/wiki/").substringBefore('#').substringBefore('?'),
        source = "uri=$uri",
    )
}

/**
 * 从 PCS / 站内链接的 href 解析条目标题。
 *
 * PCS 点击回调里常见相对路径：`./亚里士多德`、`/wiki/雅典`，不一定带 host；
 * 也会出现完整 `https://zh.wikipedia.org/wiki/...`。纯 `#锚点` 视为页内跳转，不加载新页。
 */
internal fun parseArticleTitleFromHref(
    href: String,
    language: WikiLanguage,
): String? {
    val raw = href.trim()
    if (raw.isEmpty()) {
        Log.d(WIKI_PAGE_NAV_TAG, "parse href skip: empty")
        return null
    }
    if (raw.startsWith("#")) {
        Log.d(WIKI_PAGE_NAV_TAG, "parse href skip: in-page fragment href=$raw")
        return null
    }

    val normalized = when {
        raw.startsWith("//") -> "https:$raw"
        else -> raw
    }
    val uri = Uri.parse(normalized)

    // 相对路径：./Title、/wiki/Title、或裸 Title
    if (uri.scheme.isNullOrEmpty() && uri.host.isNullOrEmpty()) {
        val path = (uri.path ?: normalized).substringBefore('#').substringBefore('?')
        val encodedTitle = when {
            path.startsWith("/wiki/") -> path.removePrefix("/wiki/")
            path.startsWith("./") -> path.removePrefix("./")
            path.startsWith("/") -> {
                Log.d(WIKI_PAGE_NAV_TAG, "parse href skip: non-wiki path=$path")
                return null
            }
            else -> path
        }
        return decodeWikiTitleSegment(encodedTitle = encodedTitle, source = "href=$href")
    }

    return parseInternalWikiArticleTitle(uri = uri, language = language)
}

private fun decodeWikiTitleSegment(encodedTitle: String, source: String): String? {
    if (encodedTitle.isBlank()) {
        Log.d(WIKI_PAGE_NAV_TAG, "parse skip: blank title ($source)")
        return null
    }
    val decoded = Uri.decode(encodedTitle).replace('_', ' ').trim()
    if (decoded.isEmpty()) {
        Log.d(WIKI_PAGE_NAV_TAG, "parse skip: decoded empty encoded=$encodedTitle ($source)")
        return null
    }
    if (isNonArticleWikiTitle(decoded)) {
        Log.d(WIKI_PAGE_NAV_TAG, "parse skip: non-article namespace title=$decoded ($source)")
        return null
    }
    Log.d(WIKI_PAGE_NAV_TAG, "parse ok: title=$decoded ($source)")
    return decoded
}

/**
 * 将协议相对地址（`//host/...`）改写成绝对地址 `https://host/...`。
 *
 * PCS / 维基 HTML 里常见无协议链接（如 `src="//upload.wikimedia.org/..."`）。
 * 在普通浏览器里会跟随当前页面协议；但用 [android.webkit.WebView.loadDataWithBaseURL]
 * 注入时这种继承不可靠，资源可能加载失败，因此强制补上 `https://`。
 *
 * 覆盖 `href` / `src` / `poster` 属性，以及 CSS 中的 `url(//...)`。
 */
private fun rewriteProtocolRelativeUrls(html: String): String =
    html
        .replace(Regex("""\b(href|src|poster)\s*=\s*(["'])//"""), "$1=$2https://")
        .replace(Regex("""\burl\(\s*//"""), "url(https://")

/**
 * 将 offline-resources 的 CSS/JS 标签注入 HTML。
 *
 * 插入位置按结构完整度三级兜底（前一步返回 null 再试下一级）：
 * 1. 已有 `</head>` → 插在关闭标签前
 * 2. 仅有 `<html>`、无 head → 在 `<html>` 后补一整段 `<head>...</head>`
 * 3. 连 `<html>` 都没有（片段 HTML）→ 拼到全文最前面，保证样式脚本仍能挂上
 */
private fun ensureOfflineResourceTags(html: String, resourceUrls: List<String>): String {
    if (resourceUrls.isEmpty()) return html

    // 构造资源 tag
    val tags = buildString {
        for (url in resourceUrls) {
            if (html.contains(url)) continue
            when {
                isJavascriptResourceUrl(url) -> append("""<script src="$url"></script>""")
                else -> append("""<link rel="stylesheet" href="$url"/>""")
            }
        }
    }
    if (tags.isEmpty()) return html

    return insertBeforeTag(html, tagName = "head", insertion = tags)
        ?: insertAfterHtmlOpen(html, "<head>$tags</head>")
        ?: "<head>$tags</head>$html"
}

/**
 * PCS offline-resources use path segments `/css/`, `/javascript/`, and `/i18n/` (JS strings).
 */
private fun isJavascriptResourceUrl(url: String): Boolean {
    val lower = url.lowercase()
    return "/javascript/" in lower || "/i18n/" in lower || lower.endsWith(".js")
}

/**
 * Inserts [insertion] immediately before `</tagName>`.
 */
private fun insertBeforeTag(html: String, tagName: String, insertion: String): String? {
    val close = Regex("""</$tagName>""", RegexOption.IGNORE_CASE)
    val match = close.find(html) ?: return null
    return html.replaceRange(match.range, "$insertion</$tagName>")
}

private fun insertAfterHtmlOpen(html: String, insertion: String): String? {
    val htmlOpen = Regex("""<html\b[^>]*>""", RegexOption.IGNORE_CASE)
    val match = htmlOpen.find(html) ?: return null
    return html.replaceRange(match.range, "${match.value}$insertion")
}

private fun isNonArticleWikiTitle(title: String): Boolean {
    val namespace = title.substringBefore(':', missingDelimiterValue = "").lowercase()
    if (namespace.isEmpty() || namespace == title.lowercase()) return false
    return namespace in NON_ARTICLE_NAMESPACES
}

private val NON_ARTICLE_NAMESPACES = setOf(
    "special", "file", "image", "media", "template", "category",
    "help", "wikipedia", "mediawiki", "user", "talk",
    "特殊", "文件", "模板", "分类", "帮助", "用户",
)
