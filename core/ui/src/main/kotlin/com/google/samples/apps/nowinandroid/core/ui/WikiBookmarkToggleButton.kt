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

package com.google.samples.apps.nowinandroid.core.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.samples.apps.nowinandroid.core.designsystem.component.CircleIconToggleButton
import com.google.samples.apps.nowinandroid.core.designsystem.icon.NiaIcons
import com.google.samples.apps.nowinandroid.core.designsystem.theme.NiaTheme

/**
 * Wiki 收藏切换按钮。
 *
 * 视觉与交互动画由 [CircleIconToggleButton] 提供；本组件只绑定 Wiki 收藏语义与图标。
 */
@Composable
fun WikiBookmarkToggleButton(
    isBookmarked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CircleIconToggleButton(
        checked = isBookmarked,
        onCheckedChange = { onToggle() },
        modifier = modifier.size(32.dp),
        icon = {
            Icon(
                imageVector = NiaIcons.Add,
                contentDescription = stringResource(R.string.core_ui_bookmark),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        },
        checkedIcon = {
            Icon(
                imageVector = NiaIcons.Check,
                contentDescription = stringResource(R.string.core_ui_unbookmark),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
        },
    )
}

@DevicePreviews
@Composable
private fun WikiBookmarkToggleButtonUncheckedPreview() {
    NiaTheme {
        WikiBookmarkToggleButton(
            isBookmarked = false,
            onToggle = {},
        )
    }
}

@DevicePreviews
@Composable
private fun WikiBookmarkToggleButtonCheckedPreview() {
    NiaTheme {
        WikiBookmarkToggleButton(
            isBookmarked = true,
            onToggle = {},
        )
    }
}
