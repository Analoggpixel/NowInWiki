/*
 * Copyright 2024 The Android Open Source Project
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

package com.google.samples.apps.nowinandroid.core.database.di

import com.google.samples.apps.nowinandroid.core.database.NiaDatabase
import com.google.samples.apps.nowinandroid.core.database.dao.RecentSearchQueryDao
import com.google.samples.apps.nowinandroid.core.database.dao.WikiBookmarkDao
import com.google.samples.apps.nowinandroid.core.database.dao.WikiBookmarkFolderDao
import com.google.samples.apps.nowinandroid.core.database.dao.WikiHistoryDao
import com.google.samples.apps.nowinandroid.core.database.dao.WikiSearchDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object DaosModule {
    @Provides
    fun providesRecentSearchQueryDao(
        database: NiaDatabase,
    ): RecentSearchQueryDao = database.recentSearchQueryDao()

    @Provides
    fun providesWikiBookmarkFolderDao(
        database: NiaDatabase,
    ): WikiBookmarkFolderDao = database.wikiBookmarkFolderDao()

    @Provides
    fun providesWikiBookmarkDao(
        database: NiaDatabase,
    ): WikiBookmarkDao = database.wikiBookmarkDao()

    @Provides
    fun providesWikiHistoryDao(
        database: NiaDatabase,
    ): WikiHistoryDao = database.wikiHistoryDao()

    @Provides
    fun providesWikiSearchDao(
        database: NiaDatabase,
    ): WikiSearchDao = database.wikiSearchDao()
}
