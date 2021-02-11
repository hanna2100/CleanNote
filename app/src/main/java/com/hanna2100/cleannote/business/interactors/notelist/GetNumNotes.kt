package com.hanna2100.cleannote.business.interactors.notelist

import com.hanna2100.cleannote.business.data.cache.CacheResponseHandler
import com.hanna2100.cleannote.business.data.cache.abstraction.NoteCacheDataSource
import com.hanna2100.cleannote.business.data.util.safeCacheCall
import com.hanna2100.cleannote.business.domain.state.*
import com.hanna2100.cleannote.framwork.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetNumNotes(
    private val noteCacheDataSource: NoteCacheDataSource
) {
    companion object {
        val GET_NUM_NOTES_SUCCESS = "캐시에 있는 노트의 갯수를 성공적으로 검색함."
        val GET_NUM_NOTES_FAILED = "캐시에 있는 노트의 갯수 검색을 실패함."
    }

    fun getNumNotes(
        stateEvent: StateEvent
    ): Flow<DataState<NoteListViewState>?> = flow {
        val cacheResult = safeCacheCall(IO) {
            noteCacheDataSource.getNumNotes()
        }

        val response = object : CacheResponseHandler<NoteListViewState, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override fun handleSuccess(result: Int): DataState<NoteListViewState>? {
                val viewState = NoteListViewState(
                    numNotesInCache = result
                )
                return DataState.data(
                    response = Response(
                        message = GET_NUM_NOTES_SUCCESS,
                        uiComponentType = UIComponentType.None(),
                        messageType = MessageType.Success()
                    ),
                    data = viewState,
                    stateEvent = stateEvent
                )
            }
        }.getResult()

        emit(response)
    }
}