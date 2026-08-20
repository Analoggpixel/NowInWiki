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

package com.google.samples.apps.nowinandroid.feature.settings.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.google.samples.apps.nowinandroid.core.model.data.DarkThemeConfig.DARK
import com.google.samples.apps.nowinandroid.core.model.data.ThemeBrand.ANDROID
import com.google.samples.apps.nowinandroid.core.model.data.ThemeBrand.DEFAULT
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import com.google.samples.apps.nowinandroid.core.model.data.WikiReaderTextScale
import com.google.samples.apps.nowinandroid.feature.settings.impl.SettingsUiState.Loading
import com.google.samples.apps.nowinandroid.feature.settings.impl.SettingsUiState.Success
import org.junit.Rule
import org.junit.Test

class SettingsDialogTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun getString(id: Int) = composeTestRule.activity.resources.getString(id)

    private fun defaultSettings(
        brand: com.google.samples.apps.nowinandroid.core.model.data.ThemeBrand = ANDROID,
        useDynamicColor: Boolean = false,
        darkThemeConfig: com.google.samples.apps.nowinandroid.core.model.data.DarkThemeConfig = DARK,
    ) = UserEditableSettings(
        brand = brand,
        useDynamicColor = useDynamicColor,
        darkThemeConfig = darkThemeConfig,
        preferredWikiLanguage = WikiLanguage.CHINESE,
        wikiReaderTextScale = WikiReaderTextScale.DEFAULT,
    )

    @Test
    fun whenLoading_showsLoadingText() {
        composeTestRule.setContent {
            SettingsDialog(
                settingsUiState = Loading,
                onDismiss = {},
                onChangeDynamicColorPreference = {},
                onChangeThemeBrand = {},
                onChangeDarkThemeConfig = {},
                onChangePreferredWikiLanguage = {},
                onChangeWikiReaderTextScale = {},
            )
        }

        composeTestRule
            .onNodeWithText(getString(R.string.feature_settings_impl_loading))
            .assertExists()
    }

    @Test
    fun whenStateIsSuccess_allDefaultSettingsAreDisplayed() {
        composeTestRule.setContent {
            SettingsDialog(
                settingsUiState = Success(defaultSettings()),
                onDismiss = { },
                onChangeDynamicColorPreference = {},
                onChangeThemeBrand = {},
                onChangeDarkThemeConfig = {},
                onChangePreferredWikiLanguage = {},
                onChangeWikiReaderTextScale = {},
            )
        }

        composeTestRule.onNodeWithText(getString(R.string.feature_settings_impl_brand_default)).assertExists()
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_impl_brand_android)).assertExists()
        composeTestRule.onNodeWithText(
            getString(R.string.feature_settings_impl_dark_mode_config_system_default),
        ).assertExists()
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_impl_dark_mode_config_light)).assertExists()
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_impl_dark_mode_config_dark)).assertExists()

        composeTestRule.onNodeWithText(getString(R.string.feature_settings_impl_brand_android)).assertIsSelected()
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_impl_dark_mode_config_dark)).assertIsSelected()
    }

    @Test
    fun whenStateIsSuccess_supportsDynamicColor_usesDefaultBrand_DynamicColorOptionIsDisplayed() {
        composeTestRule.setContent {
            SettingsDialog(
                settingsUiState = Success(defaultSettings(brand = DEFAULT)),
                supportDynamicColor = true,
                onDismiss = {},
                onChangeDynamicColorPreference = {},
                onChangeThemeBrand = {},
                onChangeDarkThemeConfig = {},
                onChangePreferredWikiLanguage = {},
                onChangeWikiReaderTextScale = {},
            )
        }

        composeTestRule.onNodeWithText(getString(R.string.feature_settings_impl_dynamic_color_preference)).assertExists()
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_impl_dynamic_color_yes)).assertExists()
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_impl_dynamic_color_no)).assertExists()

        composeTestRule.onNodeWithText(getString(R.string.feature_settings_impl_dynamic_color_no)).assertIsSelected()
    }

    @Test
    fun whenStateIsSuccess_notSupportDynamicColor_DynamicColorOptionIsNotDisplayed() {
        composeTestRule.setContent {
            SettingsDialog(
                settingsUiState = Success(defaultSettings()),
                onDismiss = {},
                onChangeDynamicColorPreference = {},
                onChangeThemeBrand = {},
                onChangeDarkThemeConfig = {},
                onChangePreferredWikiLanguage = {},
                onChangeWikiReaderTextScale = {},
            )
        }

        composeTestRule.onNodeWithText(getString(R.string.feature_settings_impl_dynamic_color_preference))
            .assertDoesNotExist()
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_impl_dynamic_color_yes)).assertDoesNotExist()
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_impl_dynamic_color_no)).assertDoesNotExist()
    }

    @Test
    fun whenStateIsSuccess_usesAndroidBrand_DynamicColorOptionIsNotDisplayed() {
        composeTestRule.setContent {
            SettingsDialog(
                settingsUiState = Success(defaultSettings()),
                onDismiss = {},
                onChangeDynamicColorPreference = {},
                onChangeThemeBrand = {},
                onChangeDarkThemeConfig = {},
                onChangePreferredWikiLanguage = {},
                onChangeWikiReaderTextScale = {},
            )
        }

        composeTestRule.onNodeWithText(getString(R.string.feature_settings_impl_dynamic_color_preference))
            .assertDoesNotExist()
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_impl_dynamic_color_yes)).assertDoesNotExist()
        composeTestRule.onNodeWithText(getString(R.string.feature_settings_impl_dynamic_color_no)).assertDoesNotExist()
    }
}
