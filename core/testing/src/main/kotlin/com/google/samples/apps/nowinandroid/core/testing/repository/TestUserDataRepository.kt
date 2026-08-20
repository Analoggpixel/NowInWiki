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

package com.google.samples.apps.nowinandroid.core.testing.repository

import com.google.samples.apps.nowinandroid.core.data.repository.UserDataRepository
import com.google.samples.apps.nowinandroid.core.model.data.DarkThemeConfig
import com.google.samples.apps.nowinandroid.core.model.data.ThemeBrand
import com.google.samples.apps.nowinandroid.core.model.data.UserData
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import com.google.samples.apps.nowinandroid.core.model.data.WikiReaderTextScale
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNotNull

val emptyUserData = UserData(
    themeBrand = ThemeBrand.DEFAULT,
    darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
    useDynamicColor = false,
    preferredWikiLanguage = WikiLanguage.CHINESE,
    wikiReaderTextScale = WikiReaderTextScale.DEFAULT,
    shouldHideOnboarding = false,
)

class TestUserDataRepository : UserDataRepository {
    /**
     * The backing hot flow for user data in tests.
     */
    private val _userData = MutableSharedFlow<UserData>(replay = 1, onBufferOverflow = DROP_OLDEST)

    private val currentUserData get() = _userData.replayCache.firstOrNull() ?: emptyUserData

    override val userData: Flow<UserData> = _userData.filterNotNull()

    override suspend fun setThemeBrand(themeBrand: ThemeBrand) {
        currentUserData.let { current ->
            _userData.tryEmit(current.copy(themeBrand = themeBrand))
        }
    }

    override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        currentUserData.let { current ->
            _userData.tryEmit(current.copy(darkThemeConfig = darkThemeConfig))
        }
    }

    override suspend fun setDynamicColorPreference(useDynamicColor: Boolean) {
        currentUserData.let { current ->
            _userData.tryEmit(current.copy(useDynamicColor = useDynamicColor))
        }
    }

    override suspend fun setPreferredWikiLanguage(preferredWikiLanguage: WikiLanguage) {
        currentUserData.let { current ->
            _userData.tryEmit(current.copy(preferredWikiLanguage = preferredWikiLanguage))
        }
    }

    override suspend fun setWikiReaderTextScale(wikiReaderTextScale: WikiReaderTextScale) {
        currentUserData.let { current ->
            _userData.tryEmit(current.copy(wikiReaderTextScale = wikiReaderTextScale))
        }
    }

    override suspend fun setShouldHideOnboarding(shouldHideOnboarding: Boolean) {
        currentUserData.let { current ->
            _userData.tryEmit(current.copy(shouldHideOnboarding = shouldHideOnboarding))
        }
    }

    /**
     * A test-only API to allow setting of user data directly.
     */
    fun setUserData(userData: UserData) {
        _userData.tryEmit(userData)
    }
}
