package com.example.movieapplication.ui.login

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.movieapplication.ui.Login.LoginActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<LoginActivity>()

    @Test
    fun loginScreenElementsAreVisible() {

        // 🟢 تحقق من وجود العنوان
        composeRule.onNodeWithTag("login_title")
            .assertExists()
            .assertIsDisplayed()

        // 🟢 Email Field
        composeRule.onNodeWithTag("email_field")
            .assertExists()
            .assertIsDisplayed()

        // 🟢 Password Field
        composeRule.onNodeWithTag("password_field")
            .assertExists()
            .assertIsDisplayed()

        // 🟢 Forgot Password
        composeRule.onNodeWithTag("forgot_password_text")
            .assertExists()
            .assertIsDisplayed()

        // 🟢 Login Button
        composeRule.onNodeWithTag("login_button")
            .assertExists()
            .assertIsDisplayed()

        // 🟢 Sign Up Text
        composeRule.onNodeWithTag("sign_up_text")
            .assertExists()
            .assertIsDisplayed()
    }


    @Test
    fun loginButton_clickWithValidData() {

        // 📝 اكتب Email
        composeRule.onNodeWithTag("email_field")
            .performTextInput("test@email.com")

        // 📝 اكتب Password
        composeRule.onNodeWithTag("password_field")
            .performTextInput("123456")

        // 🔘 اضغط Login
        composeRule.onNodeWithTag("login_button")
            .performClick()


    }
}
