package com.hanna2100.cleannote.business.data.cache

import com.hanna2100.cleannote.business.data.cache.abstraction.NoteCacheDataSource
import com.hanna2100.cleannote.business.domain.model.Note
import com.hanna2100.cleannote.business.domain.util.DateUtil
import com.hanna2100.cleannote.framework.datasource.cache.database.NOTE_PAGINATION_PAGE_SIZE

const val FORCE_DELETE_NOTE_EXCEPTION = "FORCE_DELETE_NOTE_EXCEPTION"
const val FORCE_DELETES_NOTE_EXCEPTION = "FORCE_DELETES_NOTE_EXCEPTION"
const val FORCE_UPDATE_NOTE_EXCEPTION = "FORCE_UPDATE_NOTE_EXCEPTION"
const val FORCE_NEW_NOTE_EXCEPTION = "FORCE_NEW_NOTE_EXCEPTION"
const val FORCE_SEARCH_NOTES_EXCEPTION = "FORCE_SEARCH_NOTES_EXCEPTION"
const val FORCE_GENERAL_FAILURE = "FORCE_GENERAL_FAILURE"

class FakeNoteCacheDataSourceImpl
constructor(
    private val notesData: HashMap<String, Note>,
    private val dateUtil: DateUtil
): NoteCacheDataSource{
    override suspend fun insertNote(note: Note): Long {
        if (note.id.equals(FORCE_NEW_NOTE_EXCEPTION)) {
            throw Exception("Something went wrong inserting the note.")
        }
        if (note.id.equals(FORCE_GENERAL_FAILURE)) {
            return -1 //실패
        }
        notesData.put(note.id, note)
        return 1 //성공
    }

    override suspend fun deleteNote(primaryKey: String): Int {
        if(primaryKey.equals(FORCE_DELETE_NOTE_EXCEPTION)){
            throw Exception("Something went wrong deleting the note.")
        }
        else if(primaryKey.equals(FORCE_DELETES_NOTE_EXCEPTION)){
            throw Exception("Something went wrong deleting the note.")
        }
        return notesData.remove(primaryKey)?.let {
            1 // 성공
        }?: - 1 // 실패
    }

    override suspend fun deleteNotes(notes: List<Note>): Int {
        var failOrSuccess = 1
        for (note in notes) {
            if (notesData.remove(note.id) == null) {
                failOrSuccess = -1
            }
        }
        return failOrSuccess
    }

    override suspend fun updateNote(primaryKey: String, newTitle: String, newBody: String): Int {
        if(primaryKey.equals(FORCE_UPDATE_NOTE_EXCEPTION)){
            throw Exception("Something went wrong updating the note.")
        }
        val updatedNote = Note(
            id = primaryKey,
            title = newTitle,
            body = newBody?: "",
            updated_at = dateUtil.getCurrentTimestamp(),
            created_at = notesData.get(primaryKey)?.created_at?: dateUtil.getCurrentTimestamp()
        )
        return notesData.get(primaryKey)?.let {
            notesData.put(primaryKey, updatedNote)
            1 // 성공
        }?: -1 // 실패
    }

    override suspend fun searchNotes(query: String, filterAndOrder: String, page: Int): List<Note> {
        if(query.equals(FORCE_SEARCH_NOTES_EXCEPTION)){
            throw Exception("Something went searching the cache for notes.")
        }
        val results: ArrayList<Note> = ArrayList()
        for (note in notesData.values){
            if (note.title.contains(query)) {
                results.add(note)
            } else if(note.body.contains(query)) {
                results.add(note)
            }
            if (results.size > (page * NOTE_PAGINATION_PAGE_SIZE)){
                break
            }
        }
        return results
    }

    override suspend fun getAllNotes(): List<Note> {
        return ArrayList(notesData.values)
    }

    override suspend fun searchNoteById(id: String): Note? {
        return notesData.get(id)
    }

    override suspend fun getNumNotes(): Int {
        return notesData.size
    }

    override suspend fun insertNotes(notes: List<Note>): LongArray {
        val results = LongArray(notes.size)
        for ((index,note) in notes.withIndex()) {
            results[index] = 1
            notesData.put(note.id, note)
        }
        return results
    }

}