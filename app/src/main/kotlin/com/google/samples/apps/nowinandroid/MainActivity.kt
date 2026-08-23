/*
 * Copyright 2022 The Android Open Source Project
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

package com.google.samples.apps.nowinandroid

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.metrics.performance.JankStats
import androidx.tracing.trace
import com.google.samples.apps.nowinandroid.MainActivityUiState.Loading
import com.google.samples.apps.nowinandroid.MainActivityUiState.Success
import com.google.samples.apps.nowinandroid.core.analytics.AnalyticsHelper
import com.google.samples.apps.nowinandroid.core.analytics.LocalAnalyticsHelper
import com.google.samples.apps.nowinandroid.core.data.util.NetworkMonitor
import com.google.samples.apps.nowinandroid.core.data.util.TimeZoneMonitor
import com.google.samples.apps.nowinandroid.core.designsystem.theme.NiaTheme
import com.google.samples.apps.nowinandroid.core.ui.LocalTimeZone
import com.google.samples.apps.nowinandroid.ui.NiaApp
import com.google.samples.apps.nowinandroid.ui.NowInWikiSplashScreen
import com.google.samples.apps.nowinandroid.ui.rememberNiaAppState
import com.google.samples.apps.nowinandroid.util.isSystemInDarkTheme
import com.google.samples.apps.nowinandroid.util.AppUiLanguageApplier.applyIfNeeded
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    /**
     * Lazily inject [JankStats], which is used to track jank throughout the app.
     */
    @Inject
    lateinit var lazyStats: dagger.Lazy<JankStats>

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var timeZoneMonitor: TimeZoneMonitor

    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    private val viewModel: MainActivityViewModel by viewModels()

    private var isSplashContentReady by mutableStateOf(false)
    private var isSplashAnimationFinished by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // ViewModel survives rotation; restore splash flags so the animation does not replay.
        isSplashContentReady = viewModel.isSplashContentReady
        isSplashAnimationFinished = viewModel.isSplashAnimationFinished

        // We keep this as a mutable state, so that we can track changes inside the composition.
        // This allows us to react to dark/light mode changes.
        var themeSettings by mutableStateOf(
            ThemeSettings(
                darkTheme = resources.configuration.isSystemInDarkTheme,
                androidTheme = Loading.shouldUseAndroidTheme,
                disableDynamicTheming = Loading.shouldDisableDynamicTheming,
            ),
        )

        // Update the uiState
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    isSystemInDarkTheme(),
                    viewModel.uiState,
                ) { systemDark, uiState ->
                    ThemeSettings(
                        darkTheme = uiState.shouldUseDarkTheme(systemDark),
                        androidTheme = uiState.shouldUseAndroidTheme,
                        disableDynamicTheming = uiState.shouldDisableDynamicTheming,
                    )
                }
                    .onEach { themeSettings = it }
                    .map { it.darkTheme }
                    .distinctUntilChanged()
                    .collect { darkTheme ->
                        trace("niaEdgeToEdge") {
                            // Turn off the decor fitting system windows, which allows us to handle insets,
                            // including IME animations, and go edge-to-edge.
                            // This is the same parameters as the default enableEdgeToEdge call, but we manually
                            // resolve whether or not to show dark theme using uiState, since it can be different
                            // than the configuration's dark theme value based on the user preference.
                            enableEdgeToEdge(
                                statusBarStyle = SystemBarStyle.auto(
                                    lightScrim = android.graphics.Color.TRANSPARENT,
                                    darkScrim = android.graphics.Color.TRANSPARENT,
                                ) { darkTheme },
                                navigationBarStyle = SystemBarStyle.auto(
                                    lightScrim = lightScrim,
                                    darkScrim = darkScrim,
                                ) { darkTheme },
                            )
                        }
                    }
            }
        }

        // Apply persisted UI language; recreates the activity when the locale changes.
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState
                    .filterIsInstance<Success>()
                    .map { it.userData.appUiLanguage }
                    .distinctUntilChanged()
                    .collect(::applyIfNeeded)
            }
        }

        // Keep the system splash only until startup data is ready and Compose has drawn the
        // first frame. The branded animation runs in Compose after the system splash dismisses.
        splashScreen.setKeepOnScreenCondition {
            viewModel.uiState.value.shouldKeepSplashScreen() || !isSplashContentReady
        }

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val showSplashOverlay = uiState.shouldKeepSplashScreen() || !isSplashAnimationFinished

            Box(modifier = Modifier.fillMaxSize()) {
                if (!showSplashOverlay) {
                    val appState = rememberNiaAppState(
                        networkMonitor = networkMonitor,
                        timeZoneMonitor = timeZoneMonitor,
                    )

                    val currentTimeZone by appState.currentTimeZone.collectAsStateWithLifecycle()

                    CompositionLocalProvider(
                        LocalAnalyticsHelper provides analyticsHelper,
                        LocalTimeZone provides currentTimeZone,
                    ) {
                        NiaTheme(
                            darkTheme = themeSettings.darkTheme,
                            androidTheme = themeSettings.androidTheme,
                            disableDynamicTheming = themeSettings.disableDynamicTheming,
                        ) {
                            NiaApp(appState)
                        }
                    }
                }

                if (showSplashOverlay) {
                    NowInWikiSplashScreen(
                        onContentReady = {
                            viewModel.onSplashContentReady()
                            isSplashContentReady = true
                        },
                        onAnimationFinished = {
                            viewModel.onSplashAnimationFinished()
                            isSplashAnimationFinished = true
                        },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lazyStats.get().isTrackingEnabled = true
    }

    override fun onPause() {
        super.onPause()
        lazyStats.get().isTrackingEnabled = false
    }
}

/**
 * The default light scrim, as defined by androidx and the platform:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=35-38;drc=27e7d52e8604a080133e8b842db10c89b4482598
 */
private val lightScrim = android.graphics.Color.argb(0xe6, 0xFF, 0xFF, 0xFF)

/**
 * The default dark scrim, as defined by androidx and the platform:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt;l=40-44;drc=27e7d52e8604a080133e8b842db10c89b4482598
 */
private val darkScrim = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)

/**
 * Class for the system theme settings.
 * This wrapping class allows us to combine all the changes and prevent unnecessary recompositions.
 */
data class ThemeSettings(
    val darkTheme: Boolean,
    val androidTheme: Boolean,
    val disableDynamicTheming: Boolean,
)
