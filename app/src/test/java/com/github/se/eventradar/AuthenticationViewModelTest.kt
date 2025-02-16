package com.github.se.eventradar

import android.net.Uri
import android.util.Log
import com.github.se.eventradar.model.Resource
import com.github.se.eventradar.model.User
import com.github.se.eventradar.model.repository.user.IUserRepository
import com.github.se.eventradar.model.repository.user.MockUserRepository
import com.github.se.eventradar.viewmodel.CountryCode
import com.github.se.eventradar.viewmodel.LoginUiState
import com.github.se.eventradar.viewmodel.LoginViewModel
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AuthenticationViewModelTest {

  @RelaxedMockK lateinit var viewModel: LoginViewModel
  @RelaxedMockK lateinit var user: FirebaseUser
  @RelaxedMockK lateinit var uri: Uri

  private lateinit var mockUiState: MutableStateFlow<LoginUiState>
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

  private val mockUser =
      User(
          userId = "1",
          birthDate = "01.01.2000",
          email = "test@test.com",
          firstName = "John",
          lastName = "Doe",
          phoneNumber = "1234567890",
          accountStatus = "active",
          eventsAttendeeList = mutableListOf(),
          eventsHostList = mutableListOf(),
          friendsList = mutableListOf(),
          profilePicUrl = "",
          qrCodeUrl = "",
          bio = "",
          username = "john_doe")

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  @Before
  fun setUp() {
    userRepository = MockUserRepository()
    viewModel = LoginViewModel(userRepository)
    mockUiState = MutableStateFlow(LoginUiState())
    user = mockk<FirebaseUser>()
    uri =
        mockk<Uri> {
          every { path } returns
              "content://media/picker/0/com.android.providers.media.photopicker/media/1000009885"
        }

    every { user.uid } returns "1"
    every { user.email } returns "test@test.com"
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun testOnFirstNameChange() = runTest {
    viewModel.onFirstNameChanged(mockUser.firstName, mockUiState)
    assert(mockUiState.value.firstName == mockUser.firstName)
  }

  @Test
  fun testOnLastNameChange() = runTest {
    viewModel.onLastNameChanged(mockUser.lastName, mockUiState)
    assert(mockUiState.value.lastName == mockUser.lastName)
  }

  @Test
  fun testOnUsernameChange() = runTest {
    viewModel.onUsernameChanged(mockUser.username, mockUiState)
    assert(mockUiState.value.username == mockUser.username)
  }

  @Test
  fun testOnBirthDateChange() = runTest {
    viewModel.onBirthDateChanged(mockUser.birthDate, mockUiState)
    assert(mockUiState.value.birthDate == mockUser.birthDate)
  }

  @Test
  fun testOnCountryCodeChange() = runTest {
    viewModel.onCountryCodeChanged(CountryCode.AU, mockUiState)
    assert(mockUiState.value.selectedCountryCode == CountryCode.AU)
  }

  @Test
  fun testOnPhoneNumberChange() = runTest {
    viewModel.onPhoneNumberChanged(mockUser.phoneNumber, mockUiState)
    assert(mockUiState.value.phoneNumber == mockUser.phoneNumber)
  }

  @Test
  fun testOnSelectedImageUriChange() = runTest {
    viewModel.onSelectedImageUriChanged(Uri.EMPTY, mockUiState)
    assert(mockUiState.value.selectedImageUri == Uri.EMPTY)
  }

  @Test
  fun testValidateFieldsForCorrectFields() = runTest {
    val swissPhoneNumber = "123456789"

    viewModel.onFirstNameChanged(mockUser.firstName, mockUiState)
    viewModel.onLastNameChanged(mockUser.lastName, mockUiState)
    viewModel.onUsernameChanged(mockUser.username, mockUiState)
    viewModel.onBirthDateChanged(mockUser.birthDate, mockUiState)
    viewModel.onCountryCodeChanged(CountryCode.CH, mockUiState)
    viewModel.onPhoneNumberChanged(swissPhoneNumber, mockUiState)
    viewModel.onSelectedImageUriChanged(Uri.EMPTY, mockUiState)

    assert(viewModel.validateFields(mockUiState))
  }

  @Test
  fun testValidateFieldTooOldBirthdate() = runTest {

    // set valid initial state
    val swissPhoneNumber = "123456789"
    viewModel.onFirstNameChanged(mockUser.firstName, mockUiState)
    viewModel.onLastNameChanged(mockUser.lastName, mockUiState)
    viewModel.onUsernameChanged(mockUser.username, mockUiState)
    viewModel.onBirthDateChanged(mockUser.birthDate, mockUiState)
    viewModel.onCountryCodeChanged(CountryCode.CH, mockUiState)
    viewModel.onPhoneNumberChanged(swissPhoneNumber, mockUiState)
    viewModel.onSelectedImageUriChanged(Uri.EMPTY, mockUiState)
    // assert state is valid
    assert(viewModel.validateFields(mockUiState))

    // change the birthdate with invalid
    viewModel.onBirthDateChanged("02.09.1000", mockUiState)

    assert(!viewModel.validateFields(mockUiState))
  }

  @Test
  fun testValidateFieldForEmptyFields() = runTest { assert(!viewModel.validateFields(mockUiState)) }

  @Test
  fun testValidateForPartiallyFilledFields() = runTest {
    viewModel.onFirstNameChanged(mockUser.firstName, mockUiState)
    viewModel.onLastNameChanged(mockUser.lastName, mockUiState)
    viewModel.onUsernameChanged(mockUser.username, mockUiState)
    viewModel.onBirthDateChanged(mockUser.birthDate, mockUiState)
    viewModel.onCountryCodeChanged(CountryCode.AU, mockUiState)

    assert(!viewModel.validateFields(mockUiState))
  }

  @Test
  fun testAddUser() = runTest {
    viewModel.onFirstNameChanged(mockUser.firstName, mockUiState)
    viewModel.onLastNameChanged(mockUser.lastName, mockUiState)
    viewModel.onUsernameChanged(mockUser.username, mockUiState)
    viewModel.onBirthDateChanged(mockUser.birthDate, mockUiState)
    viewModel.onCountryCodeChanged(CountryCode.AU, mockUiState)
    viewModel.onPhoneNumberChanged(mockUser.phoneNumber, mockUiState)
    viewModel.onSelectedImageUriChanged(uri, mockUiState)
    // Mocks Sign In From Google, which sets the userId
    (userRepository as MockUserRepository).updateCurrentUserId(mockUser.userId)
    viewModel.onSignUpStarted(mockUiState)
    // Adds the User Information for Sign Up after Sign In is Successful
    runBlocking { viewModel.addUser(mockUiState) }
    assert(mockUiState.value.isSignUpStarted)
    assert(mockUiState.value.isSignUpCompleted)
    assert(mockUiState.value.isSignUpSuccessful)
    assert(userRepository.getUser("1") is Resource.Success)
  }

  @Test
  fun testAddUserFalseCase() = runTest {
    mockkStatic(Log::class)
    every { Log.d(any(), any()) } returns 0
    (userRepository as MockUserRepository).updateCurrentUserId(null)
    viewModel.onSignUpStarted(mockUiState)
    runBlocking { viewModel.addUser(mockUiState) }
    assert(mockUiState.value.isSignUpStarted)
    assert(!mockUiState.value.isSignUpCompleted)
    assert(!mockUiState.value.isSignUpSuccessful)

    verify { Log.d("LoginScreenViewModel", "User not logged in") }

    unmockkAll()
  }

  @Test
  fun testIsUserLoggedIn() = runTest {
    userRepository.addUser(mockUser)
    val result = viewModel.doesUserExist("1")
    assert(result)
  }

  @Test
  fun testIsUserLoggedInFalseCase() = runTest {
    mockkStatic(Log::class)
    every { Log.d(any(), any()) } returns 0

    val result = viewModel.doesUserExist("2")
    assert(!result)
  }
}
