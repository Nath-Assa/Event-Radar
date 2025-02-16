package com.github.se.eventradar.event

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.eventradar.model.Location
import com.github.se.eventradar.model.event.EventCategory
import com.github.se.eventradar.model.event.EventTicket
import com.github.se.eventradar.screens.EventDetailsScreen
import com.github.se.eventradar.ui.event.EventDetails
import com.github.se.eventradar.ui.navigation.NavigationActions
import com.github.se.eventradar.ui.navigation.Route
import com.github.se.eventradar.viewmodel.EventDetailsViewModel
import com.github.se.eventradar.viewmodel.EventUiState
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.verify
import java.time.LocalDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventDetailsUITest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {

  @get:Rule val composeTestRule = createComposeRule()

  // This rule automatic initializes lateinit properties with @MockK, @RelaxedMockK, etc.
  @get:Rule val mockkRule = MockKRule(this)

  // Relaxed mocks methods have a default implementation returning values
  @RelaxedMockK lateinit var mockNavActions: NavigationActions
  @RelaxedMockK lateinit var mockViewModel: EventDetailsViewModel

  private val sampleEventDetailsUiState =
      MutableStateFlow(
          EventUiState(
              eventName = "Debugging",
              eventPhoto = "path",
              start = LocalDateTime.parse("2022-01-01T10:00:00"),
              end = LocalDateTime.parse("2022-01-01T12:00:00"),
              location = Location(0.0, 0.0, "base address"),
              description = "Let's debug some code together because we all enjoy kotlin !",
              ticket = EventTicket("Luck", 0.0, 7, 0),
              mainOrganiser = "some.name@host.com",
              category = EventCategory.COMMUNITY,
          ))

  private val eventId = "tdjWMT9Eon2ROTVakQb"

  @Before
  fun testSetup() {

    every { mockViewModel.isUserAttending } returns MutableStateFlow(false)
    every { mockViewModel.uiState } returns sampleEventDetailsUiState
    every { mockViewModel.eventId } returns eventId
    every { mockViewModel.showCancelRegistrationDialog } returns mutableStateOf(false)

    composeTestRule.setContent { EventDetails(mockViewModel, navigationActions = mockNavActions) }
  }

  @Test
  fun screenDisplaysNavigationElementsCorrectly() = run {
    ComposeScreen.onComposeScreen<EventDetailsScreen>(composeTestRule) {
      registrationButton { assertIsDisplayed() }
      goBackButton { assertIsDisplayed() }
      bottomNav { assertIsDisplayed() }
    }
  }

  @Test
  fun screenDisplaysContentElementsCorrectly() = run {
    ComposeScreen.onComposeScreen<EventDetailsScreen>(composeTestRule) {
      eventTitle { assertIsDisplayed() }
      eventImage { assertIsDisplayed() }
      descriptionTitle { assertIsDisplayed() }
      descriptionContent {
        assertIsDisplayed()
        assertTextContains("Let's debug some code together because we all enjoy kotlin !")
      }
      locationTitle { assertIsDisplayed() }
      locationContent { assertIsDisplayed() }
      categoryTitle { assertIsDisplayed() }
      categoryContent {
        assertIsDisplayed()
        assertTextContains("Community")
      }
      dateTitle { assertIsDisplayed() }
      dateContent { assertIsDisplayed() }
      timeTitle { assertIsDisplayed() }
      timeContent { assertIsDisplayed() }
    }
  }

  @Test
  fun goBackButtonTriggersBackNavigation() = run {
    ComposeScreen.onComposeScreen<EventDetailsScreen>(composeTestRule) {
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
  fun ticketButtonTriggersNavigation() = run {
    ComposeScreen.onComposeScreen<EventDetailsScreen>(composeTestRule) {
      registrationButton {
        // arrange: verify the pre-conditions
        assertIsDisplayed()
        assertIsEnabled()

        performClick()
      }
    }
    // assert: the nav action has been called
    verify { mockNavActions.navController.navigate("${Route.EVENT_DETAILS_TICKETS}/$eventId") }
    confirmVerified(mockNavActions)
  }
}
