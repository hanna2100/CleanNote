package com.hanna2100.cleannote.business.interactors.splash

import com.hanna2100.cleannote.business.data.cache.abstraction.NoteCacheDataSource
import com.hanna2100.cleannote.business.data.network.abstraction.NoteNetworkDataSource
import com.hanna2100.cleannote.business.di.DependencyContainer
import com.hanna2100.cleannote.business.domain.model.Note
import com.hanna2100.cleannote.business.domain.model.NoteFactory
import com.hanna2100.cleannote.business.domain.util.DateUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.collections.ArrayList

/*
테스트 케이스
1. insertNetworkNotesIntoCache()
    1) 네트워크에 새 노트들을 INSERT 함
    2) 동기화 실행
    3) 새 노트들이 캐시에도 INSERT 되었는 지 확인
2. insertCachedNotesIntoNetwork()
    1) 캐시에 새 노트를 INSERT 함
    2) 동기화 실행
    3) 새 노트들이 네트워크에도 INSERT 되었는 지 확인
3. checkCacheUpdateLogicSync()
    1) 캐시에서 랜덤 노트를 선택하고 업데이트
    2) 동기화 실행
    3) 네트워크에도 반영되었는 지 확인
4. checkNetworkUpdateLogicSync()
    1) 네트워크에서 랜덤 노트를 선택하고 업데이트
    2) 동기화 실행
    3) 캐시에도 반영되었는 지 확인
 */
class SyncNotesTest {
    // system in test
    private val syncNotesTest: SyncNotes
    // dependencies
    private val dependencyContainer: DependencyContainer
    private val noteCacheDataSource: NoteCacheDataSource
    private val noteNetworkDataSource: NoteNetworkDataSource
    private val noteFactory: NoteFactory
    private val dateUtil: DateUtil

    init {
        dependencyContainer = DependencyContainer()
        dependencyContainer.build()
        noteCacheDataSource = dependencyContainer.noteCacheDataSource
        noteNetworkDataSource = dependencyContainer.noteNetworkDataSource
        noteFactory = dependencyContainer.noteFactory
        dateUtil = dependencyContainer.dateUtil
        syncNotesTest = SyncNotes(
            noteCacheDataSource = noteCacheDataSource,
            noteNetworkDataSource = noteNetworkDataSource
        )
    }

    @Test
    fun doSuccessiveUpdatesOccur() = runBlocking {
        val newTimestamp = dateUtil.getCurrentTimestamp()
        val updatedNote = Note(
                id = noteNetworkDataSource.getAllNotes().first().id,
                title = noteNetworkDataSource.getAllNotes().first().title,
                body = noteNetworkDataSource.getAllNotes().first().body,
                created_at = noteNetworkDataSource.getAllNotes().first().created_at,
                updated_at = newTimestamp
        )
        noteNetworkDataSource.insertOrUpdateNote(updatedNote)
        // 여기까지가 1번 디바이스에서 발생한 이벤트
        // 아래서부터 2번 디바이스에서 실행함
        syncNotesTest.syncNotes()
        delay(1001)

        // 앱이 재실행되었다고 가정
        syncNotesTest.syncNotes()

        // 날짜가 업데이트 되지 않았음을 확인
        val notes = noteNetworkDataSource.getAllNotes()
        for(note in notes) {
            if(note.id.equals(updatedNote.id)) {
                assertTrue { note.updated_at.equals(newTimestamp) }
            }
        }
    }

    @Test
    fun checkUpdatedAtDates() = runBlocking {
        val newTimestamp = dateUtil.getCurrentTimestamp()
        val updatedNote = Note(
            id = noteNetworkDataSource.getAllNotes().first().id,
            title = noteNetworkDataSource.getAllNotes().first().title,
            body = noteNetworkDataSource.getAllNotes().first().body,
            created_at = noteNetworkDataSource.getAllNotes().first().created_at,
            updated_at = newTimestamp
        )
        noteNetworkDataSource.insertOrUpdateNote(updatedNote)

        syncNotesTest.syncNotes()

        val notes = noteNetworkDataSource.getAllNotes()
        for(note in notes) {
            noteCacheDataSource.searchNoteById(note.id)?.let { cacheNote ->
                if(cacheNote.id.equals(updatedNote.id)) {
                    assertTrue { cacheNote.updated_at.equals(newTimestamp) }
                } else {
                    assertFalse { cacheNote.updated_at.equals(newTimestamp) }
                }
            }
        }
    }

    @Test
    fun insertNetworkNotesIntoCache() = runBlocking {
        val newNotes = noteFactory.createNoteList(50)
        noteNetworkDataSource.insertOrUpdateNotes(newNotes)
        syncNotesTest.syncNotes()

        for(note in newNotes) {
            val cachedNote = noteCacheDataSource.searchNoteById(note.id)
            assertTrue { cachedNote != null }
        }
    }

    @Test
    fun insertCachedNotesIntoNetwork() = runBlocking {
        val newNotes = noteFactory.createNoteList(50)
        noteCacheDataSource.insertNotes(newNotes)
        syncNotesTest.syncNotes()

        for(note in newNotes) {
            val networkNote = noteNetworkDataSource.searchNote(note)
            assertTrue { networkNote != null }
        }
    }

    @Test
    fun checkCacheUpdateLogicSync() = runBlocking {
        val cachedNotes = noteCacheDataSource.searchNotes(
            query = "",
            filterAndOrder = "",
            page = 1
        )

        val notesToUpdate: ArrayList<Note> = ArrayList()
        for (note in cachedNotes) {
            val updatedNote = noteFactory.createSingleNote(
                id = note.id,
                title = UUID.randomUUID().toString(),
                body = UUID.randomUUID().toString()
            )
            notesToUpdate.add(updatedNote)
            if (notesToUpdate.size > 4) {
                break
            }
        }
        noteCacheDataSource.insertNotes(notesToUpdate)

        syncNotesTest.syncNotes()

        for (note in notesToUpdate) {
            val networkNote = noteNetworkDataSource.searchNote(note)
            assertEquals(note.id, networkNote?.id)
            assertEquals(note.title, networkNote?.title)
            assertEquals(note.body, networkNote?.body)
            assertEquals(note.updated_at, networkNote?.updated_at)
        }
    }

    @Test
    fun checkNetworkUpdateLogicSync() = runBlocking {
        val networkNotes = noteNetworkDataSource.getAllNotes()

        val notesToUpdate: ArrayList<Note> = ArrayList()
        for (note in networkNotes) {
            val updatedNote = noteFactory.createSingleNote(
                id = note.id,
                title = UUID.randomUUID().toString(),
                body = UUID.randomUUID().toString()
            )
            notesToUpdate.add(updatedNote)
            if (notesToUpdate.size > 4) {
                break
            }
        }
        noteNetworkDataSource.insertOrUpdateNotes(notesToUpdate)

        syncNotesTest.syncNotes()

        for (note in notesToUpdate) {
            val cachedNote = noteCacheDataSource.searchNoteById(note.id)
            assertEquals(note.id, cachedNote?.id)
            assertEquals(note.title, cachedNote?.title)
            assertEquals(note.body, cachedNote?.body)
            assertEquals(note.updated_at, cachedNote?.updated_at)
        }
    }
}