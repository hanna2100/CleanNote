package com.hanna2100.cleannote.business.interactors.notelist

import com.hanna2100.cleannote.business.data.cache.FORCE_DELETE_NOTE_EXCEPTION
import com.hanna2100.cleannote.business.data.cache.abstraction.NoteCacheDataSource
import com.hanna2100.cleannote.business.data.network.abstraction.NoteNetworkDataSource
import com.hanna2100.cleannote.business.di.DependencyContainer
import com.hanna2100.cleannote.business.domain.model.Note
import com.hanna2100.cleannote.business.domain.model.NoteFactory
import com.hanna2100.cleannote.business.domain.state.DataState
import com.hanna2100.cleannote.business.interactors.notelist.DeleteMultipleNotes.Companion.DELETE_NOTES_ERRORS
import com.hanna2100.cleannote.business.interactors.notelist.DeleteMultipleNotes.Companion.DELETE_NOTES_SUCCESS
import com.hanna2100.cleannote.framework.presentation.notelist.state.NoteListStateEvent
import com.hanna2100.cleannote.framework.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.collections.ArrayList

/*
테스트 케이스
1.  deleteNotes_success_confirmNetworkAndCacheUpdated()
    1) 지우려는 노트들을 랜덤 선택함
    2) 캐시와 네트워크에서 노트들 삭제
    3) flow 에서 DELETE_NOTES_SUCCESS emit 확인
    4) 네트워크의 'notes' 노드에 삭제되었는 지 확인
    5) 네트워크의 'deletes' 노드에 삭제된 노트가 주가되었는 지 확인
    6) 캐시에서 삭제되었는 지 확인
2. deleteNotes_fail_confirmCorrectDeletesMade()
    // 주의: DELETE_NOTES_ERRORS 결과가 나왔다 할 지라도 나머지 노트들은 삭제가 잘 되었는 지 확인해야함
    1) 지우려는 노트들을 랜덤 선택함
    2) 몇개의 노트는 id 값을 임의로 바꿔서 에러발생을 유도함
    3) flow 에서 DELETE_NOTES_ERRORS emit 확인
    4) id 를 임의로 바꾸지 않은 나머지 노트들이 네트워크의 'notes' 노드에 삭제되었는 지 확인
    5) id 를 임의로 바꾸지 않은 나머지 노트들이 네트워크의 'deletes' 노드에 삭제된 노트가 주가되었는 지 확인
    6) id 를 임의로 바꾸지 않은 나머지 노트들이 캐시에서 삭제되었는 지 확인
3. throwException_checkGenericError_confirmNetworkAndCacheUnchanged()
    1) 지우려는 노트들을 랜덤 선택함
    2) exception 을 강제함
    3) flow 에서 DELETE_NOTES_ERRORS emit 확인
    4) id 를 임의로 바꾸지 않은 나머지 노트들이 네트워크의 'notes' 노드에 삭제되었는 지 확인
    5) id 를 임의로 바꾸지 않은 나머지 노트들이 네트워크의 'deletes' 노드에 삭제된 노트가 주가되었는 지 확인
    6) id 를 임의로 바꾸지 않은 나머지 노트들이 캐시에서 삭제되었는 지 확인
 */
@InternalCoroutinesApi
class DeleteMultipleNotesTest {
    // system in test
    private var deleteMultipleNotesTest: DeleteMultipleNotes? = null
    // dependencies
    private lateinit var dependencyContainer: DependencyContainer
    private lateinit var  noteCacheDataSource: NoteCacheDataSource
    private lateinit var  noteNetworkDataSource: NoteNetworkDataSource
    private lateinit var noteFactory: NoteFactory

    @AfterEach
    fun afterEach() {
        deleteMultipleNotesTest = null
    }

    @BeforeEach
    fun beforeEach() {
        dependencyContainer = DependencyContainer()
        dependencyContainer.build()
        noteCacheDataSource = dependencyContainer.noteCacheDataSource
        noteNetworkDataSource = dependencyContainer.noteNetworkDataSource
        noteFactory = dependencyContainer.noteFactory
        deleteMultipleNotesTest = DeleteMultipleNotes(
            noteCacheDataSource = noteCacheDataSource,
            noteNetworkDataSource = noteNetworkDataSource
        )
    }

    @Test
    fun deleteNotes_success_confirmNetworkAndCacheUpdated() = runBlocking {
        val randomNotes = ArrayList<Note>()
        val notesInCache = noteCacheDataSource.searchNotes("", "", 1)

        for(note in notesInCache) {
            randomNotes.add(note)
            if (randomNotes.size > 4) {
                break
            }
        }

        deleteMultipleNotesTest?.deleteNote(
            notes = randomNotes,
            stateEvent = NoteListStateEvent.DeleteMultipleNotesEvent(randomNotes)
        )?.collect(object : FlowCollector<DataState<NoteListViewState>?> {
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    DELETE_NOTES_SUCCESS
                )
            }
        })

        val deletedNetworkNotes = noteNetworkDataSource.getDeletedNotes()
        assertTrue { deletedNetworkNotes.containsAll(randomNotes) }

        val doNotesExistInNetwork = noteNetworkDataSource.getAllNotes().containsAll(deletedNetworkNotes)
        assertFalse { doNotesExistInNetwork }

        for(note in randomNotes) {
            val noteInCache = noteCacheDataSource.searchNoteById(note.id)
            assertTrue { noteInCache == null }
        }
    }

    @Test
    fun deleteNotes_fail_confirmCorrectDeletesMade() = runBlocking {

        val validNotes = ArrayList<Note>()
        val invalidNotes = ArrayList<Note>()
        val notesInCache = noteCacheDataSource.searchNotes("", "", 1)

        for(index in 0..notesInCache.size) {
            var note: Note
            if(index % 2 == 0) {
                note = noteFactory.createSingleNote(
                    id = UUID.randomUUID().toString(),
                    title = notesInCache.get(index).title,
                    body = notesInCache.get(index).body
                )
                invalidNotes.add(note)
            } else {
                note = notesInCache.get(index)
                validNotes.add(note)
            }
            if((invalidNotes.size + validNotes.size) > 4) {
                break
            }
        }

        val notesToDelete = ArrayList(validNotes + invalidNotes)

        deleteMultipleNotesTest?.deleteNote(
            notes = notesToDelete,
            stateEvent = NoteListStateEvent.DeleteMultipleNotesEvent(notesToDelete)
        )?.collect(object : FlowCollector<DataState<NoteListViewState>?> {
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    DELETE_NOTES_ERRORS
                )
            }
        })

        val networkNotes = noteNetworkDataSource.getAllNotes()
        assertFalse { networkNotes.containsAll(validNotes) }

        val deletedNetworkNotes = noteNetworkDataSource.getDeletedNotes()
        assertTrue { deletedNetworkNotes.containsAll(validNotes) }
        assertFalse { deletedNetworkNotes.containsAll(invalidNotes) }

        for(note in validNotes) {
            val noteInCache = noteCacheDataSource.searchNoteById(note.id)
            assertTrue { noteInCache == null }
        }
        val numNotesInCache = noteCacheDataSource.getNumNotes()
        assertTrue { numNotesInCache == (notesInCache.size - validNotes.size) }
    }

    @Test
    fun throwException_checkGenericError_confirmNetworkAndCacheUnchanged() = runBlocking {

        val validNotes = ArrayList<Note>()
        val invalidNotes = ArrayList<Note>()
        val notesInCache = noteCacheDataSource.searchNotes("", "", 1)

        for (note in notesInCache) {
            validNotes.add(note)
            if(validNotes.size > 4) {
                break
            }
        }

        val errorNote = Note(
            id = FORCE_DELETE_NOTE_EXCEPTION,
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString(),
            created_at = UUID.randomUUID().toString(),
            updated_at = UUID.randomUUID().toString()
        )
        invalidNotes.add(errorNote)

        val notesToDelete = ArrayList(validNotes + invalidNotes)

        deleteMultipleNotesTest?.deleteNote(
            notes = notesToDelete,
            stateEvent = NoteListStateEvent.DeleteMultipleNotesEvent(notesToDelete)
        )?.collect(object : FlowCollector<DataState<NoteListViewState>?> {
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    DELETE_NOTES_ERRORS
                )
            }
        })

        val networkNotes = noteNetworkDataSource.getAllNotes()
        assertFalse { networkNotes.containsAll(validNotes) }

        val deletedNetworkNotes = noteNetworkDataSource.getDeletedNotes()
        assertTrue { deletedNetworkNotes.containsAll(validNotes) }
        assertFalse { deletedNetworkNotes.containsAll(invalidNotes) }

        for(note in validNotes) {
            val noteInCache = noteCacheDataSource.searchNoteById(note.id)
            assertTrue { noteInCache == null }
        }
        val numNotesInCache = noteCacheDataSource.getNumNotes()
        assertTrue { numNotesInCache == (notesInCache.size - validNotes.size) }
    }

}