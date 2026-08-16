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

package com.google.samples.apps.nowinandroid.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.google.samples.apps.nowinandroid.core.designsystem.icon.NiaIcons
import com.google.samples.apps.nowinandroid.core.designsystem.theme.NiaTheme
import kotlinx.coroutines.launch

/**
 * Circular outlined icon toggle with a press bounce and icon crossfade.
 *
 * No product semantics — callers supply icons and wire domain meaning (e.g. bookmark).
 *
 * @param checked Whether the toggle is currently checked.
 * @param onCheckedChange Called when the user clicks and toggles checked.
 * @param modifier Modifier to be applied to the toggle.
 * @param enabled Controls the enabled state.
 * @param icon Content when unchecked.
 * @param checkedIcon Content when checked.
 */
@Composable
fun CircleIconToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable () -> Unit,
    checkedIcon: @Composable () -> Unit = icon,
) {
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    val interactionSource = remember { MutableInteractionSource() }

    val borderColor = if (checked) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .clip(CircleShape)
            .border(width = 1.5.dp, color = borderColor, shape = CircleShape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                role = Role.Checkbox,
                onClick = {
                    scope.launch {
                        scale.animateTo(
                            targetValue = 0.82f,
                            animationSpec = tween(durationMillis = 90),
                        )
                        scale.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium,
                            ),
                        )
                    }
                    onCheckedChange(!checked)
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = checked,
            transitionSpec = {
                (
                    fadeIn(animationSpec = tween(140)) +
                        scaleIn(
                            initialScale = 0.6f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium,
                            ),
                        )
                    ) togetherWith (
                    fadeOut(animationSpec = tween(90)) +
                        scaleOut(targetScale = 0.6f, animationSpec = tween(90))
                    )
            },
            label = "CircleIconToggle",
        ) { isChecked ->
            if (isChecked) checkedIcon() else icon()
        }
    }
}

@ThemePreviews
@Composable
private fun CircleIconToggleButtonCheckedPreview() {
    NiaTheme {
        CircleIconToggleButton(
            checked = true,
            onCheckedChange = {},
            icon = {
                Icon(
                    imageVector = NiaIcons.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                )
            },
            checkedIcon = {
                Icon(
                    imageVector = NiaIcons.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp),
                )
            },
        )
    }
}

@ThemePreviews
@Composable
private fun CircleIconToggleButtonUncheckedPreview() {
    NiaTheme {
        CircleIconToggleButton(
            checked = false,
            onCheckedChange = {},
            icon = {
                Icon(
                    imageVector = NiaIcons.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                )
            },
            checkedIcon = {
                Icon(
                    imageVector = NiaIcons.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp),
                )
            },
        )
    }
}
