package com.hanna2100.cleannote.business.interactors.notelist

import com.hanna2100.cleannote.business.data.cache.CacheErrors
import com.hanna2100.cleannote.business.data.cache.FORCE_SEARCH_NOTES_EXCEPTION
import com.hanna2100.cleannote.business.data.cache.abstraction.NoteCacheDataSource
import com.hanna2100.cleannote.business.di.DependencyContainer
import com.hanna2100.cleannote.business.domain.model.Note
import com.hanna2100.cleannote.business.domain.model.NoteFactory
import com.hanna2100.cleannote.business.domain.state.DataState
import com.hanna2100.cleannote.business.interactors.notelist.SearchNotes.Companion.SEARCH_NOTES_NO_MATCHING_RESULTS
import com.hanna2100.cleannote.business.interactors.notelist.SearchNotes.Companion.SEARCH_NOTES_SUCCESS
import com.hanna2100.cleannote.framwork.datasource.cache.database.ORDER_BY_ASC_DATE_UPDATED
import com.hanna2100.cleannote.framwork.presentation.notelist.state.NoteListStateEvent
import com.hanna2100.cleannote.framwork.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/*
테스트 케이스
1. 빈 문자열 검색어로 노트 검색 성공 테스트
 1) 디폴트 검색 옵션으로 검색함
 2) flow 로부터 SEARCH_NOTES_SUCCESS emit 확인
 3) 검색된 노트 리스트 확인
 4) 검색된 노트들이 캐시데이터와 일치하는 지 확인

2. 랜덤한 검색어로 검색된 노트 없음 테스트
 1) 검색결과가 당연히 없을만한 검색어로 검색
 2) flow 로부터 SEARCH_NOTES_NOTE_MATCHING_RESULTS emit 확인
 3) 검색된 노트 리스트가 없는 지 확인
 4) 캐시에 노트가 있는 지(비어있는 경우는 아닌지) 확인

3. 노트 검색 실패 및 검색결과도 없는 지 확인
 1) Exception 강제함
 2) flow 로부터 CACHE_ERROR_UNKNOWN emit 확인
 3) 검색 결과가 없는 지 확인
 4)  캐시에 노트가 있는 지(비어있는 경우는 아닌지) 확인
 */

@InternalCoroutinesApi
class SearchNoteTest {
    //system in test
    private val searchNotes: SearchNotes
    // dependencies
    private val dependencyContainer: DependencyContainer
    private val noteCacheDataSource: NoteCacheDataSource
    private val noteFactory: NoteFactory

    init {
        dependencyContainer = DependencyContainer()
        dependencyContainer.build()
        noteCacheDataSource = dependencyContainer.noteCacheDataSource
        noteFactory = dependencyContainer.noteFactory
        searchNotes = SearchNotes(
                noteCacheDataSource = noteCacheDataSource
        )
    }

    @Test
    fun blankQuery_success_confirmNotesRetrieved() = runBlocking {
        val query = ""
        var results: ArrayList<Note>? = null
        searchNotes.searchNotes(
            query = query,
            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
            page = 1,
            stateEvent = NoteListStateEvent.SearchNotesEvent()
        ).collect(object : FlowCollector<DataState<NoteListViewState>?> {
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assertEquals (
                    value?.stateMessage?.response?.message,
                    SEARCH_NOTES_SUCCESS
                )
                value?.data?.noteList?.let { list ->
                    results = ArrayList(list)
                }
            }
        })
        // 노트 리스트가 검색되었는 지 확인
        assertTrue { results != null }

        // 검색된 노트들이 캐시와 매칭되는 지 확인
        val notesInCache = noteCacheDataSource.searchNotes(
            query = query,
            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
            page = 1
        )
        assertTrue { results?.containsAll(notesInCache)?: false }
    }

    @Test
    fun randomQuery_success_confirmNoResults() = runBlocking {
        val query = "dfaqwejlajsdlkfja"
        var results: ArrayList<Note>? = null
        searchNotes.searchNotes(
            query = query,
            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
            page = 1,
            stateEvent = NoteListStateEvent.SearchNotesEvent()
        ).collect(object: FlowCollector<DataState<NoteListViewState>?>{
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    SEARCH_NOTES_NO_MATCHING_RESULTS
                )
                value?.data?.noteList?.let { list ->
                    results = ArrayList(list)
                }
            }
        })

        // 아무것도 검색되지 않았는 지 확인
        assertTrue { results?.run { size == 0 }?: true }

        // 캐시에 노트가 있는 지(비어있는 경우는 아닌지) 확인
        val notesInCache = noteCacheDataSource.searchNotes(
                query = "",
                filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
                page = 1
        )
        assertTrue { notesInCache.isNotEmpty() }
    }

    @Test
    fun searchNotes_fail_confirmNoResults() = runBlocking {

        val query = FORCE_SEARCH_NOTES_EXCEPTION
        var results: ArrayList<Note>? = null
        searchNotes.searchNotes(
                query = query,
                filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
                page = 1,
                stateEvent = NoteListStateEvent.SearchNotesEvent()
        ).collect(object: FlowCollector<DataState<NoteListViewState>?>{
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assert(
                    value?.stateMessage?.response?.message
                            ?.contains(CacheErrors.CACHE_ERROR_UNKNOWN) ?: false
                )
                value?.data?.noteList?.let { list ->
                    results = ArrayList(list)
                }
                println("results: $results")
            }
        })

        // 검색된 게 없는 지 확인
        assertTrue { results?.run { size == 0 }?: true }

        // 캐시에 노트가 있는 지(비어있는 경우는 아닌지) 확인
        val notesInCache = noteCacheDataSource.searchNotes(
                query = "",
                filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
                page = 1
        )
        assertTrue { notesInCache.isNotEmpty() }
    }

}