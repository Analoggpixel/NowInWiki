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

package com.google.samples.apps.nowinandroid.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.samples.apps.nowinandroid.R
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.delay

private const val SplashFadeInMillis = 400
private const val SplashHoldMillis = 700L
private const val SplashFadeOutMillis = 350
private const val SplashFallbackTimeoutMillis = 3_000L

@Composable
fun NowInWikiSplashScreen(
    onContentReady: () -> Unit,
    onAnimationFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var hideContent by remember { mutableStateOf(false) }

    val contentAlpha by animateFloatAsState(
        targetValue = if (hideContent) 0f else 1f,
        animationSpec = tween(
            durationMillis = if (hideContent) SplashFadeOutMillis else SplashFadeInMillis,
        ),
        label = "splashContentAlpha",
    )

    val contentScale by animateFloatAsState(
        targetValue = if (hideContent) 0.92f else 1f,
        animationSpec = tween(
            durationMillis = if (hideContent) SplashFadeOutMillis else SplashFadeInMillis,
        ),
        label = "splashContentScale",
    )

    var animationFinished = remember { false }

    fun finishAnimation() {
        if (!animationFinished) {
            animationFinished = true
            onAnimationFinished()
        }
    }

    LaunchedEffect(Unit) {
        awaitFrame()
        onContentReady()
        delay(SplashFadeInMillis.toLong() + SplashHoldMillis)
        hideContent = true
        delay(SplashFadeOutMillis.toLong())
        finishAnimation()
    }

    LaunchedEffect(Unit) {
        delay(SplashFallbackTimeoutMillis)
        finishAnimation()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.splash_background)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_wiki_w),
            contentDescription = null,
            modifier = Modifier
                .size(220.dp)
                .scale(contentScale)
                .alpha(contentAlpha),
        )
    }
}
