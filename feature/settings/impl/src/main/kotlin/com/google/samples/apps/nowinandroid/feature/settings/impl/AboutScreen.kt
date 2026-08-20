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

package com.google.samples.apps.nowinandroid.feature.settings.impl

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.samples.apps.nowinandroid.core.designsystem.icon.NiaIcons
import com.google.samples.apps.nowinandroid.core.designsystem.theme.NiaTheme
import com.google.samples.apps.nowinandroid.core.ui.DevicePreviews
import com.google.samples.apps.nowinandroid.core.ui.TrackScreenViewEvent
import com.google.samples.apps.nowinandroid.feature.settings.impl.R.string

private enum class AboutDetail {
    ContentLicense,
    Privacy,
    OpenSource,
    Contact,
}

@Composable
internal fun AboutScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val versionName = remember(context) {
        runCatching {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0),
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName
        }.getOrNull().orEmpty()
    }
    var openDetail by rememberSaveable { mutableStateOf<AboutDetail?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
        AboutTopBar(
            title = stringResource(string.feature_settings_impl_profile_about),
            onBackClick = onBackClick,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(string.feature_settings_impl_about_app_name),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(string.feature_settings_impl_about_unofficial),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(20.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    AboutMenuRow(
                        label = stringResource(string.feature_settings_impl_about_content_license),
                        supportingText = stringResource(
                            string.feature_settings_impl_about_content_license_summary,
                        ),
                        onClick = { openDetail = AboutDetail.ContentLicense },
                    )
                    AboutMenuDivider()
                    AboutMenuRow(
                        label = stringResource(string.feature_settings_impl_about_privacy),
                        onClick = { openDetail = AboutDetail.Privacy },
                    )
                    AboutMenuDivider()
                    AboutMenuRow(
                        label = stringResource(string.feature_settings_impl_about_open_source),
                        onClick = { openDetail = AboutDetail.OpenSource },
                    )
                    AboutMenuDivider()
                    AboutMenuRow(
                        label = stringResource(string.feature_settings_impl_about_third_party_licenses),
                        onClick = {
                            context.startActivity(
                                Intent(context, OssLicensesMenuActivity::class.java),
                            )
                        },
                    )
                    AboutMenuDivider()
                    AboutMenuRow(
                        label = stringResource(string.feature_settings_impl_about_contact),
                        onClick = { openDetail = AboutDetail.Contact },
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(
                    string.feature_settings_impl_about_version,
                    versionName.ifBlank {
                        stringResource(string.feature_settings_impl_about_version_unknown)
                    },
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(16.dp))
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
        }
    }

    when (openDetail) {
        AboutDetail.ContentLicense -> {
            AboutDetailDialog(
                title = stringResource(string.feature_settings_impl_about_content_license),
                body = stringResource(string.feature_settings_impl_about_content_license_body),
                linkLabel = stringResource(string.feature_settings_impl_about_content_license_link),
                onLinkClick = {
                    uriHandler.openUri(CC_BY_SA_DEED_URL)
                },
                onDismiss = { openDetail = null },
            )
        }
        AboutDetail.Privacy -> {
            AboutDetailDialog(
                title = stringResource(string.feature_settings_impl_about_privacy),
                body = stringResource(string.feature_settings_impl_about_privacy_body),
                onDismiss = { openDetail = null },
            )
        }
        AboutDetail.OpenSource -> {
            AboutDetailDialog(
                title = stringResource(string.feature_settings_impl_about_open_source),
                body = stringResource(string.feature_settings_impl_about_open_source_body),
                linkLabel = stringResource(string.feature_settings_impl_about_nia_link),
                onLinkClick = {
                    uriHandler.openUri(NIA_GITHUB_URL)
                },
                onDismiss = { openDetail = null },
            )
        }
        AboutDetail.Contact -> {
            AboutDetailDialog(
                title = stringResource(string.feature_settings_impl_about_contact),
                body = stringResource(string.feature_settings_impl_about_contact_body),
                linkLabel = stringResource(string.feature_settings_impl_about_contact_link),
                onLinkClick = {
                    uriHandler.openUri(PROJECT_ISSUES_URL)
                },
                onDismiss = { openDetail = null },
            )
        }
        null -> Unit
    }

    TrackScreenViewEvent(screenName = "About")
}

@Composable
private fun AboutTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = NiaIcons.ArrowBack,
                contentDescription = stringResource(string.feature_settings_impl_history_back),
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
        )
    }
}

@Composable
private fun AboutMenuRow(
    label: String,
    onClick: () -> Unit,
    supportingText: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
            )
            if (supportingText != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AboutMenuDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    )
}

@Composable
private fun AboutDetailDialog(
    title: String,
    body: String,
    onDismiss: () -> Unit,
    linkLabel: String? = null,
    onLinkClick: (() -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(body)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(string.feature_settings_impl_dismiss_dialog_button_text))
            }
        },
        dismissButton = if (linkLabel != null && onLinkClick != null) {
            {
                TextButton(
                    onClick = {
                        onLinkClick()
                        onDismiss()
                    },
                ) {
                    Text(linkLabel)
                }
            }
        } else {
            null
        },
    )
}

@DevicePreviews
@Composable
private fun AboutScreenPreview() {
    NiaTheme {
        AboutScreen(onBackClick = {})
    }
}

private const val CC_BY_SA_DEED_URL = "https://creativecommons.org/licenses/by-sa/4.0/deed.zh"
private const val NIA_GITHUB_URL = "https://github.com/android/nowinandroid"
private const val PROJECT_ISSUES_URL = "https://github.com/Analoggpixel/NowInWiki/issues"
