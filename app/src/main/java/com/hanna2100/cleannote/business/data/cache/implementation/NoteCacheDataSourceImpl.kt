package com.hanna2100.cleannote.business.data.cache.implementation

import com.hanna2100.cleannote.business.data.cache.abstraction.NoteCacheDataSource
import com.hanna2100.cleannote.business.domain.model.Note
import com.hanna2100.cleannote.framework.datasource.cache.abstraction.NoteDaoService
import java.sql.Timestamp
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteCacheDataSourceImpl
@Inject
constructor(
        private val noteDaoService: NoteDaoService
): NoteCacheDataSource {

    override suspend fun insertNote(note: Note): Long
            = noteDaoService.insertNote(note)

    override suspend fun deleteNote(primaryKey: String): Int
            = noteDaoService.deleteNote(primaryKey)

    override suspend fun deleteNotes(notes: List<Note>): Int
            = noteDaoService.deleteNotes(notes)

    override suspend fun updateNote(
        primaryKey: String,
        newTitle: String,
        newBody: String,
        timestamp: String?
    ): Int
            = noteDaoService.updateNote(primaryKey, newTitle, newBody, timestamp)

    override suspend fun searchNotes(
            query: String,
            filterAndOrder: String
            , page: Int
    ): List<Note> {
        return noteDaoService.returnOrderedQuery(
                query, filterAndOrder, page
        )
    }

    override suspend fun getAllNotes(): List<Note> {
        return noteDaoService.getAllNotes()
    }

    override suspend fun searchNoteById(primaryKey: String): Note?
            = noteDaoService.searchNoteById(primaryKey)

    override suspend fun getNumNotes(): Int
            = noteDaoService.getNumNotes()

    override suspend fun insertNotes(notes: List<Note>): LongArray
            = noteDaoService.insertNotes(notes)

}