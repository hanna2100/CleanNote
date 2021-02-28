package com.hanna2100.cleannote.business.interactors.notelist

import com.hanna2100.cleannote.business.data.cache.CacheErrors
import com.hanna2100.cleannote.business.data.cache.FORCE_GENERAL_FAILURE
import com.hanna2100.cleannote.business.data.cache.FORCE_NEW_NOTE_EXCEPTION
import com.hanna2100.cleannote.business.data.cache.abstraction.NoteCacheDataSource
import com.hanna2100.cleannote.business.data.network.abstraction.NoteNetworkDataSource
import com.hanna2100.cleannote.business.di.DependencyContainer
import com.hanna2100.cleannote.business.domain.model.Note
import com.hanna2100.cleannote.business.domain.model.NoteFactory
import com.hanna2100.cleannote.business.domain.state.DataState
import com.hanna2100.cleannote.business.interactors.notelist.InsertNewNote.Companion.INSERT_NOTE_SUCCESS
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
1. 노트 insert 성공 > 캐시&네트워크 업데이트 확인
 1) 새로운 노트 insert
 2) flow 로부터 INSERT_NOTE_SUCCESS emit 확인
 3) 새 노트가 캐시에 업데이트 되었는 지 확인
 4) 새 노트가 네트워크에 업데이트 되었는 지 확인

2. 노트 insert 실패 > 캐시&네트워크 업데이트 안됐는지 확인
 1) 새로운 노트 insert
 2) 실패 강제함(-1 리턴됨)
 3) flow 로부터 INSERT_NOTE_FAILED emit 확인
 4) 캐시가 업데이트 안되었는 지 확인
 5) 네트워크가 업데이트 안되었는 지 확인

3. GENERIC ERROR Exception 발생 > 캐시&네트워크 업데이트 안됐는지 확인
 1) 새로운 노트 insert
 2) Exception 강제함
 3) flow 로부터 CACHE_ERROR_UNKNOWN emit 확인
 4) 캐시가 업데이트 안되었는 지 확인
 5) 네트워크가 업데이트 안되었는 지 확인
 */

@InternalCoroutinesApi
class InsertNewNoteTest {
    // system in test
    private val insertNewNoteTest: InsertNewNote
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
        insertNewNoteTest = InsertNewNote(
            noteCacheDataSource = noteCacheDataSource,
            noteNetworkDataSource = noteNetworkDataSource,
            noteFactory = noteFactory
        )
    }

    @Test
    fun insertNote_success_confirmNetworkAndCacheUpdated() = runBlocking {
        val newNote: Note = noteFactory.createSingleNote(
                id = null,
                title = UUID.randomUUID().toString()
        )

        insertNewNoteTest.insertNewNote(
                id = newNote.id,
                title = newNote.title,
                stateEvent = NoteListStateEvent.InsertNewNoteEvent(title = newNote.title)
        ).collect(object : FlowCollector<DataState<NoteListViewState>?>{
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    INSERT_NOTE_SUCCESS
                )
            }
        })

        //캐시가 업데이트 되었는 지 확인
        val cacheNoteThatWasInserted = noteCacheDataSource.searchNoteById(newNote.id)
        assertTrue {
            cacheNoteThatWasInserted == newNote
        }

        //네트워크가 업데이트 되었는 지 확인
        val networkNoteThatWasInserted = noteNetworkDataSource.searchNote(newNote)
        assertTrue {
            networkNoteThatWasInserted == newNote
        }
    }

    @InternalCoroutinesApi
    @Test
    fun insertNote_fail_confirmNetworkAndCacheUnchanged() = runBlocking {

        val newNote = noteFactory.createSingleNote(
                id = FORCE_GENERAL_FAILURE,
                title = UUID.randomUUID().toString(),
                body = UUID.randomUUID().toString()
        )

        insertNewNoteTest.insertNewNote(
                id = newNote.id,
                title = newNote.title,
                stateEvent = NoteListStateEvent.InsertNewNoteEvent( title = newNote.title)
        ).collect(object: FlowCollector<DataState<NoteListViewState>?>{
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assertEquals(
                        value?.stateMessage?.response?.message,
                        InsertNewNote.INSERT_NOTE_FAILED
                )
            }
        })

        // 네트워크가 업데이트 안되었는 지 확인
        val networkNoteThatWasInserted = noteNetworkDataSource.searchNote(newNote)
        assertTrue { networkNoteThatWasInserted == null }

        // 캐시가 업데이트 안되었는 지 확인
        val cacheNoteThatWasInserted = noteCacheDataSource.searchNoteById(newNote.id)
        assertTrue { cacheNoteThatWasInserted == null }
    }

    @InternalCoroutinesApi
    @Test
    fun throwException_checkGenericError_confirmNetworkAndCacheUnchanged() = runBlocking {

        val newNote = noteFactory.createSingleNote(
                id = FORCE_NEW_NOTE_EXCEPTION,
                title = UUID.randomUUID().toString(),
                body = UUID.randomUUID().toString()
        )

        insertNewNoteTest.insertNewNote(
                id = newNote.id,
                title = newNote.title,
                stateEvent = NoteListStateEvent.InsertNewNoteEvent(newNote.title)
        ).collect(object: FlowCollector<DataState<NoteListViewState>?>{
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assert(
                        value?.stateMessage?.response?.message
                                ?.contains(CacheErrors.CACHE_ERROR_UNKNOWN)?: false
                )
            }
        })

        // 네트워크가 업데이트 안되었는 지 확인
        val networkNoteThatWasInserted = noteNetworkDataSource.searchNote(newNote)
        assertTrue { networkNoteThatWasInserted == null }

        // 캐시가 업데이트 안되었는 지 확인
        val cacheNoteThatWasInserted = noteCacheDataSource.searchNoteById(newNote.id)
        assertTrue { cacheNoteThatWasInserted == null }
    }
}