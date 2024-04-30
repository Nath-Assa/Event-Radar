package com.github.se.eventradar

import com.github.se.eventradar.model.Location
import com.github.se.eventradar.model.event.Event
import com.github.se.eventradar.model.event.EventCategory
import com.github.se.eventradar.model.event.EventDetailsViewModel
import com.github.se.eventradar.model.event.EventTicket
import com.github.se.eventradar.model.repository.event.IEventRepository
import com.github.se.eventradar.model.repository.event.MockEventRepository
import java.time.LocalDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@ExperimentalCoroutinesApi
class EventDetailsViewmodelUnitTest {
  private lateinit var viewModel: EventDetailsViewModel
  private lateinit var eventRepository: IEventRepository

  class MainDispatcherRule(
      private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
  ) : TestWatcher() {
    override fun starting(description: Description) {
      Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
      Dispatchers.resetMain()
    }
  }

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  val test: MutableSet<String> = mutableSetOf("Test Organiser", "Organiser2")

  private val mockEvent =
      Event(
          eventName = "Event 1",
          eventPhoto = "",
          start = LocalDateTime.now(),
          end = LocalDateTime.now(),
          location = Location(0.0, 0.0, "Test Location"),
          description = "Test Description",
          ticket = EventTicket("Test Ticket", 0.0, 1),
          mainOrganiser = "1",
          organiserSet = mutableSetOf("Test Organiser"),
          attendeeSet = mutableSetOf("Test Attendee"),
          category = EventCategory.COMMUNITY,
          fireBaseID = "1")

  @Before
  fun setUp() {
    eventRepository = MockEventRepository()
    viewModel = EventDetailsViewModel(eventRepository)
    viewModel.saveEventId(mockEvent.fireBaseID)
  }

  @Test
  fun testGetEvent() = runTest {
    eventRepository.addEvent(mockEvent)
    viewModel.getEventData()

    assert(viewModel.uiState.value.eventName == mockEvent.eventName)
  }
}
