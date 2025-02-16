package com.github.se.eventradar.signup

import android.app.Activity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.core.app.ActivityOptionsCompat
import com.github.se.eventradar.screens.SignupScreen
import com.github.se.eventradar.ui.login.SignUpScreen
import com.github.se.eventradar.ui.navigation.NavigationActions
import com.github.se.eventradar.ui.navigation.Route
import com.github.se.eventradar.viewmodel.LoginUiState
import com.github.se.eventradar.viewmodel.LoginViewModel
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dagger.hilt.android.testing.HiltAndroidTest
import io.github.kakaocup.compose.node.element.ComposeScreen.Companion.onComposeScreen
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class SignupSuccessTest : TestCase() {
  @get:Rule val composeTestRule = createComposeRule()

  // This rule automatic initializes lateinit properties with @MockK, @RelaxedMockK, etc.
  @get:Rule val mockkRule = MockKRule(this)

  // Relaxed mocks methods have a default implementation returning values
  @RelaxedMockK lateinit var mockNavActions: NavigationActions
  @RelaxedMockK lateinit var mockAuthenticationViewModel: LoginViewModel

  private val sampleSignUpUiState = MutableStateFlow(LoginUiState())

  @Before
  fun setUp() {
    // Launch the Signup screen
    every { mockAuthenticationViewModel.uiState } returns sampleSignUpUiState
    every { mockAuthenticationViewModel.validateFields() } returns true
    composeTestRule.setContent {
      val context = LocalContext.current
      // ActivityResultRegistry is responsible for handling the
      // contracts and launching the activity
      val registryOwner =
          object : ActivityResultRegistryOwner {
            override val activityResultRegistry =
                object : ActivityResultRegistry() {
                  override fun <I : Any?, O : Any?> onLaunch(
                      requestCode: Int,
                      contract: ActivityResultContract<I, O>,
                      input: I,
                      options: ActivityOptionsCompat?
                  ) {
                    val intent = contract.createIntent(context, input)
                    this.dispatchResult(requestCode, Activity.RESULT_OK, intent)
                  }
                }
          }

      CompositionLocalProvider(LocalActivityResultRegistryOwner provides registryOwner) {
        // any composable inside this block will now use our mock ActivityResultRegistry

        SignUpScreen(mockAuthenticationViewModel, mockNavActions)
      }
    }
  }

  @Test
  fun navigateToHomeScreenOnSuccessfulSignUp() = run {
    onComposeScreen<SignupScreen>(composeTestRule) {
      usernameTextField {
        performScrollTo()
        assertIsDisplayed()
        performTextInput("test")
      }
      sampleSignUpUiState.value = sampleSignUpUiState.value.copy(username = "test")
      nameTextField {
        performScrollTo()
        assertIsDisplayed()
        performTextInput("test")
      }
      sampleSignUpUiState.value = sampleSignUpUiState.value.copy(firstName = "test")
      surnameTextField {
        performScrollTo()
        assertIsDisplayed()
        performTextInput("test")
      }
      sampleSignUpUiState.value = sampleSignUpUiState.value.copy(lastName = "test")
      phoneTextField {
        performScrollTo()
        assertIsDisplayed()
        performTextInput("123456789")
      }
      sampleSignUpUiState.value = sampleSignUpUiState.value.copy(phoneNumber = "123456789")
      birthDateTextField {
        performScrollTo()
        assertIsDisplayed()
        performTextInput("01.01.2000")
      }
      sampleSignUpUiState.value = sampleSignUpUiState.value.copy(birthDate = "01/01/2000")
      signUpButton {
        performScrollTo()
        assertIsDisplayed()
        performClick()
        verify(exactly = 1) { mockAuthenticationViewModel.validateFields() }

        verify(exactly = 1) { mockAuthenticationViewModel.onSignUpStarted() }
      }

      sampleSignUpUiState.value =
          sampleSignUpUiState.value.copy(isSignUpCompleted = false, isSignUpStarted = true)

      signUpLoadingBox { assertIsDisplayed() }
      signUpCircularLoadingIndicator { assertIsDisplayed() }
      sampleSignUpUiState.value =
          sampleSignUpUiState.value.copy(isSignUpCompleted = true, isSignUpSuccessful = true)
      composeTestRule.waitForIdle()
      verify { mockNavActions.navController.navigate(Route.HOME) }
      confirmVerified(mockNavActions)
    }
  }
}
