package com.example.movieapplication.ui.Signin

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class SigninScreenInstrumentedTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<SigninActivity>()

    @Test
    fun test_allFieldsDisplayed() {
        composeTestRule.onNodeWithTag("signup_title").assertIsDisplayed()
        composeTestRule.onNodeWithTag("username_field", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("email_field", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("password_field", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("confirm_password_field", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("sign_up_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("login_text").assertIsDisplayed()
    }

    @Test
    fun test_typingTextIntoFields() {
        composeTestRule.onNodeWithTag("username_field", useUnmergedTree = true).performTextInput("Mohamed")
        composeTestRule.onNodeWithTag("email_field", useUnmergedTree = true).performTextInput("test@test.com")
        composeTestRule.onNodeWithTag("password_field", useUnmergedTree = true).performTextInput("Password123")
        composeTestRule.onNodeWithTag("confirm_password_field", useUnmergedTree = true).performTextInput("Password123")

        composeTestRule.onNodeWithTag("username_field", useUnmergedTree = true).assertTextContains("Mohamed")
        composeTestRule.onNodeWithTag("email_field", useUnmergedTree = true).assertTextContains("test@test.com")
    }

    @Test
    fun test_togglePasswordVisibility() {
        // toggle password field visibility (trailing icon usually last child)
        composeTestRule.onNodeWithTag("password_field", useUnmergedTree = true)
            .onChildAt(2)
            .performClick()

        // toggle confirm password field visibility
        composeTestRule.onNodeWithTag("confirm_password_field", useUnmergedTree = true)
            .onChildAt(2)
            .performClick()
    }

    @Test
    fun test_signUpButtonClick() {
        composeTestRule.onNodeWithTag("username_field", useUnmergedTree = true).performTextInput("user")
        composeTestRule.onNodeWithTag("email_field", useUnmergedTree = true).performTextInput("user@test.com")
        composeTestRule.onNodeWithTag("password_field", useUnmergedTree = true).performTextInput("Password1")
        composeTestRule.onNodeWithTag("confirm_password_field", useUnmergedTree = true).performTextInput("Password1")

        composeTestRule.onNodeWithTag("sign_up_button").performClick()
    }

    @Test
    fun test_loginTextClick() {
        composeTestRule.onNodeWithTag("login_text").performClick()
    }
}
