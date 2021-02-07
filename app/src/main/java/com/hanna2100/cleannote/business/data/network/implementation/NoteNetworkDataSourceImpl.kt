package com.hanna2100.cleannote.business.data.network.implementation

import com.hanna2100.cleannote.business.data.network.abstraction.NoteNetworkDataSource
import com.hanna2100.cleannote.business.domain.model.Note
import com.hanna2100.cleannote.framwork.datasource.network.abstraction.NoteFirestoreService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteNetworkDataSourceImpl
@Inject
constructor(
        private val firestoreService: NoteFirestoreService
): NoteNetworkDataSource{
    override suspend fun insertOrUpdateNote(note: Note)
            = firestoreService.insertOrUpdateNote(note)

    override suspend fun deleteNote(primaryKey: String)
            = firestoreService.deleteNote(primaryKey)

    override suspend fun insertDeletedNote(note: Note)
            = firestoreService.insertDeletedNote(note)

    override suspend fun insertDeletedNotes(notes: List<Note>)
            = firestoreService.insertDeletedNotes(notes)

    override suspend fun deleteDeletedNote(note: Note)
            = firestoreService.deleteDeletedNote(note)

    override suspend fun getDeletedNotes(): List<Note>
            = firestoreService.getDeletedNotes()

    override suspend fun deleteAllNotes()
            = firestoreService.deleteAllNotes()

    override suspend fun searchNote(note: Note): Note?
            = firestoreService.searchNote(note)

    override suspend fun getAllNotes(): List<Note>
            = firestoreService.getAllNotes()

    override suspend fun insertOrUpdateNotes(notes: List<Note>)
            = firestoreService.insertOrUpdateNotes(notes)
}