package com.hanna2100.cleannote.business.interactors.common

import com.hanna2100.cleannote.business.data.cache.CacheErrors.CACHE_ERROR_UNKNOWN
import com.hanna2100.cleannote.business.data.cache.FORCE_DELETE_NOTE_EXCEPTION
import com.hanna2100.cleannote.business.data.cache.abstraction.NoteCacheDataSource
import com.hanna2100.cleannote.business.data.network.abstraction.NoteNetworkDataSource
import com.hanna2100.cleannote.business.di.DependencyContainer
import com.hanna2100.cleannote.business.domain.model.Note
import com.hanna2100.cleannote.business.domain.state.DataState
import com.hanna2100.cleannote.business.interactors.common.DeleteNote.Companion.DELETE_NOTE_FAILURE
import com.hanna2100.cleannote.business.interactors.common.DeleteNote.Companion.DELETE_NOTE_SUCCESS
import com.hanna2100.cleannote.framework.presentation.notelist.state.NoteListStateEvent
import com.hanna2100.cleannote.framework.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

/*
테스트 케이스
1. deleteNote_success_confirmNetworkUpdated()
    1) 노트를 삭제함
    2) flow 에서 success 메세지 emit 확인
    3) 네트워크의 'notes' 노드에도 삭제되었는 지 확인
    4) 'deletes' 노드에 삭제된 노트가 새로 추가되었는 지 확인
2. deleteNote_fail_confirmNetworkUnchanged()
    1) 노트 삭제를 실패함(존재하지 않는 노트)
    2) flow 에서 failure 메세지 emit 확인
    3) 네트워크도 변화가 없는 지 확인
3. throwException_checkGenericError_confirmNetworkUnchanged()
    1) 노트 삭제를 실패함(exception 발생)
    2) flow 에서 failure 메세지 emit 확인
    3) 네트워크도 변화가 없는 지 확인
 */
@InternalCoroutinesApi
class DeleteNoteTest {
    // system in test
    private val deleteNoteTest: DeleteNote<NoteListViewState>
    // dependencies
    private val dependencyContainer: DependencyContainer
    private val noteCacheDataSource: NoteCacheDataSource
    private val noteNetworkDataSource: NoteNetworkDataSource

    init {
        dependencyContainer = DependencyContainer()
        dependencyContainer.build()
        noteCacheDataSource = dependencyContainer.noteCacheDataSource
        noteNetworkDataSource = dependencyContainer.noteNetworkDataSource
        deleteNoteTest = DeleteNote(
                noteCacheDataSource = noteCacheDataSource,
                noteNetworkDataSource = noteNetworkDataSource
        )
    }

    @Test
    fun deleteNote_success_confirmNetworkUpdated() = runBlocking {
        val noteToDelete = noteCacheDataSource.searchNotes(
            query = "",
            filterAndOrder = "",
            page = 1
        ).get(0)

        deleteNoteTest.deleteNote(
            note = noteToDelete,
            stateEvent = NoteListStateEvent.DeleteNoteEvent(noteToDelete)
        ).collect(object : FlowCollector<DataState<NoteListViewState>?> {

            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    DELETE_NOTE_SUCCESS
                )
            }
        })

        val wasNoteDeleted = !noteNetworkDataSource.getAllNotes().contains(noteToDelete)
        assertTrue { wasNoteDeleted }

        val wasDeletedNoteInserted = noteNetworkDataSource.getDeletedNotes().contains(noteToDelete)
        assertTrue { wasDeletedNoteInserted }
    }

    @Test
    fun deleteNote_fail_confirmNetworkUnchanged() = runBlocking {
        val noteToDelete = Note(
            id = UUID.randomUUID().toString(),
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString(),
            created_at = UUID.randomUUID().toString(),
            updated_at = UUID.randomUUID().toString()
        )

        deleteNoteTest.deleteNote(
            note = noteToDelete,
            stateEvent = NoteListStateEvent.DeleteNoteEvent(noteToDelete)
        ).collect(object : FlowCollector<DataState<NoteListViewState>?> {

            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    DELETE_NOTE_FAILURE
                )
            }
        })

        val notes = noteNetworkDataSource.getAllNotes()
        val numNotesInCache = noteCacheDataSource.getNumNotes()
        assertTrue { notes.size == numNotesInCache }

        val wasDeletedNoteInserted = !noteNetworkDataSource.getDeletedNotes().contains(noteToDelete)
        assertTrue { wasDeletedNoteInserted }
    }

    @Test
    fun throwException_checkGenericError_confirmNetworkUnchanged() = runBlocking {
        val noteToDelete = Note(
            id = FORCE_DELETE_NOTE_EXCEPTION,
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString(),
            created_at = UUID.randomUUID().toString(),
            updated_at = UUID.randomUUID().toString()
        )

        deleteNoteTest.deleteNote(
            note = noteToDelete,
            stateEvent = NoteListStateEvent.DeleteNoteEvent(noteToDelete)
        ).collect(object : FlowCollector<DataState<NoteListViewState>?> {

            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assert(
                    value?.stateMessage?.response?.message?.contains(CACHE_ERROR_UNKNOWN)?: false
                )
            }
        })

        val notes = noteNetworkDataSource.getAllNotes()
        val numNotesInCache = noteCacheDataSource.getNumNotes()
        assertTrue { notes.size == numNotesInCache }

        val wasDeletedNoteInserted = !noteNetworkDataSource.getDeletedNotes().contains(noteToDelete)
        assertTrue { wasDeletedNoteInserted }
    }

}