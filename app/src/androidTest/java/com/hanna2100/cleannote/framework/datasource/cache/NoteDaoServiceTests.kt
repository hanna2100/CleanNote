package com.hanna2100.cleannote.framework.datasource.cache

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.hanna2100.cleannote.business.domain.model.Note
import com.hanna2100.cleannote.business.domain.util.DateUtil
import com.hanna2100.cleannote.di.TestAppComponent
import com.hanna2100.cleannote.framework.BaseTest
import com.hanna2100.cleannote.framework.datasource.cache.abstraction.NoteDaoService
import com.hanna2100.cleannote.framework.datasource.cache.database.NoteDao
import com.hanna2100.cleannote.framework.datasource.cache.implementation.NoteDaoServiceImpl
import com.hanna2100.cleannote.framework.datasource.cache.util.CacheMapper
import com.hanna2100.cleannote.framework.datasource.data.NoteDataFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


/*
    CBS = "confirm by searching 검색으로 확인함"

    테스트 케이스
    1. 시작할 때 DB에 노트가 없는지 확인(CacheTest.kt 에 있는 데이터로 테스트해야하기 때문).
    2. 노트 삽입 & CBS
    3. 노트 리스트 삽입 & CBS
    4. 노트 1000개 삽입, 필터링된 검색 쿼리가 제대로 작동하는지 확인
    5. 노트 1000개 삽입, db 크기 증가 확인
    6. 노트 삭제, 삭제 확인
    7. 노트 리스트 삭제, CBS
    8. 노트 업데이트, 업데이트 확인
    9. 노트 검색, 날짜별 정렬(ASC), 정렬 확인
    10. 노트 검색, 날짜별 정렬(DESC), 정렬 확인
    11. 노트 검색, 제목별 정렬(ASC), 정렬 확인
    12. 노트 검색, 제목별 정렬(DESC), 정렬 확인
*/

@RunWith(AndroidJUnit4ClassRunner::class)
class NoteDaoServiceTests : BaseTest() {

    // system in test
    private val noteDaoService: NoteDaoService

    // dependencies
    @Inject
    lateinit var dao: NoteDao
    @Inject
    lateinit var noteDataFactory: NoteDataFactory
    @Inject
    lateinit var dateUtil: DateUtil
    @Inject
    lateinit var cacheMapper: CacheMapper

    init {
        injectTest()
        insertTestData()
        noteDaoService = NoteDaoServiceImpl(
                noteDao = dao,
                noteMapper = cacheMapper,
                dateUtil = dateUtil
        )
    }

    fun insertTestData() = runBlocking{
        val entityList = cacheMapper.noteListToEntityList(
            noteDataFactory.produceListOfNotes()
        )
        dao.insertNotes(entityList)
    }

    //1. 시작할 때 DB에 노트가 없는지 확인(CacheTest.kt 에 있는 데이터로 테스트해야하기 때문).
    @Test
    fun a_searchNotes_confirmDbNotEmpty() = runBlocking {
        val numNotes = noteDaoService.getNumNotes()

        assertTrue { numNotes > 0 }
    }
    //2. 노트 삽입 & CBS
    @Test
    fun insertNote_CBS() = runBlocking {
        val newNote = noteDataFactory.createSingleNote(
                id = null,
                title = "테스트 제목",
                body = "테스트 내용"
        )
        noteDaoService.insertNote(newNote)
        val notes = noteDaoService.searchNotes()
        assert(notes.contains(newNote))
    }

    //3. 노트 리스트 삽입 & CBS
    @Test
    fun insertNoteList_CBS() = runBlocking {
        val noteList = noteDataFactory.createNoteList(10)
        noteDaoService.insertNotes(noteList)

        val queriedNotes = noteDaoService.getAllNotes()
        assert(queriedNotes.containsAll(noteList))
    }

    //4. 노트 1000개 삽입, 필터링된 검색 쿼리가 제대로 작동하는지 확인
    @Test
    fun insert1000Notes_searchNoteByTitle_confirm50ExpectedValues() = runBlocking {
        val noteList = noteDataFactory.createNoteList(1000)
        noteDaoService.insertNotes(noteList)

        repeat(50) {
            val randomIndex = Random.nextInt(0, noteList.size -1)
            val result = noteDaoService.searchNotesOrderByTitleASC(
                    query = noteList.get(randomIndex).title,
                    page = 1,
                    pageSize = 1
            )
            assertEquals(
                    noteList.get(randomIndex).title, result.get(0).title
            )
        }
    }

    //5. 노트 1000개 삽입, db 크기 증가 확인
    @Test
    fun insert1000Notes_confirmNumNotesInDb() = runBlocking {
        val currentNumNotes = noteDaoService.getNumNotes()
        val noteList = noteDataFactory.createNoteList(1000)
        noteDaoService.insertNotes(noteList)

        val numNotes = noteDaoService.getNumNotes()
        assertEquals(currentNumNotes+1000, numNotes)
    }

    //6. 노트 삭제, 삭제 확인
    @Test
    fun insertNote_deleteNote_confirmDeleted() = runBlocking {
        val newNote = noteDataFactory.createSingleNote(
            id = null,
            title = "테스트 노트 제목",
            body = "테스트 노트 내용"
        )
        noteDaoService.insertNote(newNote)

        var notes = noteDaoService.searchNotes()
        assert(notes.contains(newNote))

        noteDaoService.deleteNote(newNote.id)
        notes = noteDaoService.searchNotes()
        assert(!notes.contains(newNote))
    }

    //7. 노트 리스트 삭제, CBS
    @Test
    fun deleteNoteList_confirmDeleted() = runBlocking {
        val noteList: ArrayList<Note> = ArrayList(noteDaoService.searchNotes())
        val notesToDelete: ArrayList<Note> = ArrayList()
        var noteToDelete: Note

        repeat(4) {
            noteToDelete = noteList.get(Random.nextInt(0, noteList.size - 1) + 1)
            noteList.remove(noteToDelete)
            notesToDelete.add(noteToDelete)
        }

        noteDaoService.deleteNotes(notesToDelete)

        val searchResults = noteDaoService.searchNotes()
        assertFalse { searchResults.containsAll(notesToDelete) }
    }

    //8. 노트 업데이트, 업데이트 확인
    @Test
    fun insertNote_updateNote_confirmUpdated() = runBlocking {
        val newNote = noteDataFactory.createSingleNote(
            id = null,
            title = "테스트 제목",
            body = "테스트 내용"
        )
        noteDaoService.insertNote(newNote)

        delay(2000)

        val newTitle = UUID.randomUUID().toString()
        val newBody = UUID.randomUUID().toString()
        noteDaoService.updateNote(
            primaryKey = newNote.id,
            title = newTitle,
            body = newBody,
            timestamp = null
        )
        val notes = noteDaoService.searchNotes()

        var foundNote = false
        for(note in notes){
            if(note.id.equals(newNote.id)){
                foundNote = true
                assertEquals(newNote.id, note.id)
                assertEquals(newTitle, note.title)
                assertEquals(newBody, note.body)
                assert(newNote.updated_at != note.updated_at)
                assertEquals(
                        newNote.created_at,
                        note.created_at
                )
                break
            }
        }
        assertTrue { foundNote }
    }
    //9. 날짜별 정렬(ASC) 노트 검색, 정렬 확인
    @Test
    fun searchNotes_orderByDateASC_confirmOrder() = runBlocking {
        val noteList = noteDaoService.searchNotesOrderByDateASC(
            query = "",
            page = 1,
            pageSize = 100
        )

        var previousNoteDate = noteList.get(0).updated_at
        for(index in 1 until noteList.size) {
            val currentNoteDate = noteList.get(index).updated_at
            assertTrue {
                currentNoteDate >= previousNoteDate
            }
            previousNoteDate = currentNoteDate
        }
    }
    //10. 날짜별 정렬(DESC) 노트 검색, 정렬 확인
    @Test
    fun searchNotes_orderByDateDESC_confirmOrder() = runBlocking {
        val noteList = noteDaoService.searchNotesOrderByDateDESC(
                query = "",
                page = 1,
                pageSize = 100
        )

        // check that the date gets larger (newer) as iterate down the list
        var previous = noteList.get(0).updated_at
        for(index in 1..noteList.size - 1){
            val current = noteList.get(index).updated_at
            assertTrue { current <= previous }
            previous = current
        }
    }

    //11. 제목별 정렬(ASC) 노트 검색, 정렬 확인
    @Test
    fun searchNotes_orderByTitleASC_confirmOrder() = runBlocking {
        val noteList = noteDaoService.searchNotesOrderByTitleASC(
                query = "",
                page = 1,
                pageSize = 100
        )

        // check that the date gets larger (newer) as iterate down the list
        var previous = noteList.get(0).title
        for(index in 1..noteList.size - 1){
            val current = noteList.get(index).title

            assertTrue {
                listOf(previous, current)
                        .asSequence()
                        .zipWithNext { a, b ->
                            a <= b
                        }.all { it }
            }
            previous = current
        }
    }
    //12. 제목별 정렬(DESC) 노트 검색, 정렬 확인
    @Test
    fun searchNotes_orderByTitleDESC_confirmOrder() = runBlocking {
        val noteList = noteDaoService.searchNotesOrderByTitleDESC(
                query = "",
                page = 1,
                pageSize = 100
        )

        // check that the date gets larger (newer) as iterate down the list
        var previous = noteList.get(0).title
        for(index in 1..noteList.size - 1){
            val current = noteList.get(index).title

            assertTrue {
                listOf(previous, current)
                        .asSequence()
                        .zipWithNext { a, b ->
                            a >= b
                        }.all { it }
            }
            previous = current
        }
    }

    override fun injectTest() {
        (application.appComponent as TestAppComponent).inject(this)
    }

}