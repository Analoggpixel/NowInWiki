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

package com.google.samples.apps.nowinandroid.core.model.data

/**
 * For You / 推荐流中的单条 Wiki 卡片。
 *
 * 与 [WikiSuggestionItem] 分开建模：搜索联想保持精简；Feed 后续可扩展推荐分、
 * 兴趣标签、语言等，而不污染搜索 domain。
 *
 * 缩略图保留 API 的像素宽高，布局用 [thumbnailAspectRatio]（width / height）。
 */
data class WikiFeedItem(
    val id: Long,
    val key: String,
    val title: String,
    val description: String? = null,
    val excerpt: String? = null,
    val thumbnailUrl: String? = null,
    val thumbnailWidth: Int? = null,
    val thumbnailHeight: Int? = null,
    val itemLanguage: WikiLanguage,
) {
    /** Compose [androidx.compose.foundation.layout.aspectRatio] 用的宽高比；未知时为 null。 */
    val thumbnailAspectRatio: Float?
        get() {
            val width = thumbnailWidth ?: return null
            val height = thumbnailHeight ?: return null
            if (height <= 0) return null
            return width.toFloat() / height.toFloat()
        }
}
