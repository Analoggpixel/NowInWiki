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

package com.google.samples.apps.nowinandroid.core.domain

import com.google.samples.apps.nowinandroid.core.data.repository.WikiBookmarkRepository
import javax.inject.Inject

/**
 * 新建 Wiki 收藏夹。
 *
 * @return 新夹 id；名称为空时返回 `null` 且不写入。
 */
class CreateWikiBookmarkFolderUseCase @Inject constructor(
    private val wikiBookmarkRepository: WikiBookmarkRepository,
) {
    suspend operator fun invoke(
        name: String,
        description: String? = null,
        sortOrder: Int? = null,
    ): Long? {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return null

        return wikiBookmarkRepository.createFolder(
            name = trimmedName,
            description = description?.trim()?.takeIf { it.isNotEmpty() },
            sortOrder = sortOrder,
        )
    }
}
