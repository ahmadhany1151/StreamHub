package com.example.movieapplication.ui.Splash

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class SplashScreenInstrumentedTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<SplashActivity>()

    @Test
    fun test_allElementsDisplayed() {
        composeTestRule.onNodeWithTag("welcome_text", useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("app_name_text", useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("splash_description", useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("next_button", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun test_clickNextButton() {
        composeTestRule.onNodeWithTag("next_button", useUnmergedTree = true)
            .performClick()
        // تقدر تضيف تحقق إضافي بعد الضغط لو حابب
    }

    @Test
    fun test_textContents() {
        composeTestRule.onNodeWithTag("welcome_text", useUnmergedTree = true)
            .assertTextEquals("Welcome to")

        composeTestRule.onNodeWithTag("app_name_text", useUnmergedTree = true)
            .assertTextEquals("StreamHub")

        // تعديل باستخدم substring = true
        composeTestRule.onNodeWithTag("splash_description", useUnmergedTree = true)
            .assertTextContains("endless entertainment", substring = true)
    }
}
