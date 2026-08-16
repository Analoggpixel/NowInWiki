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

package com.google.samples.apps.nowinandroid.feature.bookmarks.impl

import com.google.samples.apps.nowinandroid.core.data.repository.WikiBookmarkRepository
import com.google.samples.apps.nowinandroid.core.domain.CreateWikiBookmarkFolderUseCase
import com.google.samples.apps.nowinandroid.core.domain.ObserveWikiBookmarkFoldersUseCase
import com.google.samples.apps.nowinandroid.core.domain.RenameWikiBookmarkFolderUseCase
import com.google.samples.apps.nowinandroid.core.domain.ToggleWikiBookmarkUseCase
import com.google.samples.apps.nowinandroid.core.model.data.WikiBookmark
import com.google.samples.apps.nowinandroid.core.model.data.WikiBookmarkFolder
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import com.google.samples.apps.nowinandroid.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class BookmarksViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val foldersFlow = MutableStateFlow<List<WikiBookmarkFolder>>(emptyList())
    private var nextFolderId = 100L
    private val fakeRepository = object : WikiBookmarkRepository {
        override fun observeFolders(): Flow<List<WikiBookmarkFolder>> = foldersFlow
        override fun observeFolder(folderId: Long) = MutableStateFlow(null)
        override fun observeIsBookmarked(title: String, language: WikiLanguage) =
            MutableStateFlow(false)
        override fun observeIsBookmarkedInFolder(
            folderId: Long,
            title: String,
            language: WikiLanguage,
        ) = MutableStateFlow(false)
        override suspend fun createFolder(name: String, description: String?, sortOrder: Int?): Long {
            val id = nextFolderId++
            foldersFlow.update { current ->
                current + WikiBookmarkFolder(id = id, name = name, bookmarks = emptyList())
            }
            return id
        }
        override suspend fun updateFolder(
            folderId: Long,
            name: String,
            description: String?,
            sortOrder: Int?,
        ) = Unit
        override suspend fun deleteFolder(folderId: Long) = Unit
        override suspend fun upsertBookmark(folderId: Long, bookmark: WikiBookmark) = Unit
        override suspend fun removeBookmark(
            folderId: Long,
            title: String,
            language: WikiLanguage,
        ) = Unit
        override suspend fun removeBookmarkById(bookmarkId: Long) = Unit
        override suspend fun moveBookmark(bookmarkId: Long, toFolderId: Long) = Unit
        override suspend fun getOrCreateDefaultFolder() = 1L
        override suspend fun removeBookmarkEverywhere(title: String, language: WikiLanguage) = Unit
        override suspend fun toggleBookmark(
            title: String,
            language: WikiLanguage,
            folderId: Long?,
            description: String?,
            thumbnailUrl: String?,
        ) = Unit
    }

    private lateinit var viewModel: BookmarksViewModel

    @Before
    fun setup() {
        viewModel = BookmarksViewModel(
            observeWikiBookmarkFoldersUseCase = ObserveWikiBookmarkFoldersUseCase(fakeRepository),
            toggleWikiBookmarkUseCase = ToggleWikiBookmarkUseCase(fakeRepository),
            createWikiBookmarkFolderUseCase = CreateWikiBookmarkFolderUseCase(fakeRepository),
            renameWikiBookmarkFolderUseCase = RenameWikiBookmarkFolderUseCase(fakeRepository),
        )
    }

    @Test
    fun stateIsInitiallyLoading() = runTest {
        assertEquals(WikiBookmarksUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun emptyFolders_emitsEmpty() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        foldersFlow.value = emptyList()
        assertEquals(WikiBookmarksUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun foldersWithBookmarks_emitsSuccess() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        foldersFlow.update {
            listOf(
                WikiBookmarkFolder(
                    id = 1,
                    name = "默认收藏",
                    bookmarks = listOf(
                        WikiBookmark(
                            id = 11,
                            folderId = 1,
                            title = "Kotlin",
                            language = WikiLanguage.ENGLISH,
                            bookmarkedAt = 1L,
                        ),
                    ),
                ),
            )
        }

        val state = viewModel.uiState.value
        assertIs<WikiBookmarksUiState.Success>(state)
        assertEquals(1, state.folders.size)
        assertEquals("Kotlin", state.folders.first().bookmarks.first().title)
    }

    @Test
    fun startRename_setsRenameState() = runTest {
        viewModel.startRenameFolder(folderId = 1L, currentName = "默认收藏")
        assertEquals(
            FolderRenameUiState(folderId = 1L, draftName = "默认收藏"),
            viewModel.renameState.value,
        )

        viewModel.onRenameDraftChanged("旅行")
        assertEquals("旅行", viewModel.renameState.value?.draftName)

        viewModel.confirmRenameFolder()
        assertNull(viewModel.renameState.value)
    }

    @Test
    fun cancelRename_clearsRenameState() = runTest {
        viewModel.startRenameFolder(folderId = 1L, currentName = "默认收藏")
        viewModel.cancelRenameFolder()
        assertNull(viewModel.renameState.value)
    }

    @Test
    fun createBookmarkFolder_usesFolderCountPlusOneAsDefaultName() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        foldersFlow.value = listOf(
            WikiBookmarkFolder(id = 1, name = "默认收藏", bookmarks = emptyList()),
            WikiBookmarkFolder(id = 2, name = "旅行", bookmarks = emptyList()),
        )

        viewModel.createBookmarkFolder()

        // 已有 2 个夹 → 默认名「收藏夹3」
        assertEquals(
            FolderRenameUiState(folderId = 100L, draftName = "收藏夹3"),
            viewModel.renameState.value,
        )
        assertEquals("收藏夹3", foldersFlow.value.last().name)
    }
}
