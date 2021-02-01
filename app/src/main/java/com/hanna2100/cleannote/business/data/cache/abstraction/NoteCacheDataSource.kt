package com.hanna2100.cleannote.business.data.cache.abstraction

import com.hanna2100.cleannote.business.domain.model.Note

interface NoteCacheDataSource {
    suspend fun insertNote(note: Note): Long
    suspend fun deleteNote(primary: String): Int
    suspend fun deleteNotes(note: List<Note>): Int
    suspend fun updateNote(primary: String, newTitle: String, newBody: String): Int
    suspend fun searchNote(
            query: String,
            filterAndOrder: String,
            page: Int
    ): List<Note>
    suspend fun searchNoteById(primaryKey: String): Note?
    suspend fun getNumNotes(): Int
    suspend fun insertNotes(notes: List<Note>): LongArray
}