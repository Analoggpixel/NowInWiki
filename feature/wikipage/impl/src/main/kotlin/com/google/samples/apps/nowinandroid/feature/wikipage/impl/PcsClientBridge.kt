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

package com.google.samples.apps.nowinandroid.feature.wikipage.impl

import android.util.Log
import android.webkit.JavascriptInterface
import org.json.JSONObject

/**
 * Android bridge expected by PCS JS (`window.pcsClient`).
 *
 * PCS intercepts in-article taps, calls [preventDefault], and notifies native via
 * [onReceiveMessage] instead of letting [android.webkit.WebViewClient.shouldOverrideUrlLoading]
 * run. Without this bridge, link taps appear to do nothing.
 *
 * See: https://github.com/wikimedia/mediawiki-services-mobileapps/blob/master/pagelib/docs/pcs/pcs.md
 */
internal class PcsClientBridge(
    private val onHref: (String) -> Unit,
) {
    /**
     * Returned to PCS as `document.pcsSetupSettings` (JSON string).
     */
    @JavascriptInterface
    fun getSetupSettings(): String =
        JSONObject()
            .put("platform", "android")
            .put("version", 1)
            .put("theme", "default")
            .put("loadImages", true)
            .toString()

    /**
     * PCS posts interaction payloads here, e.g. `{"action":"link","data":{"href":"./雅典"}}`.
     */
    @JavascriptInterface
    fun onReceiveMessage(message: String?) {
        Log.d(WIKI_PAGE_NAV_TAG, "pcsClient.onReceiveMessage raw=$message")
        if (message.isNullOrBlank()) return

        runCatching {
            val root = JSONObject(message)
            val action = root.optString("action")
            val data = root.optJSONObject("data")
            val href = data?.optString("href").orEmpty().ifBlank {
                root.optString("href")
            }
            Log.d(WIKI_PAGE_NAV_TAG, "pcsClient action=$action href=$href")
            when (action) {
                "link", "" -> {
                    if (href.isNotBlank()) onHref(href)
                }
                else -> {
                    // Future-proofing per PCS docs: unknown actions may still carry href.
                    if (href.isNotBlank() && looksLikeNavigationHref(href)) {
                        onHref(href)
                    }
                }
            }
        }.onFailure { error ->
            Log.e(WIKI_PAGE_NAV_TAG, "pcsClient parse failed message=$message", error)
        }
    }

    private fun looksLikeNavigationHref(href: String): Boolean {
        val t = href.trim()
        return t.startsWith("./") ||
            t.startsWith("/wiki/") ||
            t.startsWith("http://") ||
            t.startsWith("https://") ||
            t.startsWith("//")
    }
}

private const val WIKI_PAGE_NAV_TAG = "WikiPageNav"
