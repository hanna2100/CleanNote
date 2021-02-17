package com.hanna2100.cleannote.business.interactors.splash

import com.hanna2100.cleannote.business.data.cache.CacheResponseHandler
import com.hanna2100.cleannote.business.data.cache.abstraction.NoteCacheDataSource
import com.hanna2100.cleannote.business.data.network.ApiResponseHandler
import com.hanna2100.cleannote.business.data.network.abstraction.NoteNetworkDataSource
import com.hanna2100.cleannote.business.data.util.safeApiCall
import com.hanna2100.cleannote.business.data.util.safeCacheCall
import com.hanna2100.cleannote.business.domain.model.Note
import com.hanna2100.cleannote.business.domain.state.DataState
import com.hanna2100.cleannote.util.printLogD
import kotlinx.coroutines.Dispatchers.IO

class SyncDeletedNotes(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
) {
    suspend fun syncDeletedNotes() {
        val apiResult = safeApiCall(IO) {
            noteNetworkDataSource.getDeletedNotes()
        }

        val response = object : ApiResponseHandler<List<Note>, List<Note>>(
            response = apiResult,
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

        val notes = response?.data?: ArrayList()

        val cacheResult = safeCacheCall(IO) {
            noteCacheDataSource.deleteNotes(notes)
        }

        object : CacheResponseHandler<Int, Int>(
            response = cacheResult,
            stateEvent = null
        ) {
            override fun handleSuccess(result: Int): DataState<Int>? {
                // 로그 찍기용
                printLogD(this.javaClass, "num deleted notes: $result")
                return DataState.data(null, null, null)
            }

        }
    }

}