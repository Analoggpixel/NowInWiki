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

package com.google.samples.apps.nowinandroid.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.google.samples.apps.nowinandroid.core.model.data.AppUiLanguage

/**
 * Applies [AppUiLanguage] to the process via [AppCompatDelegate.setApplicationLocales].
 *
 * Requires an [androidx.appcompat.app.AppCompatActivity] (or AppCompat theme) so locale
 * changes recreate the activity and reload `values` / `values-zh` resources.
 */
object AppUiLanguageApplier {

    fun AppUiLanguage.toLocaleList(): LocaleListCompat = when (this) {
        AppUiLanguage.FOLLOW_SYSTEM -> LocaleListCompat.getEmptyLocaleList()
        AppUiLanguage.ENGLISH -> LocaleListCompat.forLanguageTags("en")
        AppUiLanguage.CHINESE_SIMPLIFIED -> LocaleListCompat.forLanguageTags("zh")
    }

    /**
     * Updates the app locale when [language] differs from the current application locales.
     * No-op when already applied (avoids recreate loops after configuration change).
     */
    fun applyIfNeeded(language: AppUiLanguage) {
        val target = language.toLocaleList()
        val current = AppCompatDelegate.getApplicationLocales()
        if (current.toLanguageTags() != target.toLanguageTags()) {
            AppCompatDelegate.setApplicationLocales(target)
        }
    }
}
