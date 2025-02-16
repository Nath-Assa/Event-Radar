package com.github.se.eventradar.screens

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

/**  */
class SignupScreen(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<SignupScreen>(
        semanticsProvider = semanticsProvider, viewBuilderAction = { hasTestTag("signUpScreen") }) {

  // Structural elements of the UI of the Signup screen
  val eventRadarLogo: KNode = onNode { hasTestTag("signUpLogo") }
  private val username: KNode = onNode { hasTestTag("signUpUsernameField") }
  val usernameTextField: KNode = username.child { hasTestTag("signUpUsernameTextField") }
  private val name: KNode = onNode { hasTestTag("signUpNameField") }
  val nameTextField: KNode = name.child { hasTestTag("signUpNameTextField") }
  private val surname: KNode = onNode { hasTestTag("signUpSurnameField") }
  val surnameTextField: KNode = surname.child { hasTestTag("signUpSurnameTextField") }
  private val phone: KNode = onNode { hasTestTag("signUpPhoneField") }
  val phoneTextField: KNode = phone.child { hasTestTag("signUpPhoneTextField") }
  private val birthdate: KNode = onNode { hasTestTag("signUpBirthDateField") }
  val signUpLoadingBox: KNode = onNode { hasTestTag("signUpLoadingBox") }
  val signUpCircularLoadingIndicator: KNode =
      signUpLoadingBox.child { hasTestTag("signUpCircularLoadingIndicator") }
  val birthDateTextField: KNode = birthdate.child { hasTestTag("signUpBirthDateTextField") }
  val signUpButton: KNode = onNode { hasTestTag("signUpLoginButton") }
  val profileSelectedPicture: KNode = onNode { hasTestTag("signUpProfilePicture") }
  val profilePicture: KNode = onNode { hasTestTag("signUpProfilePicturePlaceholder") }
  val errorDialog: KNode = onNode { hasTestTag("signUpErrorDialog") }
  val errorDialogText: KNode = onNode { hasTestTag("loginErrorDisplayText") }
  val errorDialogButton: KNode = onNode { hasTestTag("errorDialogConfirmButton") }
  val errorDialogIcon: KNode = onNode { hasTestTag("errorDialogIcon") }
  val errorDialogTitle: KNode = onNode { hasTestTag("loginErrorTitle") }
}
