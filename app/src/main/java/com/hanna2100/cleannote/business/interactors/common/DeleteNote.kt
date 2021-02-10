package com.hanna2100.cleannote.business.interactors.common

import com.hanna2100.cleannote.business.data.cache.CacheResponseHandler
import com.hanna2100.cleannote.business.data.cache.abstraction.NoteCacheDataSource
import com.hanna2100.cleannote.business.data.network.abstraction.NoteNetworkDataSource
import com.hanna2100.cleannote.business.data.util.safeApiCall
import com.hanna2100.cleannote.business.data.util.safeCacheCall
import com.hanna2100.cleannote.business.domain.model.Note
import com.hanna2100.cleannote.business.domain.state.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeleteNote<ViewState>(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
) {

    companion object {
        const val DELETE_NOTE_SUCCESS = "성공적으로 노트를 삭제함."
        const val DELETE_NOTE_FAILURE = "노트를 삭제를 실패함."
    }

    fun deleteNote(
        note: Note,
        stateEvent: StateEvent
    ): Flow<DataState<ViewState>?> = flow {

        val cacheResult = safeCacheCall(IO) {
            noteCacheDataSource.deleteNote(note.id)
        }
        val response = object : CacheResponseHandler<ViewState, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override fun handleSuccess(result: Int): DataState<ViewState> {
                return if(result > 0) {
                    DataState.data(
                        response = Response(
                            message = DELETE_NOTE_SUCCESS,
                            uiComponentType = UIComponentType.None(),
                            messageType = MessageType.Success()
                        ),
                        data = null,
                        stateEvent = stateEvent
                    )
                } else {
                    DataState.data(
                        response = Response(
                            message = DELETE_NOTE_FAILURE,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Error()
                        ),
                        data = null,
                        stateEvent = stateEvent
                    )
                }
            }
        }.getResult()

        emit(response)
        updateNetwork(
            message = response?.stateMessage?.response?.message,
            note = note
        )
    }

    private suspend fun updateNetwork(message: String?, note: Note) {
        if(message.equals(DELETE_NOTE_SUCCESS)) {
            // 네트워크의 'notes'의 노트도 삭제
            safeApiCall(IO) {
                noteNetworkDataSource.deleteNote(note.id)
            }
            // 'deletes' 의 삭제된 노트 추가
            safeApiCall(IO) {
                noteNetworkDataSource.insertDeletedNote(note)
            }
        }
    }
}