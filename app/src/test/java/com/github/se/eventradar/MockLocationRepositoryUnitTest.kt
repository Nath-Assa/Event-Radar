package com.github.se.eventradar

import com.github.se.eventradar.model.Location
import com.github.se.eventradar.model.Resource
import com.github.se.eventradar.model.repository.location.ILocationRepository
import com.github.se.eventradar.model.repository.location.MockLocationRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class MockLocationRepositoryUnitTest {
  private lateinit var locationRepository: ILocationRepository
  private val mockLocation = Location(100.0, 100.0, "EPFL")

  @Before
  fun setUp() {
    locationRepository = MockLocationRepository()
  }

  @Test
  fun testGetEventsEmptyAtConstruction() = runTest {
    val location = locationRepository.fetchLocation("EPFL")

    assert(location is Resource.Success)

    assert((location as Resource.Success).data.address == mockLocation.address)
    assert(location.data.latitude == mockLocation.latitude)
    assert(location.data.longitude == mockLocation.longitude)
  }
}
