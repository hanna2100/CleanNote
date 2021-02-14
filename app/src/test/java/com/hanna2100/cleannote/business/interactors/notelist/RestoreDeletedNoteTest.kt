package com.hanna2100.cleannote.business.interactors.notelist

import com.hanna2100.cleannote.business.data.cache.CacheErrors.CACHE_ERROR_UNKNOWN
import com.hanna2100.cleannote.business.data.cache.FORCE_GENERAL_FAILURE
import com.hanna2100.cleannote.business.data.cache.FORCE_NEW_NOTE_EXCEPTION
import com.hanna2100.cleannote.business.data.cache.abstraction.NoteCacheDataSource
import com.hanna2100.cleannote.business.data.network.abstraction.NoteNetworkDataSource
import com.hanna2100.cleannote.business.di.DependencyContainer
import com.hanna2100.cleannote.business.domain.model.NoteFactory
import com.hanna2100.cleannote.business.domain.state.DataState
import com.hanna2100.cleannote.business.interactors.notelist.RestoreDeletedNote.Companion.RESTORE_NOTE_FAILED
import com.hanna2100.cleannote.business.interactors.notelist.RestoreDeletedNote.Companion.RESTORE_NOTE_SUCCESS
import com.hanna2100.cleannote.framwork.presentation.notelist.state.NoteListStateEvent
import com.hanna2100.cleannote.framwork.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

/*
1. restoreNote_success_confirmCacheAndNetworkUpdated()
    1) 새로운 노트를 생성하고, 네트워크의 'deleted'노드에 추가한다.
    2) 해당 노트를 복구한다.
    3) flow 에서 RESTORE_NOTE_SUCCESS 메세지를 받는다
    4) 캐시에 노트가 있는 지 확인
    5) 네트워크의 'notes'노드에 노트가 있는 지 확인
    6) 'deleted'노드에 노트가 없는 지 확인
2. restoreNote_fail_confirmCacheAndNetworkUnchanged()
    1) 새로운 노트를 생성하고, 네트워크의 'deleted'노드에 추가한다.
    2) 해당 노트를 복구한다.
    3) flow 에서 RESTORE_NOTE_FAILED 메세지를 받는다
    4) 캐시에 노트가 없는 지 확인
    5) 네트워크의 'notes'노드에 노트가 없는 지 확인
    6) 'deleted'노드에 노트가 있는 지 확인
3. throwException_checkGenericError_confirmNetworkAndCacheUnchanged()
    1) 새로운 노트를 생성하고, 네트워크의 'deleted'노드에 추가한다.
    2) 해당 노트를 복구한다. (이때 exception 강제)
    3) flow 에서 CACHE_ERROR_UNKNOWN 메세지를 받는다
    4) 캐시에 노트가 없는 지 확인
    5) 네트워크의 'notes'노드에 노트가 없는 지 확인
    6) 'deleted'노드에 노트가 있는 지 확인
 */

@InternalCoroutinesApi
class RestoreDeletedNoteTest {
    // system in test
    private val restoreDeletedNoteTest: RestoreDeletedNote
    // dependencies
    private val dependencyContainer: DependencyContainer
    private val noteCacheDataSource: NoteCacheDataSource
    private val noteNetworkDataSource: NoteNetworkDataSource
    private val noteFactory: NoteFactory

    init {
        dependencyContainer = DependencyContainer()
        dependencyContainer.build()
        noteCacheDataSource = dependencyContainer.noteCacheDataSource
        noteNetworkDataSource = dependencyContainer.noteNetworkDataSource
        noteFactory = dependencyContainer.noteFactory
        restoreDeletedNoteTest = RestoreDeletedNote(
                noteCacheDataSource = noteCacheDataSource,
                noteNetworkDataSource = noteNetworkDataSource
        )
    }

    @Test
    fun restoreNote_success_confirmCacheAndNetworkUpdated() = runBlocking {
        val restoredNote = noteFactory.createSingleNote(
            id = UUID.randomUUID().toString(),
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString()
        )
        noteNetworkDataSource.insertDeletedNote(restoredNote)

        // restore 하기전에 'deletes'노드에 노트가 있는 지 확인
        var deletedNotes = noteNetworkDataSource.getDeletedNotes()
        assertTrue{ deletedNotes.contains(restoredNote) }

        restoreDeletedNoteTest.restoreDeletedNote(
            note = restoredNote,
            stateEvent = NoteListStateEvent.RestoreDeletedNoteEvent(restoredNote)
        ).collect(object: FlowCollector<DataState<NoteListViewState>?>{
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    RESTORE_NOTE_SUCCESS
                )
            }
        })

        val noteInCache = noteCacheDataSource.searchNoteById(restoredNote.id)
        assertTrue { noteInCache == restoredNote }

        val noteInNetwork = noteCacheDataSource.searchNoteById(restoredNote.id)
        assertTrue {  noteInNetwork == restoredNote}

        deletedNotes = noteNetworkDataSource.getDeletedNotes()
        assertFalse{ deletedNotes.contains(restoredNote) }

    }

    @Test
    fun restoreNote_fail_confirmCacheAndNetworkUnchanged() = runBlocking {
        val restoredNote = noteFactory.createSingleNote(
                id = FORCE_GENERAL_FAILURE,
                title = UUID.randomUUID().toString(),
                body = UUID.randomUUID().toString()
        )

        noteNetworkDataSource.insertDeletedNote(restoredNote)

        // restore 하기전에 'deletes'노드에 노트가 있는 지 확인
        var deletedNotes = noteNetworkDataSource.getDeletedNotes()
        assertTrue{ deletedNotes.contains(restoredNote) }

        restoreDeletedNoteTest.restoreDeletedNote(
                note = restoredNote,
                stateEvent = NoteListStateEvent.RestoreDeletedNoteEvent(restoredNote)
        ).collect(object: FlowCollector<DataState<NoteListViewState>?>{
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    RESTORE_NOTE_FAILED
                )
            }
        })

        val noteInCache = noteCacheDataSource.searchNoteById(restoredNote.id)
        assertTrue { noteInCache == null }

        val noteInNetwork = noteCacheDataSource.searchNoteById(restoredNote.id)
        assertTrue {  noteInNetwork == null}

        deletedNotes = noteNetworkDataSource.getDeletedNotes()
        assertTrue{ deletedNotes.contains(restoredNote) }

    }

    @Test
    fun throwException_checkGenericError_confirmNetworkAndCacheUnchanged() = runBlocking {
        val restoredNote = noteFactory.createSingleNote(
            id = FORCE_NEW_NOTE_EXCEPTION,
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString()
        )

        noteNetworkDataSource.insertDeletedNote(restoredNote)

        // restore 하기전에 'deletes'노드에 노트가 있는 지 확인
        var deletedNotes = noteNetworkDataSource.getDeletedNotes()
        assertTrue{ deletedNotes.contains(restoredNote) }

        restoreDeletedNoteTest.restoreDeletedNote(
                note = restoredNote,
                stateEvent = NoteListStateEvent.RestoreDeletedNoteEvent(restoredNote)
        ).collect(object: FlowCollector<DataState<NoteListViewState>?>{
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assert(
                        value?.stateMessage?.response?.message?.contains(CACHE_ERROR_UNKNOWN)?: false
                )
            }
        })

        val noteInCache = noteCacheDataSource.searchNoteById(restoredNote.id)
        assertTrue { noteInCache == null }

        val noteInNetwork = noteCacheDataSource.searchNoteById(restoredNote.id)
        assertTrue {  noteInNetwork == null}

        deletedNotes = noteNetworkDataSource.getDeletedNotes()
        assertTrue{ deletedNotes.contains(restoredNote) }

    }
}