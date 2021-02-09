package com.hanna2100.cleannote.business.interactors.notelist

import com.hanna2100.cleannote.business.data.cache.abstraction.NoteCacheDataSource
import com.hanna2100.cleannote.business.data.network.abstraction.NoteNetworkDataSource
import com.hanna2100.cleannote.business.di.DependencyContainer
import com.hanna2100.cleannote.business.domain.model.NoteFactory
import com.hanna2100.cleannote.business.domain.state.DataState
import com.hanna2100.cleannote.business.interactors.notelist.GetNumNotes.Companion.GET_NUM_NOTES_SUCCESS
import com.hanna2100.cleannote.framwork.presentation.notelist.state.NoteListStateEvent
import com.hanna2100.cleannote.framwork.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/*
1. getNumNotes_success_confirmCorrect()
    1) 캐시에서 노트의 갯수 가져오기
    2) flow 에서 GET_NUM_NOTES_SUCCESS emit 확인
    3) fake data set의 노트갯수와 비교
 */
class GetNumNotesTest {
    // system in test
    private val getNumNotes: GetNumNotes

    // dependencies
    private val dependencyContainer: DependencyContainer
    private val noteCacheDataSource: NoteCacheDataSource
    private val noteFactory: NoteFactory

    init {
        dependencyContainer = DependencyContainer()
        dependencyContainer.build()
        noteCacheDataSource = dependencyContainer.noteCacheDataSource
        noteFactory = dependencyContainer.noteFactory
        getNumNotes = GetNumNotes(
                noteCacheDataSource = noteCacheDataSource
        )
    }

    @InternalCoroutinesApi
    @Test
    fun getNumNotes_success_confirmCorrect() = runBlocking {
        var numNotes = 0
        getNumNotes.getNumNotes(
            stateEvent = NoteListStateEvent.GetNumNotesInCacheEvent()
        ).collect(object : FlowCollector<DataState<NoteListViewState>?> {
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    GET_NUM_NOTES_SUCCESS
                )
                numNotes = value?.data?.numNotesInCache?: 0
            }
        })

        val actualNumNotesInCache = noteCacheDataSource.getNumNotes()
        assertTrue { actualNumNotesInCache == numNotes}
    }
}