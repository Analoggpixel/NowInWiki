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

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.samples.apps.nowinandroid.core.designsystem.component.NiaLoadingWheel
import com.google.samples.apps.nowinandroid.core.designsystem.icon.NiaIcons
import com.google.samples.apps.nowinandroid.core.designsystem.theme.NiaTheme
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import com.google.samples.apps.nowinandroid.core.model.data.WikiPage
import com.google.samples.apps.nowinandroid.core.ui.DevicePreviews
import com.google.samples.apps.nowinandroid.core.ui.R as UiR

@Composable
internal fun WikiPageScreen(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    language: WikiLanguage = WikiLanguage.ENGLISH,
    viewModel: WikiPageViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(title, language) {
        viewModel.loadPage(title = title, language = language)
    }

    WikiPageScreen(
        title = title,
        uiState = uiState,
        onBackClick = onBackClick,
        siteBaseUrl = "https://${language.code}.wikipedia.org/",
        modifier = modifier,
    )
}

@Composable
internal fun WikiPageScreen(
    title: String,
    uiState: WikiPageUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    siteBaseUrl: String = "https://en.wikipedia.org/",
) {
    Column(modifier = modifier.fillMaxSize()) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
        WikiPageTopBar(
            title = when (uiState) {
                is WikiPageUiState.Success -> uiState.page.title
                else -> title
            },
            onBackClick = onBackClick,
        )

        when (uiState) {
            WikiPageUiState.Idle -> Unit
            WikiPageUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    NiaLoadingWheel(contentDesc = "Loading Wikipedia page")
                }
            }
            WikiPageUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Unable to load this page.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            is WikiPageUiState.Success -> {
                WikiPageHtmlContent(
                    html = uiState.page.html,
                    siteBaseUrl = siteBaseUrl,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )
            }
        }

        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
    }
}

@Composable
private fun WikiPageTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = NiaIcons.ArrowBack,
                contentDescription = stringResource(id = UiR.string.core_ui_back),
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp),
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun WikiPageHtmlContent(
    html: String,
    siteBaseUrl: String,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(
                siteBaseUrl,
                html,
                "text/html",
                Charsets.UTF_8.name(),
                null,
            )
        },
    )
}

@DevicePreviews
@Composable
private fun WikiPageScreenPreview() {
    NiaTheme {
        WikiPageScreen(
            title = "Kotlin",
            uiState = WikiPageUiState.Success(
                page = WikiPage(
                    id = 1,
                    key = "Kotlin",
                    title = "Kotlin",
                    html = "<html><body><h1>Kotlin</h1><p>Preview content</p></body></html>",
                    contentModel = "wikitext",
                    latestRevisionId = 1,
                    latestTimestamp = "2026-01-01T00:00:00Z",
                    licenseUrl = "https://creativecommons.org/licenses/by-sa/4.0/",
                    licenseTitle = "CC BY-SA 4.0",
                ),
            ),
            onBackClick = {},
        )
    }
}
