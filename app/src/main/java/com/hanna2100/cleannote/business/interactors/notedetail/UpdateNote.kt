package com.hanna2100.cleannote.business.interactors.notedetail

import com.hanna2100.cleannote.business.data.cache.CacheResponseHandler
import com.hanna2100.cleannote.business.data.cache.abstraction.NoteCacheDataSource
import com.hanna2100.cleannote.business.data.network.abstraction.NoteNetworkDataSource
import com.hanna2100.cleannote.business.data.util.safeApiCall
import com.hanna2100.cleannote.business.data.util.safeCacheCall
import com.hanna2100.cleannote.business.domain.model.Note
import com.hanna2100.cleannote.business.domain.state.*
import com.hanna2100.cleannote.framwork.presentation.notedetail.state.NoteDetailViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UpdateNote(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
) {

    companion object {
        val UPDATE_NOTE_SUCCESS = "노트 업데이트 성공함."
        val UPDATE_NOTE_FAILURE = "노트 업데이트 실패함."
    }

    fun updateNote(
        note: Note,
        stateEvent: StateEvent
    ): Flow<DataState<NoteDetailViewState>?> = flow {
        val cacheResult = safeCacheCall(IO) {
            noteCacheDataSource.updateNote(
                primaryKey = note.id,
                newTitle = note.title,
                newBody = note.body
            )
        }

        val response = object : CacheResponseHandler<NoteDetailViewState, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override fun handleSuccess(result: Int): DataState<NoteDetailViewState>? {
                return if (result > 0) {
                    DataState.data(
                        response = Response(
                            message = UPDATE_NOTE_SUCCESS,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Success()
                        ),
                        data = null,
                        stateEvent = stateEvent
                    )
                } else {
                    return DataState.data(
                        response = Response(
                            message = UPDATE_NOTE_FAILURE,
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
        updateNetwork(response?.stateMessage?.response?.message, note)
    }

    private suspend fun updateNetwork(response: String?, note: Note) {
        if(response.equals(UPDATE_NOTE_SUCCESS)) {
            safeApiCall(IO) {
                noteNetworkDataSource.insertOrUpdateNote(note)
            }
        }
    }
}