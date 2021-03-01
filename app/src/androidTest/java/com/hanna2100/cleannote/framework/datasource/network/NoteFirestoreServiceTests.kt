package com.hanna2100.cleannote.framework.datasource.network

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.hanna2100.cleannote.business.domain.model.NoteFactory
import com.hanna2100.cleannote.di.TestAppComponent
import com.hanna2100.cleannote.framework.datasource.network.abstraction.NoteFirestoreService
import com.hanna2100.cleannote.framework.datasource.network.implementation.NoteFirestoreServiceImpl
import com.hanna2100.cleannote.framework.datasource.network.mappers.NetworkMapper
import com.hanna2100.cleannote.framework.presentation.TestBaseApplication
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

@ExperimentalCoroutinesApi
@FlowPreview
@RunWith(AndroidJUnit4ClassRunner::class)
class NoteFirestoreServiceTests {

    companion object {
        const val EMAIL = "hanna0497test@naver.com"
        const val PASSWORD = "password"
    }

    private lateinit var noteFirestoreService: NoteFirestoreService

    val application: TestBaseApplication = ApplicationProvider.getApplicationContext<Context>() as TestBaseApplication

    @Inject
    lateinit var firestore: FirebaseFirestore

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var noteFactory: NoteFactory

    @Inject
    lateinit var networkMapper: NetworkMapper

    init {
        (application.appComponent as TestAppComponent)
            .inject(this)
        signIn()
    }

    @Before
    fun before() {
        noteFirestoreService = NoteFirestoreServiceImpl(
            firebaseAuth = firebaseAuth,
            firestore = firestore,
            networkMapper = networkMapper
        )
    }

    @Test
    fun insertSingleNote_CBS() = runBlocking {
        val note = noteFactory.createSingleNote(
            id = UUID.randomUUID().toString(),
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString()
        )

        noteFirestoreService.insertOrUpdateNote(note)

        val searchResult = noteFirestoreService.searchNote(note)
        assertEquals(searchResult, note)
    }

    private fun signIn() = runBlocking {
        firebaseAuth.signInWithEmailAndPassword(
            EMAIL,
            PASSWORD
        ).await()
    }

}