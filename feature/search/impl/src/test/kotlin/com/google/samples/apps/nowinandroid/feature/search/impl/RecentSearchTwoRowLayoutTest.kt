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

package com.google.samples.apps.nowinandroid.feature.search.impl

import com.google.samples.apps.nowinandroid.core.model.data.RecentSearchQuery
import com.google.samples.apps.nowinandroid.core.model.data.WikiLanguage
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecentSearchTwoRowLayoutTest {

    @Test
    fun emptyQueries_returnsEmptyRows() {
        val layout = takeRecentSearchesForTwoRows(
            queries = emptyList(),
            maxWidthDp = 360f,
        )
        assertTrue(layout.firstRow.isEmpty())
        assertTrue(layout.secondRow.isEmpty())
    }

    @Test
    fun shortQueries_fillFirstRowThenSecond() {
        val queries = listOf("a", "b", "c", "d", "e").map(::queryOf)
        // Each chip ≈ 12*2 + 0.55*14 ≈ 31.7; +8 spacing ≈ 40. packed into 90 → ~2 per row
        val layout = takeRecentSearchesForTwoRows(
            queries = queries,
            maxWidthDp = 90f,
            horizontalPaddingDp = 12f,
            charWidthDp = 14f,
            itemSpacingDp = 8f,
        )
        assertEquals(2, layout.firstRow.size)
        assertEquals(2, layout.secondRow.size)
    }

    @Test
    fun overflowOnSecondRow_stops() {
        val queries = listOf("one", "two", "three", "four", "five").map(::queryOf)
        val layout = takeRecentSearchesForTwoRows(
            queries = queries,
            maxWidthDp = 90f,
        )
        val placed = layout.firstRow.size + layout.secondRow.size
        assertTrue(placed < queries.size)
        assertEquals(queries.take(placed), layout.firstRow + layout.secondRow)
    }

    @Test
    fun itemWiderThanRow_stillPlacedAsFirstInRow() {
        val queries = listOf(queryOf("这是一条特别特别长的搜索词"))
        val layout = takeRecentSearchesForTwoRows(
            queries = queries,
            maxWidthDp = 40f,
        )
        assertEquals(1, layout.firstRow.size)
        assertTrue(layout.secondRow.isEmpty())
    }

    @Test
    fun shortQuery_displayTextUnchanged() {
        assertEquals(
            "Kotlin",
            recentSearchChipDisplayText(
                query = "Kotlin",
                maxChipWidthDp = 360f,
            ),
        )
    }

    @Test
    fun longQuery_displayTextUsesEllipsisAndWidthIsCapped() {
        val query = "这是一条特别特别长的搜索词用来测试省略号"
        val maxChipWidthDp = 80f
        val display = recentSearchChipDisplayText(
            query = query,
            maxChipWidthDp = maxChipWidthDp,
        )
        assertTrue(display.endsWith(RECENT_SEARCH_ELLIPSIS))
        assertTrue(display.length < query.length)

        val width = estimateRecentSearchChipWidthDp(
            query = query,
            maxChipWidthDp = maxChipWidthDp,
        )
        assertTrue(width <= maxChipWidthDp)
    }

    private fun queryOf(text: String) = RecentSearchQuery(
        query = text,
        language = WikiLanguage.CHINESE,
    )
}
