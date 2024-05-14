package com.github.se.eventradar

import android.util.Log
import com.github.se.eventradar.model.Location
import com.github.se.eventradar.model.User
import com.github.se.eventradar.model.event.Event
import com.github.se.eventradar.model.event.EventCategory
import com.github.se.eventradar.model.event.EventTicket
import com.github.se.eventradar.model.repository.event.IEventRepository
import com.github.se.eventradar.model.repository.event.MockEventRepository
import com.github.se.eventradar.model.repository.user.IUserRepository
import com.github.se.eventradar.model.repository.user.MockUserRepository
import com.github.se.eventradar.viewmodel.EventDetailsViewModel
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
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
  private lateinit var userRepository: IUserRepository

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

  private val ticketCapacity = 1

  private val mockEvent =
      Event(
          eventName = "Event 1",
          eventPhoto = "",
          start = LocalDateTime.now(),
          end = LocalDateTime.now(),
          location = Location(0.0, 0.0, "Test Location"),
          description = "Test Description",
          ticket = EventTicket("Test Ticket", 0.0, ticketCapacity),
          mainOrganiser = "1",
          organiserList = mutableListOf("Test Organiser"),
          attendeeList = mutableListOf("Test Attendee"),
          category = EventCategory.COMMUNITY,
          fireBaseID = "event1")

  private val mockUser =
      User(
          userId = "user1",
          birthDate = "01/01/2000",
          email = "test@example.com",
          firstName = "John",
          lastName = "Doe",
          phoneNumber = "1234567890",
          accountStatus = "active",
          eventsAttendeeList = mutableListOf("event2"),
          eventsHostList = mutableListOf("event3"),
          friendsList = mutableListOf(),
          profilePicUrl = "http://example.com/Profile_Pictures/pic.jpg",
          qrCodeUrl = "http://example.com/QR_Codes/qr.jpg",
          username = "johndoe")

  private val factory =
      object : EventDetailsViewModel.Factory {
        override fun create(eventId: String): EventDetailsViewModel {
          return EventDetailsViewModel(eventRepository, userRepository, eventId)
        }
      }

  @Before
  fun setUp() {
    eventRepository = MockEventRepository()

    userRepository = MockUserRepository()
    (userRepository as MockUserRepository).updateCurrentUserId(mockUser.userId)

    viewModel = factory.create(eventId = mockEvent.fireBaseID)
  }

  @Test
  fun testGetEventWithNoDataInDataBase() = runTest {
    mockkStatic(Log::class)
    every { Log.d(any(), any()) } returns 0

    viewModel.getEventData()
    assert(viewModel.uiState.value.eventName.isEmpty())
    assert(viewModel.uiState.value.description.isEmpty())
    assert(viewModel.uiState.value.eventPhoto.isEmpty())
    assert(viewModel.uiState.value.mainOrganiser.isEmpty())

    unmockkAll()
  }

  @Test
  fun testGetEventSuccess() = runTest {
    eventRepository.addEvent(mockEvent)
    viewModel.getEventData()

    assert(viewModel.uiState.value.eventName == mockEvent.eventName)
    assert(viewModel.uiState.value.description == mockEvent.description)
    assert(viewModel.uiState.value.mainOrganiser == mockEvent.mainOrganiser)
    assert(viewModel.uiState.value.start == mockEvent.start)
    assert(viewModel.uiState.value.end == mockEvent.end)
    assert(viewModel.uiState.value.location == mockEvent.location)
    assert(viewModel.uiState.value.ticket == mockEvent.ticket)
    assert(viewModel.uiState.value.category == mockEvent.category)
  }

  @Test
  fun testGetEventWithUpdateAndFetchAgain() = runTest {
    eventRepository.addEvent(mockEvent)
    viewModel.getEventData()
    assert(viewModel.uiState.value.eventName == mockEvent.eventName)

    mockEvent.eventName = "New Name"
    assert(viewModel.uiState.value.eventName != mockEvent.eventName)

    viewModel.getEventData()
    assert(viewModel.uiState.value.eventName == mockEvent.eventName)
  }

  @Test
  fun testTicketIsFree() = runTest {
    eventRepository.addEvent(mockEvent)
    mockEvent.ticket = EventTicket("Paid", 0.0, 10)
    viewModel.getEventData()
    assert(viewModel.isTicketFree())
  }

  @Test
  fun testTicketIsNotFree() = runTest {
    eventRepository.addEvent(mockEvent)
    val randomPrice: Double = kotlin.random.Random.nextDouble(0.001, Double.MAX_VALUE)
    mockEvent.ticket = EventTicket("Paid", randomPrice, 10)
    viewModel.getEventData()
    assert(!viewModel.isTicketFree())
  }

  @Test
  fun testJoinAnEvent() = runTest {
    mockkStatic(Log::class)
    every { Log.d(any(), any()) } returns 0

    eventRepository.addEvent(mockEvent)
    userRepository.addUser(mockUser)
    println("\n\n\n")
    println("user set: ${mockUser.eventsAttendeeList}")
    println("event set: ${mockEvent.attendeeList}")

    viewModel.getEventData()

    println("eventId: ${viewModel.eventId}")
    println("userId: ${userRepository.getCurrentUserId()}")

    viewModel.buyTicketForEvent()

    println("user set: ${mockUser.eventsAttendeeList}")
    println("event set: ${mockEvent.attendeeList}")

    assert(mockUser.eventsAttendeeList.contains(mockEvent.fireBaseID))
    assert(mockEvent.attendeeList.contains(mockUser.userId))
    assert(mockEvent.ticket.capacity == ticketCapacity - 1)

    unmockkAll()
  }
}
