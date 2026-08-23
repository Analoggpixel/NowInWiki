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

package com.google.samples.apps.nowinandroid.core.data.model

/** Turns protocol-relative `//...` URLs into absolute `https://...` URLs. */
internal fun String.toAbsoluteWikiUrl(): String =
    when {
        startsWith("//") -> "https:$this"
        else -> this
    }

/** Rewrites Commons thumbnail path size segments (e.g. `/50px-` → `/250px-`). */
internal fun String.toHighResolution(size: Int = 250): String =
    replace(
        Regex("/\\d+px-"),
        "/${size}px-",
    )
