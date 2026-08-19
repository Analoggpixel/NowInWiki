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

package com.google.samples.apps.nowinandroid.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.espresso.NoActivityResumedException
import com.google.samples.apps.nowinandroid.MainActivity
import com.google.samples.apps.nowinandroid.R
import com.google.samples.apps.nowinandroid.core.rules.GrantPostNotificationsPermissionRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.google.samples.apps.nowinandroid.feature.bookmarks.api.R as BookmarksR
import com.google.samples.apps.nowinandroid.feature.foryou.api.R as FeatureForyouR
import com.google.samples.apps.nowinandroid.feature.settings.impl.R as SettingsR

/**
 * Tests all the navigation flows that are handled by the navigation library.
 */
@HiltAndroidTest
class NavigationTest {

    /**
     * Manages the components' state and is used to perform injection on your test
     */
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    /**
     * Grant [android.Manifest.permission.POST_NOTIFICATIONS] permission.
     */
    @get:Rule(order = 1)
    val postNotificationsPermission = GrantPostNotificationsPermissionRule()

    /**
     * Use the primary activity to initialize the app normally.
     */
    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    // The strings used for matching in these tests
    private val navigateUp by composeTestRule.stringResource(FeatureForyouR.string.feature_foryou_api_navigate_up)
    private val forYou by composeTestRule.stringResource(FeatureForyouR.string.feature_foryou_api_title)
    private val appName by composeTestRule.stringResource(R.string.app_name)
    private val saved by composeTestRule.stringResource(BookmarksR.string.feature_bookmarks_api_title)
    private val profile by composeTestRule.stringResource(SettingsR.string.feature_settings_impl_profile_tab)
    private val settings by composeTestRule.stringResource(SettingsR.string.feature_settings_impl_top_app_bar_action_icon_description)
    private val brand by composeTestRule.stringResource(SettingsR.string.feature_settings_impl_brand_android)
    private val ok by composeTestRule.stringResource(SettingsR.string.feature_settings_impl_dismiss_dialog_button_text)

    @Before
    fun setup() = hiltRule.inject()

    @Test
    fun firstScreen_isForYou() {
        composeTestRule.apply {
            // VERIFY for you is selected
            onNodeWithText(forYou).assertIsSelected()
        }
    }

    /**
     * When reselecting a tab, it should show that tab's start destination and restore its state.
     */
    @Test
    fun navigationBar_reselectTab_keepsState() {
        composeTestRule.apply {
            // WHEN the user taps the For You navigation bar item
            onNodeWithText(forYou).performClick()
            // THEN For You remains selected
            onNodeWithText(forYou).assertIsSelected()
        }
    }

    /*
     * Top level destinations should never show an up affordance.
     */
    @Test
    fun topLevelDestinations_doNotShowUpArrow() {
        composeTestRule.apply {
            // GIVEN the user is on any of the top level destinations, THEN the Up arrow is not shown.
            onNodeWithContentDescription(navigateUp).assertDoesNotExist()

            onNodeWithText(saved).performClick()
            onNodeWithContentDescription(navigateUp).assertDoesNotExist()

            onNodeWithText(profile).performClick()
            onNodeWithContentDescription(navigateUp).assertDoesNotExist()
        }
    }

    @Test
    fun topLevelDestinations_showTopBarWithTitle() {
        composeTestRule.apply {
            // Verify that the top bar contains the app name on the first screen.
            onNodeWithText(appName).assertExists()

            // Go to the saved tab, verify that the top bar contains "saved". This means
            // we'll have 2 elements with the text "saved" on screen. One in the top bar, and
            // one in the bottom navigation.
            onNodeWithText(saved).performClick()
            onAllNodesWithText(saved).assertCountEquals(2)

            // Profile hides the shared top app bar.
            onNodeWithText(profile).performClick()
            onNodeWithText(appName).assertDoesNotExist()
        }
    }

    @Test
    fun topLevelDestinations_showSettingsIcon() {
        composeTestRule.apply {
            onNodeWithContentDescription(settings).assertExists()

            onNodeWithText(saved).performClick()
            onNodeWithContentDescription(settings).assertExists()

            // Profile hides the shared top app bar (and its settings icon).
            onNodeWithText(profile).performClick()
            onNodeWithContentDescription(settings).assertDoesNotExist()
        }
    }

    @Test
    fun whenSettingsIconIsClicked_settingsDialogIsShown() {
        composeTestRule.apply {
            onNodeWithContentDescription(settings).performClick()

            // Check that one of the settings is actually displayed.
            onNodeWithText(brand).assertExists()
        }
    }

    @Test
    fun whenSettingsDialogDismissed_previousScreenIsDisplayed() {
        composeTestRule.apply {
            // Navigate to the saved screen, open the settings dialog, then close it.
            onNodeWithText(saved).performClick()
            onNodeWithContentDescription(settings).performClick()
            onNodeWithText(ok).performClick()

            // Check that the saved screen is still visible and selected.
            onNode(hasText(saved) and hasTestTag("NiaNavItem")).assertIsSelected()
        }
    }

    /*
     * There should always be at most one instance of a top-level destination at the same time.
     */
    @Test(expected = NoActivityResumedException::class)
    fun homeDestination_back_quitsApp() {
        composeTestRule.apply {
            // GIVEN the user navigates to the Profile destination
            onNodeWithText(profile).performClick()
            // and then navigates to the For you destination
            onNodeWithText(forYou).performClick()
            // WHEN the user uses the system button/gesture to go back
            Espresso.pressBack()
            // THEN the app quits
        }
    }

    /*
     * When pressing back from any top level destination except "For you", the app navigates back
     * to the "For you" destination, no matter which destinations you visited in between.
     */
    @Test
    fun navigationBar_backFromAnyDestination_returnsToForYou() {
        composeTestRule.apply {
            // GIVEN the user navigated to the Profile destination
            onNodeWithText(profile).performClick()
            // WHEN the user uses the system button/gesture to go back,
            Espresso.pressBack()
            // THEN the app shows the For You destination
            onNodeWithText(forYou).assertExists()
        }
    }
}
