package com.hanna2100.cleannote.business.interactors.notedetail

import com.hanna2100.cleannote.business.data.cache.CacheErrors.CACHE_ERROR_UNKNOWN
import com.hanna2100.cleannote.business.data.cache.FORCE_UPDATE_NOTE_EXCEPTION
import com.hanna2100.cleannote.business.data.cache.abstraction.NoteCacheDataSource
import com.hanna2100.cleannote.business.data.network.abstraction.NoteNetworkDataSource
import com.hanna2100.cleannote.business.di.DependencyContainer
import com.hanna2100.cleannote.business.domain.model.Note
import com.hanna2100.cleannote.business.domain.model.NoteFactory
import com.hanna2100.cleannote.business.domain.state.DataState
import com.hanna2100.cleannote.business.interactors.notedetail.UpdateNote.Companion.UPDATE_NOTE_FAILURE
import com.hanna2100.cleannote.business.interactors.notedetail.UpdateNote.Companion.UPDATE_NOTE_SUCCESS
import com.hanna2100.cleannote.framwork.presentation.notedetail.state.NoteDetailStateEvent
import com.hanna2100.cleannote.framwork.presentation.notedetail.state.NoteDetailViewState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

/*
테스트 케이스
1. updateNote_success_confirmNetworkAndCacheUpdated()
    1) 캐시에서 랜덤 노트 선택하기
    2) 노트 업데이트하기
    3) flow 에서 UPDATE_NOTE_SUCCESS 메세지 확인하기
    4) 네트워크 노트가 업데이트 됐는 지 확인
    5) 캐시에 노트가 업데이트 됐는 지 확인
2. updateNote_fail_confirmNetworkAndCacheUnchanged()
    1) 노트 업데이트를 시도하지만, 존재하지 않는 노트라서 실패를 유도함
    2) flow 에서 UPDATE_NOTE_FAILURE 메세지 확인
    3) 캐시에 노트가 업데이트 된게 없는 지 확인
3. throwException_checkGenericError_confirmNetworkAndCacheUnchanged()
    1) 노트 업데이트를 시도함, exception 강제
    2) flow 에서 UPDATE_NOTE_FAILURE 메세지 확인
    3) 캐시에 노트가 업데이트 된게 없는 지 확인
 */
@InternalCoroutinesApi
class UpdateNoteTest {
    // system in test
    private val updateNoteTest: UpdateNote
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
        updateNoteTest = UpdateNote(
            noteCacheDataSource = noteCacheDataSource,
            noteNetworkDataSource = noteNetworkDataSource
        )
    }

    @Test
    fun updateNote_success_confirmNetworkAndCacheUpdated() = runBlocking {
        val randomNote = noteCacheDataSource.searchNotes(
            query = "",
            filterAndOrder = "",
            page = 1
        ).first()

        val updatedNote = noteFactory.createSingleNote(
            id = randomNote.id,
            title = UUID.randomUUID().toString(),
            body = UUID.randomUUID().toString()
        )

        updateNoteTest.updateNote(
            note = updatedNote,
            stateEvent = NoteDetailStateEvent.UpdateNoteEvent()
        ).collect(object : FlowCollector<DataState<NoteDetailViewState>?> {
            override suspend fun emit(value: DataState<NoteDetailViewState>?) {
                assertEquals (
                    value?.stateMessage?.response?.message,
                    UPDATE_NOTE_SUCCESS
                )
            }
        })

        val cacheNote = noteCacheDataSource.searchNoteById(updatedNote.id)
        assertTrue { cacheNote == updatedNote }

        val networkNote = noteNetworkDataSource.searchNote(updatedNote)
        assertTrue { networkNote == updatedNote }
    }

    @Test
    fun updateNote_fail_confirmNetworkAndCacheUnchanged() = runBlocking {
        val updatedNote = Note(
                id = UUID.randomUUID().toString(),
                title = UUID.randomUUID().toString(),
                body = UUID.randomUUID().toString(),
                updated_at = UUID.randomUUID().toString(),
                created_at = UUID.randomUUID().toString()
        )

        updateNoteTest.updateNote(
                note = updatedNote,
                stateEvent = NoteDetailStateEvent.UpdateNoteEvent()
        ).collect(object : FlowCollector<DataState<NoteDetailViewState>?> {
            override suspend fun emit(value: DataState<NoteDetailViewState>?) {
                assertEquals (
                        value?.stateMessage?.response?.message,
                        UPDATE_NOTE_FAILURE
                )
            }
        })

        val cacheNote = noteCacheDataSource.searchNoteById(updatedNote.id)
        assertTrue { cacheNote == null }

        val networkNote = noteNetworkDataSource.searchNote(updatedNote)
        assertTrue { networkNote == null }
    }

    @Test
    fun throwException_checkGenericError_confirmNetworkAndCacheUnchanged() = runBlocking {
        val updatedNote = Note(
                id = FORCE_UPDATE_NOTE_EXCEPTION,
                title = UUID.randomUUID().toString(),
                body = UUID.randomUUID().toString(),
                updated_at = UUID.randomUUID().toString(),
                created_at = UUID.randomUUID().toString()
        )

        updateNoteTest.updateNote(
                note = updatedNote,
                stateEvent = NoteDetailStateEvent.UpdateNoteEvent()
        ).collect(object : FlowCollector<DataState<NoteDetailViewState>?> {
            override suspend fun emit(value: DataState<NoteDetailViewState>?) {
                assert(
                        value?.stateMessage?.response?.message?.contains(CACHE_ERROR_UNKNOWN)?: false
                )
            }
        })

        val cacheNote = noteCacheDataSource.searchNoteById(updatedNote.id)
        assertTrue { cacheNote == null }

        val networkNote = noteNetworkDataSource.searchNote(updatedNote)
        assertTrue { networkNote == null }
    }

}