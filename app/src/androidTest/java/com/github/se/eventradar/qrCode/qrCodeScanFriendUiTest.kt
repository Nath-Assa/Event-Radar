package com.github.se.eventradar.qrCode

import android.os.Build
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiSelector
import com.github.se.eventradar.screens.QrCodeScanFriendUiScreen
import com.github.se.eventradar.ui.navigation.NavigationActions
import com.github.se.eventradar.ui.qrCode.QrCodeScreen
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.compose.node.element.ComposeScreen.Companion.onComposeScreen
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QrCodeScanFriendUiTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK lateinit var mockNavActions: NavigationActions

  private lateinit var mDevice: UiDevice

  @Before
  fun testSetup() {
    //        MockKAnnotations.init(this)
    composeTestRule.setContent { QrCodeScreen(mockNavActions) }
    mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
  }

  @Test
  fun displaysAllComponentsCorrectly_CameraPermittedAlways(): Unit = run {
    onComposeScreen<QrCodeScanFriendUiScreen>(composeTestRule) {
      scanQrTab.performClick()
      allowPermissionsWhileUsingApp()

      try {
        logo.assertIsDisplayed()
      } catch (e: AssertionError) {
        println("Failed to find Logo: ${e.message}")
        throw e // Re-throw to fail the test
      }
      try {
        tabs.assertIsDisplayed()
      } catch (e: AssertionError) {
        println("Failed to find Tabs: ${e.message}")
        throw e // Re-throw to fail the test
      }
      try {
        myQrTab.assertIsDisplayed()
      } catch (e: AssertionError) {
        println("Failed to find My QR Tab: ${e.message}")
        throw e // Re-throw to fail the test
      }
      try {
        scanQrTab.assertIsDisplayed()
      } catch (e: AssertionError) {
        println("Failed to find Scan QR Tab: ${e.message}")
        throw e // Re-throw to fail the test
      }
      try {
        qrScanner.assertIsDisplayed()
      } catch (e: AssertionError) {
        println("Failed to find QR Scanner: ${e.message}")
        throw e // Re-throw to fail the test
      }
      try {
        bottomNavMenu.assertIsDisplayed()
      } catch (e: AssertionError) {
        println("Failed to find Bottom Navigation Menu: ${e.message}")
        throw e // Re-throw to fail the test
      }
    }
  }

  @Test
  fun displaysAllComponentsCorrectly_CameraPermittedOnce(): Unit = run {
    onComposeScreen<QrCodeScanFriendUiScreen>(composeTestRule) {
      scanQrTab.performClick()
      allowPermissionsOnce()

      try {
        logo.assertIsDisplayed()
      } catch (e: AssertionError) {
        println("Failed to find Logo: ${e.message}")
        throw e // Re-throw to fail the test
      }
      try {
        tabs.assertIsDisplayed()
      } catch (e: AssertionError) {
        println("Failed to find Tabs: ${e.message}")
        throw e // Re-throw to fail the test
      }
      try {
        myQrTab.assertIsDisplayed()
      } catch (e: AssertionError) {
        println("Failed to find My QR Tab: ${e.message}")
        throw e // Re-throw to fail the test
      }
      try {
        scanQrTab.assertIsDisplayed()
      } catch (e: AssertionError) {
        println("Failed to find Scan QR Tab: ${e.message}")
        throw e // Re-throw to fail the test
      }
      try {
        qrScanner.assertIsDisplayed()
      } catch (e: AssertionError) {
        println("Failed to find QR Scanner: ${e.message}")
        throw e // Re-throw to fail the test
      }
      try {
        bottomNavMenu.assertIsDisplayed()
      } catch (e: AssertionError) {
        println("Failed to find Bottom Navigation Menu: ${e.message}")
        throw e // Re-throw to fail the test
      }
    }
  }

  @Test
  fun displaysAllComponentsCorrectly_CameraDenied(): Unit = run {
    onComposeScreen<QrCodeScanFriendUiScreen>(composeTestRule) {
      scanQrTab.performClick()
      denyPermissions()

      try {
        logo.assertIsDisplayed()
      } catch (e: AssertionError) {
        println("Failed to find Logo: ${e.message}")
        throw e // Re-throw to fail the test
      }
      try {
        tabs.assertIsDisplayed()
      } catch (e: AssertionError) {
        println("Failed to find Tabs: ${e.message}")
        throw e // Re-throw to fail the test
      }
      try {
        myQrTab.assertIsDisplayed()
      } catch (e: AssertionError) {
        println("Failed to find My QR Tab: ${e.message}")
        throw e // Re-throw to fail the test
      }
      try {
        scanQrTab.assertIsDisplayed()
      } catch (e: AssertionError) {
        println("Failed to find Scan QR Tab: ${e.message}")
        throw e // Re-throw to fail the test
      }
      try {
        bottomNavMenu.assertIsDisplayed()
      } catch (e: AssertionError) {
        println("Failed to find Bottom Navigation Menu: ${e.message}")
        throw e // Re-throw to fail the test
      }
    }
  }



  private fun allowPermissionsWhileUsingApp() {
    if (Build.VERSION.SDK_INT >= 23) {
      val allowPermissions: UiObject = mDevice.findObject(UiSelector().text("While using the app"))
      if (allowPermissions.exists()) {
        try {
          allowPermissions.click()
        } catch (e: UiObjectNotFoundException) {
          println("There is no permissions dialog to interact with ")
          throw e
        }
      }
    }
  }

  private fun allowPermissionsOnce() {
    if (Build.VERSION.SDK_INT >= 23) {
      val allowPermissions: UiObject = mDevice.findObject(UiSelector().text("Only this time"))
      if (allowPermissions.exists()) {
        try {
          allowPermissions.click()
        } catch (e: UiObjectNotFoundException) {
          println("There is no permissions dialog to interact with ")
          throw e
        }
      }
    }
  }

  private fun denyPermissions() {
    if (Build.VERSION.SDK_INT >= 23) {
      val allowPermissions: UiObject = mDevice.findObject(UiSelector().text("Don't Allow"))
      if (allowPermissions.exists()) {
        try {
          allowPermissions.click()
        } catch (e: UiObjectNotFoundException) {
          println("There is no permissions dialog to interact with ")
          throw e
        }
      }
    }
  }
}

//                logo { assertIsDisplayed() }
//                tabs { assertIsDisplayed() }
//                myQrTab { assertIsDisplayed() }
//                scanQrTab { assertIsDisplayed() }
//                qrScanner { assertIsDisplayed() }
//                bottomNavMenu { assertIsDisplayed() }
//            }
//        }
// }

// @Test
// fun qrCodeScanFriendUi_displaysAllComponentsCorrectly() {
//    composeTestRule.setContent {
//        QrCodeScreen(mockNavActions) // Assuming a fake implementation for NavigationActions
//    }
//
//    QrCodeScanFriendUiScreen(composeTestRule).apply {
//        logo.assertIsDisplayed()
//        tabs.assertIsDisplayed()
//        myQrTab.assertIsDisplayed()
//        scanQrTab.assertIsDisplayed()
//        qrScanner.assertIsDisplayed()
//        bottomNavMenu.assertIsDisplayed()
//    }
// }
// }
