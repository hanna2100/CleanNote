package com.hanna2100.cleannote.framework.datasource.network

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.hanna2100.cleannote.business.domain.model.Note
import com.hanna2100.cleannote.di.TestAppComponent
import com.hanna2100.cleannote.framework.BaseTest
import com.hanna2100.cleannote.framework.datasource.data.NoteDataFactory
import com.hanna2100.cleannote.framework.datasource.network.abstraction.NoteFirestoreService
import com.hanna2100.cleannote.framework.datasource.network.implementation.NoteFirestoreServiceImpl
import com.hanna2100.cleannote.framework.datasource.network.implementation.NoteFirestoreServiceImpl.Companion.NOTES_COLLECTION
import com.hanna2100.cleannote.framework.datasource.network.implementation.NoteFirestoreServiceImpl.Companion.USER_ID
import com.hanna2100.cleannote.framework.datasource.network.mappers.NetworkMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.random.Random
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
@FlowPreview
@RunWith(AndroidJUnit4ClassRunner::class)
class NoteFirestoreServiceTests: BaseTest() {

    companion object {
        const val EMAIL = "hanna0497test@naver.com"
        const val PASSWORD = "password"
    }

    private lateinit var noteFirestoreService: NoteFirestoreService


    @Inject
    lateinit var firestore: FirebaseFirestore

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var noteDataFactory: NoteDataFactory

    @Inject
    lateinit var networkMapper: NetworkMapper

    init {
        injectTest()
        signIn()
        insertTestData()
    }

    @Before
    fun before() {
        noteFirestoreService = NoteFirestoreServiceImpl(
            firebaseAuth = firebaseAuth,
            firestore = firestore,
            networkMapper = networkMapper
        )
    }

    private fun signIn() = runBlocking {
        firebaseAuth.signInWithEmailAndPassword(
                EMAIL,
                PASSWORD
        ).await()
    }

    private fun insertTestData() {
        val entityList = networkMapper.noteListToEntityList(
                noteDataFactory.produceListOfNotes()
        )
        for(entity in entityList) {
            firestore.collection(NOTES_COLLECTION)
                    .document(USER_ID)
                    .collection(NOTES_COLLECTION)
                    .document(entity.id)
                    .set(entity)
        }
    }

    @Test //CBS > Confirm by Searching
    fun insertSingleNote_CBS() = runBlocking {
        val note = noteDataFactory.createSingleNote(
            id = UUID.randomUUID().toString(),
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString()
        )

        noteFirestoreService.insertOrUpdateNote(note)

        val searchResult = noteFirestoreService.searchNote(note)
        assertEquals(searchResult, note)
    }

    @Test
    fun updateSingleNote_CBS() = runBlocking {
        val allNotes = noteFirestoreService.getAllNotes()

        val randomNote = allNotes.get(Random.nextInt(0, allNotes.size -1) + 1)
        val UPDATED_TITLE = UUID.randomUUID().toString()
        val UPDATED_BODY = UUID.randomUUID().toString()
        var updatedNote = noteDataFactory.createSingleNote(
                id = randomNote.id,
                title = UPDATED_TITLE,
                body = UPDATED_BODY
        )

        noteFirestoreService.insertOrUpdateNote(updatedNote)

        updatedNote = noteFirestoreService.searchNote(updatedNote)!!

        assertEquals(UPDATED_TITLE, updatedNote.title)
        assertEquals(UPDATED_BODY, updatedNote.body)

    }

    @Test
    fun insertNoteList_CBS() = runBlocking {
        val notesToInserted = noteDataFactory.createNoteList(10)

        noteFirestoreService.insertOrUpdateNotes(notesToInserted)

        val searchResults = noteFirestoreService.getAllNotes()
        assertTrue { searchResults.containsAll(notesToInserted) }
    }

    @Test
    fun deleteSingleNote_CBS() = runBlocking {
        val allNotes = noteFirestoreService.getAllNotes()
        val randomNoteToDelete = allNotes.get(Random.nextInt(0, allNotes.size - 1) + 1)

        noteFirestoreService.deleteNote(randomNoteToDelete.id)

        val searchResult = noteFirestoreService.getAllNotes()
        assertFalse { searchResult.contains(randomNoteToDelete) }
    }

    @Test
    fun insertIntoDeletesNode_CBS() = runBlocking {
        val allNotes = noteFirestoreService.getAllNotes()
        val randomNoteToDelete = allNotes.get(Random.nextInt(0, allNotes.size - 1) + 1)

        noteFirestoreService.insertDeletedNote(randomNoteToDelete)

        val searchResults = noteFirestoreService.getDeletedNotes()

        assertTrue { searchResults.contains(randomNoteToDelete) }
    }

    @Test
    fun insertListIntoDeletesNode() = runBlocking {
        val allNotes = ArrayList<Note>(noteFirestoreService.getAllNotes())
        val notesToDelete = ArrayList<Note>()

        var randomNoteToDelete: Note
        repeat(5) {
            randomNoteToDelete = allNotes.get(Random.nextInt(0, allNotes.size - 1))
            allNotes.remove(randomNoteToDelete)
            notesToDelete.add(randomNoteToDelete)
        }

        noteFirestoreService.insertDeletedNotes(notesToDelete)

        val searchResults = noteFirestoreService.getAllNotes()
        assertTrue { searchResults.containsAll(notesToDelete) }
    }

    @Test
    fun deleteDeletedNote_CBS() = runBlocking {
        val note = noteDataFactory.createSingleNote(
                id = UUID.randomUUID().toString(),
                title = UUID.randomUUID().toString(),
                body = UUID.randomUUID().toString()
        )

        noteFirestoreService.insertDeletedNote(note)

        var searchResults = noteFirestoreService.getDeletedNotes()
        assertTrue { searchResults.contains(note) }

        noteFirestoreService.deleteDeletedNote(note)

        searchResults = noteFirestoreService.getDeletedNotes()
        assertFalse { searchResults.contains(note) }

    }
/*
    @Test
    fun queryAllNotes() = runBlocking {
        val notes = noteFirestoreService.getAllNotes()
        printLogD(this.javaClass, "notes: ${notes.size}")
        assertTrue { notes.size == 11 }
    }
    */

    override fun injectTest() {
        (application.appComponent as TestAppComponent).inject(this)
    }

}