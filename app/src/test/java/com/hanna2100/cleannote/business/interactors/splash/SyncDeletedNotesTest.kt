package com.hanna2100.cleannote.business.interactors.splash

import com.hanna2100.cleannote.business.data.cache.abstraction.NoteCacheDataSource
import com.hanna2100.cleannote.business.data.network.abstraction.NoteNetworkDataSource
import com.hanna2100.cleannote.business.di.DependencyContainer
import com.hanna2100.cleannote.business.domain.model.Note
import com.hanna2100.cleannote.business.domain.model.NoteFactory
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/*
1. deleteNetworkNotes_confirmCacheSync()
    1) 네트워크에서 삭제할 노트들 선택
    2) 네트워크에서 노트들 삭제
    3) 동기화 실행
    4) 캐시에서 노트가 삭제되었는지 확인
 */
class SyncDeletedNotesTest {
    // system in test
    private val syncDeletedNotesTest: SyncDeletedNotes
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
        syncDeletedNotesTest = SyncDeletedNotes(
            noteCacheDataSource = noteCacheDataSource,
            noteNetworkDataSource = noteNetworkDataSource
        )
    }

    @Test
    fun deleteNetworkNotes_confirmCacheSync() = runBlocking {
        val networkNotes = noteNetworkDataSource.getAllNotes()

        val notesToDelete = ArrayList<Note>()

        for (note in networkNotes) {
            notesToDelete.add(note)
            noteNetworkDataSource.deleteNote(note.id)
            noteNetworkDataSource.insertDeletedNote(note)
            if(notesToDelete.size > 4) {
                break
            }
        }

        syncDeletedNotesTest.syncDeletedNotes()

        for (note in notesToDelete) {
            val cachedNote = noteCacheDataSource.searchNoteById(note.id)
            assertTrue { cachedNote == null}
        }
    }
}