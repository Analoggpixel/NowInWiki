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

package com.google.samples.apps.nowinandroid.feature.foryou.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.samples.apps.nowinandroid.core.designsystem.component.DynamicAsyncImage
import com.google.samples.apps.nowinandroid.core.designsystem.theme.NiaTheme
import com.google.samples.apps.nowinandroid.core.model.data.WikiFeedItem
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import com.google.samples.apps.nowinandroid.core.ui.DevicePreviews

@Composable
fun ForYouScreenNew(
    onItemClick: (WikiFeedItem) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForYouViewModelNew = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val preferredLanguage by viewModel.preferredLanguage.collectAsStateWithLifecycle()
    ForYouScreenNew(
        uiState = uiState,
        isRefreshing = isRefreshing,
        preferredLanguage = preferredLanguage,
        onItemClick = onItemClick,
        onRefresh = viewModel::onRefresh,
        modifier = modifier,
    )
}

@Composable
internal fun ForYouScreenNew(
    modifier: Modifier = Modifier,
    uiState: ForYouFeedUiState,
    isRefreshing: Boolean = false,
    preferredLanguage: WikiLanguage,
    onItemClick: (WikiFeedItem) -> Unit,
    onRefresh: () -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            when (uiState) {
                ForYouFeedUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                ForYouFeedUiState.Empty -> {
                    FeedMessage(
                        message = "No feed items found.",
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                ForYouFeedUiState.Error -> {
                    FeedMessage(
                        message = "Unable to load feed.",
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                is ForYouFeedUiState.Success -> {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalItemSpacing = 12.dp,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            items = uiState.items,
                            key = { it.id },
                        ) { item ->
                            WikiFeedCard(
                                item = item,
                                onClick = { onItemClick(item) },
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
    }
}

@Composable
private fun FeedMessage(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun WikiFeedCard(
    item: WikiFeedItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val thumbnailUrl = item.thumbnailUrl
    val description = item.description ?: item.excerpt

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .clickable(onClick = onClick)
            .padding(bottom = 10.dp),
    ) {
        if (thumbnailUrl != null) {
            DynamicAsyncImage(
                imageUrl = thumbnailUrl,
                contentDescription = item.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(item.thumbnailAspectRatio ?: 1f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
            )
        } else {
            // TODO: thumbnail 为空时改为本地「造图」占位（如按 title/id 稳定配色 + 首字母/渐变封面），
            // 保证卡片高度与可点区域，避免纯文字卡过矮影响瀑布流交互。
            // Mock-only height variation so the staggered grid looks like a waterfall
            // before real thumbnails are available.
            val placeholderHeight = 100.dp + (40.dp * ((item.id % 4).toInt()))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(placeholderHeight)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(mockPlaceholderColor(item.id)),
            )
        }
        Text(
            text = item.title,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
        )
        if (!description.isNullOrBlank()) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 10.dp),
            )
        }
    }
}

private fun mockPlaceholderColor(id: Long): Color {
    val colors = listOf(
        Color(0xFF90CAF9),
        Color(0xFFA5D6A7),
        Color(0xFFFFCC80),
        Color(0xFFCE93D8),
        Color(0xFF80CBC4),
        Color(0xFFEF9A9A),
        Color(0xFFFFE082),
        Color(0xFF9FA8DA),
    )
    return colors[(id % colors.size).toInt()]
}

private val mockWikiFeedItems: List<WikiFeedItem> = listOf(
    WikiFeedItem(
        id = 1,
        key = "Kotlin",
        title = "Kotlin",
        itemLanguage = WikiLanguage.ENGLISH,
        description = "A cross-platform, statically typed, general-purpose programming language.",
    ),
    WikiFeedItem(
        id = 2,
        key = "Jetpack_Compose",
        title = "Jetpack Compose",
        itemLanguage = WikiLanguage.ENGLISH,
        description = "Android’s modern toolkit for building native UI with less code and powerful tools.",
    ),
    WikiFeedItem(
        id = 3,
        key = "Wikipedia",
        title = "Wikipedia",
        itemLanguage = WikiLanguage.ENGLISH,
        description = "A free online encyclopedia.",
    ),
    WikiFeedItem(
        id = 4,
        key = "Android_(operating_system)",
        title = "Android (operating system)",
        itemLanguage = WikiLanguage.ENGLISH,
        description = "A mobile operating system based on a modified version of the Linux kernel and other open-source software.",
    ),
    WikiFeedItem(
        id = 5,
        key = "Material_Design",
        title = "Material Design",
        itemLanguage = WikiLanguage.ENGLISH,
        description = "A design language developed by Google.",
    ),
    WikiFeedItem(
        id = 6,
        key = "Coroutine",
        title = "Coroutine",
        itemLanguage = WikiLanguage.ENGLISH,
        description = "A concurrency design pattern that can be used for asynchronous programming.",
    ),
    WikiFeedItem(
        id = 7,
        key = "Open_source",
        title = "Open source",
        itemLanguage = WikiLanguage.ENGLISH,
        description = "Source code that is made freely available for use, modification, and redistribution.",
    ),
    WikiFeedItem(
        id = 8,
        key = "Machine_learning",
        title = "Machine learning",
        itemLanguage = WikiLanguage.ENGLISH,
        description = "A field of study in artificial intelligence concerned with the development of algorithms that can learn from data.",
    ),
)

@DevicePreviews
@Composable
private fun ForYouScreenNewPreview() {
    NiaTheme {
        ForYouScreenNew(
            uiState = ForYouFeedUiState.Success(mockWikiFeedItems),
            onItemClick = {},
            onRefresh = {},
            modifier = Modifier,
            isRefreshing = false,
            preferredLanguage = WikiLanguage.ENGLISH,
        )
    }
}
