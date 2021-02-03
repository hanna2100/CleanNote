package com.hanna2100.cleannote.business.data.network.abstraction

import com.hanna2100.cleannote.business.domain.model.Note

interface NoteNetworkDataSource {
    suspend fun insertOrUpdateNote(note: Note)
    suspend fun deleteNote(primaryKey: String)
    suspend fun insertDeletedNote(note: Note)
    suspend fun insertDeletedNotes(notes: List<Note>)
    suspend fun deleteDeletedNote(note: Note)
    suspend fun getDeletedNote(): List<Note>
    suspend fun deleteAllNotes()
    suspend fun searchNote(note: Note): Note?
    suspend fun getAllNotes(): List<Note>
    suspend fun insertOrUpdateNotes(notes: List<Note>)
}