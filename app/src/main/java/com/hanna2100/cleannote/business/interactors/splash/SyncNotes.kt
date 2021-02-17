package com.hanna2100.cleannote.business.interactors.splash

import com.hanna2100.cleannote.business.data.cache.CacheResponseHandler
import com.hanna2100.cleannote.business.data.cache.abstraction.NoteCacheDataSource
import com.hanna2100.cleannote.business.data.network.ApiResponseHandler
import com.hanna2100.cleannote.business.data.network.abstraction.NoteNetworkDataSource
import com.hanna2100.cleannote.business.data.util.safeApiCall
import com.hanna2100.cleannote.business.data.util.safeCacheCall
import com.hanna2100.cleannote.business.domain.model.Note
import com.hanna2100.cleannote.business.domain.state.DataState
import com.hanna2100.cleannote.business.domain.util.DateUtil
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SyncNotes(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
) {

    suspend fun syncNotes() {
        val cachedNotesList = getCacheNotes()
        syncNetworkNotesWithCachedNotes(ArrayList(cachedNotesList))
    }

    private suspend fun getCacheNotes(): List<Note> {
        val cacheResult = safeCacheCall(IO) {
            noteCacheDataSource.getAllNotes()
        }

        val response = object : CacheResponseHandler<List<Note>, List<Note>> (
            response = cacheResult,
            stateEvent = null
        ) {
            override fun handleSuccess(result: List<Note>): DataState<List<Note>>? {
                return DataState.data(
                    response = null,
                    data = result,
                    stateEvent = null
                )
            }
        }.getResult()

        return response?.data?: ArrayList()
    }

    /*
    네트워크에서 모든 노트를 가져오고,
    만약 캐시에 없는 게 네트워크에 있으면, 캐시에 INSERT 함
    만약 캐시에 있는 게 네트워크에도 있으면, 비교하여 최신걸로 업데이트 함
    
     */
    private suspend fun syncNetworkNotesWithCachedNotes(
        cachedNotes: ArrayList<Note>
    ) {
        return withContext(IO) {
            val networkResult = safeApiCall(IO) {
                noteNetworkDataSource.getAllNotes()
            }

            val response = object : ApiResponseHandler<List<Note>, List<Note>>(
                response = networkResult,
                stateEvent = null
            ) {
                override suspend fun handleSuccess(resultObj: List<Note>): DataState<List<Note>> {
                    return DataState.data(
                        response = null,
                        data = resultObj,
                        stateEvent = null
                    )
                }
            }.getResult()

            val noteList = response.data?: ArrayList()

            val job = launch {
                for(note in noteList) {
                    noteCacheDataSource.searchNoteById(note.id)?.let { cachedNote ->
                        cachedNotes.remove(cachedNote)
                        checkIfCachedNoteRequiresUpdate(cachedNote, note)
                    }?: noteCacheDataSource.insertNote(note)
                }
            }
            job.join() // wait

            // 남아있는 것들을 네트워크에 넣기
            for(cachedNote in cachedNotes) {
                safeApiCall(IO) {
                    noteNetworkDataSource.insertOrUpdateNote(cachedNote)
                }
            }
        }
    }

    private suspend fun checkIfCachedNoteRequiresUpdate(
        cachedNote: Note,
        networkNote: Note
    ) {
        val cacheUpdatedAt = cachedNote.updated_at
        val networkUpdatedAt = networkNote.updated_at

        if(networkUpdatedAt > cacheUpdatedAt) {
            safeApiCall(IO) {
                noteCacheDataSource.updateNote(
                    primaryKey = networkNote.id,
                    newTitle = networkNote.title,
                    newBody = networkNote.body
                )
            }
        } else {
            safeApiCall(IO) {
                noteNetworkDataSource.insertOrUpdateNote(cachedNote)
            }
        }
    }

}