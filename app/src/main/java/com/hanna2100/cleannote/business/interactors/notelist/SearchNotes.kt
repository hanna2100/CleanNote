package com.hanna2100.cleannote.business.interactors.notelist

import com.hanna2100.cleannote.business.data.cache.CacheResponseHandler
import com.hanna2100.cleannote.business.data.cache.abstraction.NoteCacheDataSource
import com.hanna2100.cleannote.business.data.util.safeCacheCall
import com.hanna2100.cleannote.business.domain.model.Note
import com.hanna2100.cleannote.business.domain.state.*
import com.hanna2100.cleannote.framwork.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SearchNotes(
    private val noteCacheDataSource: NoteCacheDataSource
) {
    fun searchNotes(
        query: String,
        filterAndOrder: String,
        page: Int,
        stateEvent: StateEvent
    ): Flow<DataState<NoteListViewState>?> = flow {
        var updatedPage = page
        if (page <= 0) {
            updatedPage = 1
        }
        val cacheResult = safeCacheCall(IO) {
            noteCacheDataSource.searchNotes(
                query = query,
                filterAndOrder = filterAndOrder,
                page = updatedPage
            )
        }

        val response = object: CacheResponseHandler<NoteListViewState, List<Note>>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override fun handleSuccess(result: List<Note>): DataState<NoteListViewState>? {
                var message: String? = SEARCH_NOTES_SUCCESS
                var uiComponentType: UIComponentType = UIComponentType.None()
                if (result.isEmpty()) {
                    message = SEARCH_NOTES_NO_MATCHING_RESULTS
                    uiComponentType = UIComponentType.Toast()
                }
                return DataState.data(
                    response = Response(
                        message = message,
                        uiComponentType = uiComponentType,
                        messageType = MessageType.Success()
                    ),
                    data = NoteListViewState(
                        noteList = ArrayList(result)
                    ),
                    stateEvent = stateEvent
                )
            }
        }.getResult()

        emit(response)
    }

    companion object {
        val SEARCH_NOTES_SUCCESS = "노트 리스트를 성공적으로 검색함."
        val SEARCH_NOTES_NO_MATCHING_RESULTS = "검색어와 매칭되는 노트가 없음."
        val SEARCH_NOTES_FAILED = "노트 검색을 실패함."
    }
}