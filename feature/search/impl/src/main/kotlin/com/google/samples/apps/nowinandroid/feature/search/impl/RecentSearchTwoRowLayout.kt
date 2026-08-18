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

internal const val RECENT_SEARCH_ELLIPSIS = "…"

internal data class RecentSearchTwoRowLayout(
    val firstRow: List<RecentSearchQuery>,
    val secondRow: List<RecentSearchQuery>,
)

/**
 * Packs [queries] (already newest-first) into at most two rows.
 *
 * For each item: estimate chip width (capped by ellipsis) → if it fits the current
 * row, place it; otherwise wrap to row 2 (when on row 1) or stop (when already on row 2).
 */
internal fun takeRecentSearchesForTwoRows(
    queries: List<RecentSearchQuery>,
    maxWidthDp: Float,
    horizontalPaddingDp: Float = 12f,
    charWidthDp: Float = 14f,
    itemSpacingDp: Float = 8f,
): RecentSearchTwoRowLayout {
    if (queries.isEmpty() || maxWidthDp <= 0f) {
        return RecentSearchTwoRowLayout(firstRow = emptyList(), secondRow = emptyList())
    }

    val firstRow = mutableListOf<RecentSearchQuery>()
    val secondRow = mutableListOf<RecentSearchQuery>()
    var currentRow = firstRow
    var usedWidth = 0f

    for (item in queries) {
        val chipWidth = estimateRecentSearchChipWidthDp(
            query = item.query,
            maxChipWidthDp = maxWidthDp,
            horizontalPaddingDp = horizontalPaddingDp,
            charWidthDp = charWidthDp,
        )
        val isFirstInRow = currentRow.isEmpty()
        val occupied = if (isFirstInRow) chipWidth else chipWidth + itemSpacingDp

        when {
            isFirstInRow || occupied <= maxWidthDp - usedWidth -> {
                currentRow += item
                usedWidth += occupied
            }
            currentRow === firstRow -> {
                currentRow = secondRow
                usedWidth = 0f
                currentRow += item
                usedWidth += chipWidth
            }
            else -> break
        }
    }

    return RecentSearchTwoRowLayout(
        firstRow = firstRow,
        secondRow = secondRow,
    )
}

/**
 * Estimated Chip width. Text longer than one row is treated as prefix + ellipsis,
 * so the result never exceeds [maxChipWidthDp].
 */
internal fun estimateRecentSearchChipWidthDp(
    query: String,
    maxChipWidthDp: Float,
    horizontalPaddingDp: Float = 12f,
    charWidthDp: Float = 14f,
): Float {
    val displayText = recentSearchChipDisplayText(
        query = query,
        maxChipWidthDp = maxChipWidthDp,
        horizontalPaddingDp = horizontalPaddingDp,
        charWidthDp = charWidthDp,
    )
    return (horizontalPaddingDp * 2 + displayText.weightedCharCount() * charWidthDp)
        .coerceAtMost(maxChipWidthDp)
}

/** Text shown on the Chip; truncates with ellipsis when it would exceed one row. */
internal fun recentSearchChipDisplayText(
    query: String,
    maxChipWidthDp: Float,
    horizontalPaddingDp: Float = 12f,
    charWidthDp: Float = 14f,
): String {
    val maxTextWidthDp = (maxChipWidthDp - horizontalPaddingDp * 2)
        .coerceAtLeast(charWidthDp)
    val queryWidthDp = query.weightedCharCount() * charWidthDp
    if (queryWidthDp <= maxTextWidthDp) return query

    val ellipsisWidthDp = RECENT_SEARCH_ELLIPSIS.weightedCharCount() * charWidthDp
    val prefixBudgetDp = (maxTextWidthDp - ellipsisWidthDp).coerceAtLeast(0f)
    var usedDp = 0f
    val prefix = StringBuilder()
    for (ch in query) {
        val charDp = ch.weightedCharCount() * charWidthDp
        if (usedDp + charDp > prefixBudgetDp) break
        prefix.append(ch)
        usedDp += charDp
    }
    return prefix.append(RECENT_SEARCH_ELLIPSIS).toString()
}

private fun String.weightedCharCount(): Float =
    sumOf { it.weightedCharCount().toDouble() }.toFloat()

private fun Char.weightedCharCount(): Float =
    if (code > 0x7F) 1f else 0.55f
