package com.github.se.eventradar.qrCode

import android.Manifest
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import com.github.se.eventradar.model.Location
import com.github.se.eventradar.model.event.EventCategory
import com.github.se.eventradar.model.event.EventTicket
import com.github.se.eventradar.model.repository.event.IEventRepository
import com.github.se.eventradar.model.repository.event.MockEventRepository
import com.github.se.eventradar.model.repository.user.IUserRepository
import com.github.se.eventradar.model.repository.user.MockUserRepository
import com.github.se.eventradar.screens.QrCodeScanTicketUiScreen
import com.github.se.eventradar.ui.navigation.NavigationActions
import com.github.se.eventradar.ui.qrCode.QrCodeTicketUi
import com.github.se.eventradar.viewmodel.EventUiState
import com.github.se.eventradar.viewmodel.qrCode.QrCodeAnalyser
import com.github.se.eventradar.viewmodel.qrCode.ScanTicketQrViewModel
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.compose.node.element.ComposeScreen.Companion.onComposeScreen
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QrCodeScanTicketUiTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule
  val mRuntimePermissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(Manifest.permission.CAMERA)

  @get:Rule val mockkRule = MockKRule(this)

  @RelaxedMockK lateinit var mockNavActions: NavigationActions

  @RelaxedMockK lateinit var mockViewModel: ScanTicketQrViewModel

  private lateinit var mDevice: UiDevice
  private lateinit var userRepository: IUserRepository
  private lateinit var eventRepository: IEventRepository
  private lateinit var qrCodeAnalyser: QrCodeAnalyser
  private val myUID = "user1"

  private val myEventUiState =
      MutableStateFlow(
          ScanTicketQrViewModel.QrCodeScanTicketState(
              decodedResult = "",
              tabState = ScanTicketQrViewModel.Tab.MyEvent,
              action = ScanTicketQrViewModel.Action.ScanTicket,
              eventUiState =
                  EventUiState(
                      eventName = "Test_Event",
                      eventPhoto = "",
                      start = LocalDateTime.now(),
                      end = LocalDateTime.now(),
                      location = Location(0.0, 0.0, "Test Location"),
                      description = "Test Description",
                      ticket = EventTicket("Test Ticket", 0.0, 100, 59),
                      mainOrganiser = "1",
                      category = EventCategory.COMMUNITY)))

  @Before
  fun testSetup() {
    MockKAnnotations.init(this)
    every { mockNavActions.navigateTo(any()) } just Runs
    userRepository = MockUserRepository()
    (userRepository as MockUserRepository).updateCurrentUserId(myUID)
    eventRepository = MockEventRepository()
    qrCodeAnalyser = mockk<QrCodeAnalyser>(relaxed = true)
    mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
  }

  @Test
  fun screenDisplaysNavigationElementsCorrectly() = run {
    every { mockViewModel.uiState } returns myEventUiState
    onComposeScreen<QrCodeScanTicketUiScreen>(composeTestRule) {
      composeTestRule.setContent { QrCodeTicketUi(mockViewModel, mockNavActions) }
      bottomNavMenu.assertIsDisplayed()
      goBackButton.assertIsDisplayed()
      myQrTab.assertIsDisplayed()
      scanQrTab.assertIsDisplayed()
    }
  }

  @Test
  fun screenDisplaysContentElementsCorrectly() = run {
    every { mockViewModel.uiState } returns myEventUiState
    onComposeScreen<QrCodeScanTicketUiScreen>(composeTestRule) {
      step("Title + Image + Description") {
        composeTestRule.setContent { QrCodeTicketUi(mockViewModel, mockNavActions) }
        lazyEventDetails.assertIsDisplayed()
        eventTitle {
          assertIsDisplayed()
          assertTextContains(myEventUiState.value.eventUiState.eventName)
        }
        eventImage { assertIsDisplayed() }
        descriptionTitle { assertIsDisplayed() }
        descriptionContent {
          assertIsDisplayed()
          assertTextContains(myEventUiState.value.eventUiState.description)
        }
      }
      step("Location") {
        locationTitle { assertIsDisplayed() }
        locationContent { assertIsDisplayed() }
      }
      step("Category") {
        categoryTitle { assertIsDisplayed() }
        categoryContent {
          assertIsDisplayed()
          assertTextContains("Community")
        }
        step("Date") {
          dateTitle { assertIsDisplayed() }
          dateContent { assertIsDisplayed() }
        }
        step("Time") {
          timeTitle { assertIsDisplayed() }
          timeContent { assertIsDisplayed() }
        }
        step("Tickets Sold") {
          ticketSoldContent {
            assertIsDisplayed()
            assertTextContains(
                myEventUiState.value.eventUiState.ticket.purchases.toString() + " tickets sold")
          }
        }
      }
    }
  }

  @Test
  fun goBackButtonTriggersBackNavigation() = run {
    //    val viewModel = setupViewModelMyEventTab()
    every { mockViewModel.uiState } returns myEventUiState
    onComposeScreen<QrCodeScanTicketUiScreen>(composeTestRule) {
      composeTestRule.setContent { QrCodeTicketUi(mockViewModel, mockNavActions) }
      goBackButton {
        // arrange: verify the pre-conditions
        assertIsDisplayed()
        assertIsEnabled()
        // act: go back !
        performClick()
      }
    }
    // assert: the nav action has been called
    verify { mockNavActions.goBack() }
    confirmVerified(mockNavActions)
  }

  @Test
  fun displaysAllComponentsCorrectly_CameraPermittedAlways(): Unit = run {
    onComposeScreen<QrCodeScanTicketUiScreen>(composeTestRule) {
      val viewModel = setupViewModelWithState(ScanTicketQrViewModel.Action.ScanTicket)
      composeTestRule.setContent { QrCodeTicketUi(viewModel, mockNavActions) }
      scanQrTab.performClick()
      tabs.assertIsDisplayed()
      myQrTab.assertIsDisplayed()
      scanQrTab.assertIsDisplayed()
      qrScanner.assertIsDisplayed()
      bottomNavMenu.assertIsDisplayed()
    }
  }

  @Test
  fun closeDialogueResetState(): Unit = run {
    val viewModel = setupViewModelWithState(ScanTicketQrViewModel.Action.ApproveEntry)
    onComposeScreen<QrCodeScanTicketUiScreen>(composeTestRule) {
      composeTestRule.setContent { QrCodeTicketUi(viewModel, mockNavActions) }
      scanQrTab.performClick()
      closeButton.performClick()
      assertEquals(ScanTicketQrViewModel.Action.ScanTicket, viewModel.uiState.value.action)
    }
  }

  @Test
  fun displaysAllComponentsCorrectly_Approved(): Unit = run {
    val viewModel = setupViewModelWithState(ScanTicketQrViewModel.Action.ApproveEntry)
    onComposeScreen<QrCodeScanTicketUiScreen>(composeTestRule) {
      composeTestRule.setContent { QrCodeTicketUi(viewModel, mockNavActions) }
      scanQrTab.performClick()
      tabs.assertIsDisplayed()
      myQrTab.assertIsDisplayed()
      scanQrTab.assertIsDisplayed()
      bottomNavMenu.assertIsDisplayed()
      approvedBox.assertIsDisplayed()
      approvedText.assertIsDisplayed()
      closeButton.assertIsDisplayed()
    }
  }

  @Test
  fun displaysAllComponentsCorrectly_Denied(): Unit = run {
    val viewModel = setupViewModelWithState(ScanTicketQrViewModel.Action.DenyEntry)
    onComposeScreen<QrCodeScanTicketUiScreen>(composeTestRule) {
      composeTestRule.setContent { QrCodeTicketUi(viewModel, mockNavActions) }
      scanQrTab.performClick()
      tabs.assertIsDisplayed()
      myQrTab.assertIsDisplayed()
      bottomNavMenu.assertIsDisplayed()
      deniedBox.assertIsDisplayed()
      deniedText.assertIsDisplayed()
      closeButton.assertIsDisplayed()
    }
  }

  @Test
  fun displaysAllComponentsCorrectly_Error1(): Unit = run {
    val viewModel = setupViewModelWithState(ScanTicketQrViewModel.Action.FirebaseFetchError)
    onComposeScreen<QrCodeScanTicketUiScreen>(composeTestRule) {
      composeTestRule.setContent { QrCodeTicketUi(viewModel, mockNavActions) }
      scanQrTab.performClick()
      tabs.assertIsDisplayed()
      myQrTab.assertIsDisplayed()
      scanQrTab.assertIsDisplayed()
      bottomNavMenu.assertIsDisplayed()
      errorBox.assertIsDisplayed()
      errorText.assertIsDisplayed()
      closeButton.assertIsDisplayed()
    }
  }

  @Test
  fun displaysAllComponentsCorrectly_Error2(): Unit = run {
    val viewModel = setupViewModelWithState(ScanTicketQrViewModel.Action.FirebaseUpdateError)
    onComposeScreen<QrCodeScanTicketUiScreen>(composeTestRule) {
      composeTestRule.setContent { QrCodeTicketUi(viewModel, mockNavActions) }
      scanQrTab.performClick()
      tabs.assertIsDisplayed()
      myQrTab.assertIsDisplayed()
      scanQrTab.assertIsDisplayed()
      bottomNavMenu.assertIsDisplayed()
      errorBox.assertIsDisplayed()
      errorText.assertIsDisplayed()
      closeButton.assertIsDisplayed()
    }
  }

  @Test
  fun displaysAllComponentsCorrectly_Error3(): Unit = run {
    val viewModel = setupViewModelWithState(ScanTicketQrViewModel.Action.AnalyserError)
    onComposeScreen<QrCodeScanTicketUiScreen>(composeTestRule) {
      composeTestRule.setContent { QrCodeTicketUi(viewModel, mockNavActions) }
      scanQrTab.performClick()
      tabs.assertIsDisplayed()
      myQrTab.assertIsDisplayed()
      scanQrTab.assertIsDisplayed()
      bottomNavMenu.assertIsDisplayed()
      errorBox.assertIsDisplayed()
      errorText.assertIsDisplayed()
      closeButton.assertIsDisplayed()
    }
  }

  @Test
  fun resetsTabState(): Unit = run {
    val viewModel = setupViewModelWithState(ScanTicketQrViewModel.Action.ScanTicket)
    onComposeScreen<QrCodeScanTicketUiScreen>(composeTestRule) {
      composeTestRule.setContent { QrCodeTicketUi(viewModel, mockNavActions) }
      scanQrTab.performClick()
      myQrTab.performClick()
      assertEquals(ScanTicketQrViewModel.Tab.MyEvent, viewModel.uiState.value.tabState)
    }
  }

  private fun setupViewModelWithState(action: ScanTicketQrViewModel.Action): ScanTicketQrViewModel {
    // Create the ViewModel with a specific state for testing
    return ScanTicketQrViewModel(userRepository, eventRepository, qrCodeAnalyser, "1").apply {
      changeAction(action) // Assuming there is a method to change actions
    }
  }
}
