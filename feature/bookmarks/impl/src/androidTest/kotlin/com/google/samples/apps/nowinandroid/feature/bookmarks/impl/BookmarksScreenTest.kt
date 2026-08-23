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

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.samples.apps.nowinandroid.core.model.data.WikiBookmark
import com.google.samples.apps.nowinandroid.core.model.data.WikiBookmarkFolder
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import com.google.samples.apps.nowinandroid.feature.bookmarks.api.R
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertTrue

/**
 * UI tests for [BookmarksScreen] composable.
 */
class BookmarksScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun getString(id: Int) = composeTestRule.activity.resources.getString(id)

    @Test
    fun loading_showsLoadingSpinner() {
        composeTestRule.setContent {
            BookmarksScreen(
                uiState = WikiBookmarksUiState.Loading,
                onFolderClick = {},
            )
        }

        composeTestRule
            .onNodeWithContentDescription(getString(R.string.feature_bookmarks_api_loading_folders))
            .assertExists()
    }

    @Test
    fun empty_showsCreateFolderRow() {
        composeTestRule.setContent {
            BookmarksScreen(
                uiState = WikiBookmarksUiState.Empty,
                onFolderClick = {},
            )
        }

        composeTestRule
            .onNodeWithText(getString(R.string.feature_bookmarks_api_create_folder))
            .assertExists()
            .assertHasClickAction()
    }

    @Test
    fun success_showsFolderRow_notBookmarkTitle() {
        composeTestRule.setContent {
            BookmarksScreen(
                uiState = WikiBookmarksUiState.Success(folders = sampleFolders),
                onFolderClick = {},
            )
        }

        composeTestRule
            .onNodeWithText(getString(R.string.feature_bookmarks_api_default_folder_name))
            .assertExists()
            .assertHasClickAction()
        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.resources
                    .getQuantityString(R.plurals.feature_bookmarks_api_folder_item_count, 1, 1),
            )
            .assertExists()
        composeTestRule.onNodeWithText("Kotlin").assertDoesNotExist()
    }

    @Test
    fun success_clickFolder_opensFolder() {
        var openedFolderId: Long? = null

        composeTestRule.setContent {
            BookmarksScreen(
                uiState = WikiBookmarksUiState.Success(folders = sampleFolders),
                onFolderClick = { folderId -> openedFolderId = folderId },
            )
        }

        composeTestRule
            .onNodeWithText(getString(R.string.feature_bookmarks_api_default_folder_name))
            .performClick()
        assertTrue(openedFolderId == 1L)
    }

    private val sampleFolders = listOf(
        WikiBookmarkFolder(
            id = 1,
            name = "Saved",
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
