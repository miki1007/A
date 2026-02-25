package com.mikix

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class NavigationSmokeTest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()

    @Test fun bottomTabsAreVisible() {
        composeRule.onNodeWithText("Home").assertIsDisplayed()
        composeRule.onNodeWithText("Log").assertIsDisplayed()
        composeRule.onNodeWithText("Progress").assertIsDisplayed()
        composeRule.onNodeWithText("Community").assertIsDisplayed()
        composeRule.onNodeWithText("Profile").assertIsDisplayed()
    }
}
