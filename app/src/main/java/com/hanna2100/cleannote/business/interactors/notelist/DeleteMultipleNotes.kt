package com.hanna2100.cleannote.business.interactors.notelist

import com.hanna2100.cleannote.business.data.cache.CacheResponseHandler
import com.hanna2100.cleannote.business.data.cache.abstraction.NoteCacheDataSource
import com.hanna2100.cleannote.business.data.network.abstraction.NoteNetworkDataSource
import com.hanna2100.cleannote.business.data.util.safeApiCall
import com.hanna2100.cleannote.business.data.util.safeCacheCall
import com.hanna2100.cleannote.business.domain.model.Note
import com.hanna2100.cleannote.business.domain.state.*
import com.hanna2100.cleannote.framwork.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeleteMultipleNotes(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
) {
    private var onDeleteError: Boolean = false

    companion object{
        val DELETE_NOTES_SUCCESS = "성공적으로 노트들을 삭제함."
        val DELETE_NOTES_ERRORS = "모든 노트들이 삭제되지 않음. 어떤 오류가 있음."
        val DELETE_NOTES_YOU_MUST_SELECT = "삭제할 노트가 선택되지 않음."
        val DELETE_NOTES_ARE_YOU_SURE = "정말로 노트들을 삭제하겠습니까?"
    }

    fun deleteNote(
        notes: List<Note>,
        stateEvent: StateEvent
    ): Flow<DataState<NoteListViewState>?> = flow {
        val successfulDeletes: ArrayList<Note> = ArrayList()
        for(note in notes) {
            val cacheResult = safeCacheCall(IO) {
                noteCacheDataSource.deleteNote(note.id)
            }
            val response = object: CacheResponseHandler<NoteListViewState, Int> (
                response = cacheResult,
                stateEvent = stateEvent
            ) {
                override fun handleSuccess(result: Int): DataState<NoteListViewState>? {
                    if(result < 0) { //Error
                        onDeleteError = true
                    } else {
                        successfulDeletes.add(note)
                    }
                    return null
                }
            }.getResult()

            val error = response?.stateMessage?.response?.message?.contains(stateEvent.errorInfo()) == true

            if (error) {
                onDeleteError = true
            }
        }
        if(onDeleteError) {
            emit(
                DataState.data<NoteListViewState>(
                    response = Response(
                        message = DELETE_NOTES_ERRORS,
                        uiComponentType = UIComponentType.Dialog(),
                        messageType = MessageType.Error()
                    ),
                    data = null,
                    stateEvent = stateEvent
                )
            )
        } else {
            emit(
                DataState.data<NoteListViewState>(
                    response = Response(
                        message = DELETE_NOTES_SUCCESS,
                        uiComponentType = UIComponentType.Toast(),
                        messageType = MessageType.Success()
                    ),
                    data = null,
                    stateEvent = stateEvent
                )
            )
        }

        updateNetwork(successfulDeletes)
    }

    private suspend fun updateNetwork(successfulDeletes: ArrayList<Note>) {
        for(note in successfulDeletes) {
            safeApiCall(IO) {
                noteNetworkDataSource.deleteNote(note.id)
            }

            safeApiCall(IO) {
                noteNetworkDataSource.insertDeletedNote(note)
            }
        }
    }
}