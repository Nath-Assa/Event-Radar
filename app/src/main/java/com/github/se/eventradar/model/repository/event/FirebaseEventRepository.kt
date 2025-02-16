package com.github.se.eventradar.model.repository.event

import com.github.se.eventradar.model.Resource
import com.github.se.eventradar.model.event.Event
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseEventRepository(val db: FirebaseFirestore = Firebase.firestore) : IEventRepository {
  private val eventRef: CollectionReference = db.collection("events")
  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
  private val currentDateTimeString: String
    get() = LocalDateTime.now().format(formatter)

  override suspend fun getEvents(): Resource<List<Event>> {
    val resultDocument = eventRef.whereGreaterThan("end", currentDateTimeString).get().await()

    return try {
      val events = resultDocument.documents.map { document -> Event(document.data!!, document.id) }
      Resource.Success(events)
    } catch (e: Exception) {
      Resource.Failure(e)
    }
  }

  override suspend fun getEvent(id: String): Resource<Event?> {
    val resultDocument = eventRef.document(id).get().await()

    return try {
      val event = Event(resultDocument.data!!, resultDocument.id)
      Resource.Success(event)
    } catch (e: Exception) {
      Resource.Failure(e)
    }
  }

  /**
   * Retrieves a new unique identifier for an event from the Firestore database. This generates a
   * newEventId from firestore without creating an actual document, which can be fed into a new
   * event object that is added to the firebase event collection database. This ID is generated
   * without creating an actual document in the database, ensuring that the ID can be used for a
   * future event creation without risking ID collision or premature document instantiation.
   *
   * @return A [Resource<String>] object which encapsulates the operation result. If successful, it
   *   returns [Resource.Success] containing the newly generated event ID as a string. If there is a
   *   failure during the ID generation process, it returns [Resource.Failure] containing the
   *   exception that was thrown.
   */
  override suspend fun getUniqueEventId(): Resource<String> {
    return try {
      val nextEventId = eventRef.document().id
      Resource.Success(nextEventId)
    } catch (e: Exception) {
      Resource.Failure(e)
    }
  }

  override suspend fun addEvent(event: Event): Resource<Unit> {
    val eventMap = event.toMap()
    val eventId = event.fireBaseID
    return try {
      eventRef.document(eventId).set(eventMap).await()
      Resource.Success(Unit)
    } catch (e: Exception) {
      Resource.Failure(e)
    }
  }

  override suspend fun updateEvent(event: Event): Resource<Unit> {
    val eventMap = event.toMap()

    return try {
      eventRef.document(event.fireBaseID).update(eventMap as Map<String, Any>).await()
      Resource.Success(Unit)
    } catch (e: Exception) {
      Resource.Failure(e)
    }
  }

  override suspend fun deleteEvent(event: Event): Resource<Unit> {
    return try {
      eventRef.document(event.fireBaseID).delete().await()
      Resource.Success(Unit)
    } catch (e: Exception) {
      Resource.Failure(e)
    }
  }

  override suspend fun getEventsByIds(ids: List<String>): Resource<List<Event>> {
    return try {
      val events = mutableListOf<Event>()
      for (id in ids) {
        val document = eventRef.document(id).get().await()
        if (document.exists()) {
          events.add(Event(document.data!!, document.id))
        } else {
          return Resource.Failure(Exception("Event with id $id is missing"))
        }
      }
      Resource.Success(events)
    } catch (e: Exception) {
      Resource.Failure(e)
    }
  }

  override fun observeAllEvents(): Flow<Resource<List<Event>>> = callbackFlow {
    val query = eventRef.whereGreaterThan("end", currentDateTimeString)
    val listener =
        query.addSnapshotListener { snapshot, error ->
          if (error != null) {
            trySend(
                Resource.Failure(Exception("Error listening to event updates: ${error.message}")))
            return@addSnapshotListener
          }
          val events = snapshot?.documents?.mapNotNull { Event(it.data!!, it.id) }
          trySend(Resource.Success(events ?: listOf()))
        }
    awaitClose { listener.remove() }
  }

  override fun observeUpcomingEvents(userId: String): Flow<Resource<List<Event>>> = callbackFlow {
    val query = eventRef.whereArrayContains("attendees_list", userId)

    val listener =
        query.addSnapshotListener { snapshot, error ->
          if (error != null) {
            trySend(
                Resource.Failure(
                    Exception("Error listening to upcoming event updates: ${error.message}")))
            return@addSnapshotListener
          }

          val upcomingEvents =
              snapshot?.documents?.mapNotNull { it.data?.let { data -> Event(data, it.id) } }
                  ?: listOf()

          trySend(Resource.Success(upcomingEvents))
        }

    awaitClose { listener.remove() }
  }

  override suspend fun addAttendee(eventId: String, attendeeUserId: String): Resource<Unit> {
    return try {
      eventRef.document(eventId).update("attendees_list", FieldValue.arrayUnion(attendeeUserId))
      Resource.Success(Unit)
    } catch (e: Exception) {
      Resource.Failure(e)
    }
  }

  override suspend fun removeAttendee(eventId: String, attendeeUserId: String): Resource<Unit> {
    return try {
      eventRef.document(eventId).update("attendees_list", FieldValue.arrayRemove(attendeeUserId))
      Resource.Success(Unit)
    } catch (e: Exception) {
      Resource.Failure(e)
    }
  }

  override suspend fun incrementPurchases(eventId: String): Resource<Unit> {
    try {
      db.runTransaction { transaction ->
        val ref = eventRef.document(eventId)
        val resultDocument = transaction.get(ref)
        val purchases = resultDocument.getLong("ticket_purchases")
        val capacity = resultDocument.getLong("ticket_capacity")

        if (purchases != null && capacity != null) {
          if (purchases < capacity) {
            transaction.update(ref, "ticket_purchases", purchases + 1)
          } else {
            throw FirebaseFirestoreException(
                "No more available tickets",
                FirebaseFirestoreException.Code.ABORTED,
            )
          }
        } else {
          throw FirebaseFirestoreException(
              "Invalid data",
              FirebaseFirestoreException.Code.ABORTED,
          )
        }
      }
      return Resource.Success(Unit)
    } catch (e: Exception) {
      return Resource.Failure(e)
    }
  }

  override suspend fun decrementPurchases(eventId: String): Resource<Unit> {
    try {
      db.runTransaction { transaction ->
        val ref = eventRef.document(eventId)
        val resultDocument = transaction.get(ref)
        val purchases = resultDocument.getLong("ticket_purchases")
        val capacity = resultDocument.getLong("ticket_capacity")

        if (purchases != null && capacity != null) {
          if (purchases > 0) {
            transaction.update(ref, "ticket_purchases", purchases - 1)
          } else {
            throw FirebaseFirestoreException(
                "Ticket purchases is already 0",
                FirebaseFirestoreException.Code.ABORTED,
            )
          }
        } else {
          throw FirebaseFirestoreException(
              "Invalid data",
              FirebaseFirestoreException.Code.ABORTED,
          )
        }
      }
      return Resource.Success(Unit)
    } catch (e: Exception) {
      return Resource.Failure(e)
    }
  }
}
