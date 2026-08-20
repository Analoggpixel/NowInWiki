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

package com.google.samples.apps.nowinandroid.core.datastore

import android.util.Log
import androidx.datastore.core.DataStore
import com.google.samples.apps.nowinandroid.core.model.data.DarkThemeConfig
import com.google.samples.apps.nowinandroid.core.model.data.ThemeBrand
import com.google.samples.apps.nowinandroid.core.model.data.UserData
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import com.google.samples.apps.nowinandroid.core.model.data.WikiReaderTextScale
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class NiaPreferencesDataSource @Inject constructor(
    private val userPreferences: DataStore<UserPreferences>,
) {
    val userData = userPreferences.data
        .map {
            UserData(
                themeBrand = when (it.themeBrand) {
                    null,
                    ThemeBrandProto.THEME_BRAND_UNSPECIFIED,
                    ThemeBrandProto.UNRECOGNIZED,
                    ThemeBrandProto.THEME_BRAND_DEFAULT,
                    -> ThemeBrand.DEFAULT
                    ThemeBrandProto.THEME_BRAND_ANDROID -> ThemeBrand.ANDROID
                },
                darkThemeConfig = when (it.darkThemeConfig) {
                    null,
                    DarkThemeConfigProto.DARK_THEME_CONFIG_UNSPECIFIED,
                    DarkThemeConfigProto.UNRECOGNIZED,
                    DarkThemeConfigProto.DARK_THEME_CONFIG_FOLLOW_SYSTEM,
                    ->
                        DarkThemeConfig.FOLLOW_SYSTEM
                    DarkThemeConfigProto.DARK_THEME_CONFIG_LIGHT ->
                        DarkThemeConfig.LIGHT
                    DarkThemeConfigProto.DARK_THEME_CONFIG_DARK -> DarkThemeConfig.DARK
                },
                useDynamicColor = it.useDynamicColor,
                preferredWikiLanguage = WikiLanguage.fromCode(
                    if (it.preferredWikiLanguage.isBlank()) WikiLanguage.CHINESE.code
                    else it.preferredWikiLanguage,
                ),
                wikiReaderTextScale = it.wikiReaderTextScale.toExternalModel(),
                shouldHideOnboarding = it.shouldHideOnboarding,
            )
        }

    suspend fun setThemeBrand(themeBrand: ThemeBrand) {
        userPreferences.updateData {
            it.copy {
                this.themeBrand = when (themeBrand) {
                    ThemeBrand.DEFAULT -> ThemeBrandProto.THEME_BRAND_DEFAULT
                    ThemeBrand.ANDROID -> ThemeBrandProto.THEME_BRAND_ANDROID
                }
            }
        }
    }

    suspend fun setDynamicColorPreference(useDynamicColor: Boolean) {
        userPreferences.updateData {
            it.copy { this.useDynamicColor = useDynamicColor }
        }
    }

    suspend fun setPreferredWikiLanguage(preferredWikiLanguage: WikiLanguage) {
        userPreferences.updateData {
            it.copy { this.preferredWikiLanguage = preferredWikiLanguage.code }
        }
    }

    suspend fun setWikiReaderTextScale(wikiReaderTextScale: WikiReaderTextScale) {
        userPreferences.updateData {
            it.copy { this.wikiReaderTextScale = wikiReaderTextScale.toProto() }
        }
    }

    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        userPreferences.updateData {
            it.copy {
                this.darkThemeConfig = when (darkThemeConfig) {
                    DarkThemeConfig.FOLLOW_SYSTEM ->
                        DarkThemeConfigProto.DARK_THEME_CONFIG_FOLLOW_SYSTEM
                    DarkThemeConfig.LIGHT -> DarkThemeConfigProto.DARK_THEME_CONFIG_LIGHT
                    DarkThemeConfig.DARK -> DarkThemeConfigProto.DARK_THEME_CONFIG_DARK
                }
            }
        }
    }

    suspend fun setShouldHideOnboarding(shouldHideOnboarding: Boolean) {
        try {
            userPreferences.updateData {
                it.copy { this.shouldHideOnboarding = shouldHideOnboarding }
            }
        } catch (ioException: IOException) {
            Log.e("NiaPreferences", "Failed to update user preferences", ioException)
        }
    }
}

private fun WikiReaderTextScaleProto?.toExternalModel(): WikiReaderTextScale =
    when (this) {
        null,
        WikiReaderTextScaleProto.WIKI_READER_TEXT_SCALE_UNSPECIFIED,
        WikiReaderTextScaleProto.UNRECOGNIZED,
        WikiReaderTextScaleProto.WIKI_READER_TEXT_SCALE_DEFAULT,
        -> WikiReaderTextScale.DEFAULT
        WikiReaderTextScaleProto.WIKI_READER_TEXT_SCALE_SMALL -> WikiReaderTextScale.SMALL
        WikiReaderTextScaleProto.WIKI_READER_TEXT_SCALE_LARGE -> WikiReaderTextScale.LARGE
        WikiReaderTextScaleProto.WIKI_READER_TEXT_SCALE_EXTRA_LARGE -> WikiReaderTextScale.EXTRA_LARGE
    }

private fun WikiReaderTextScale.toProto(): WikiReaderTextScaleProto =
    when (this) {
        WikiReaderTextScale.SMALL -> WikiReaderTextScaleProto.WIKI_READER_TEXT_SCALE_SMALL
        WikiReaderTextScale.DEFAULT -> WikiReaderTextScaleProto.WIKI_READER_TEXT_SCALE_DEFAULT
        WikiReaderTextScale.LARGE -> WikiReaderTextScaleProto.WIKI_READER_TEXT_SCALE_LARGE
        WikiReaderTextScale.EXTRA_LARGE -> WikiReaderTextScaleProto.WIKI_READER_TEXT_SCALE_EXTRA_LARGE
    }
