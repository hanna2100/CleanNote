package com.hanna2100.cleannote.framework.datasource.network

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.hanna2100.cleannote.di.TestAppComponent
import com.hanna2100.cleannote.framework.BaseTest
import com.hanna2100.cleannote.framework.datasource.data.NoteDataFactory
import com.hanna2100.cleannote.framework.datasource.network.abstraction.NoteFirestoreService
import com.hanna2100.cleannote.framework.datasource.network.implementation.NoteFirestoreServiceImpl
import com.hanna2100.cleannote.framework.datasource.network.implementation.NoteFirestoreServiceImpl.Companion.NOTES_COLLECTION
import com.hanna2100.cleannote.framework.datasource.network.implementation.NoteFirestoreServiceImpl.Companion.USER_ID
import com.hanna2100.cleannote.framework.datasource.network.mappers.NetworkMapper
import com.hanna2100.cleannote.util.printLogD
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import javax.inject.Inject
import kotlin.test.assertTrue

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

    @Test
    fun a_insertSingleNote_CBS() = runBlocking {
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
    fun b_queryAllNotes() = runBlocking {
        val notes = noteFirestoreService.getAllNotes()
        printLogD(this.javaClass, "notes: ${notes.size}")
        assertTrue { notes.size == 11 }
    }

    override fun injectTest() {
        (application.appComponent as TestAppComponent).inject(this)
    }

}