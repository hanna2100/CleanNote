package com.hanna2100.cleannote.business.data.cache.implementation

import com.hanna2100.cleannote.business.data.cache.abstraction.NoteCacheDataSource
import com.hanna2100.cleannote.business.domain.model.Note
import javax.inject.Inject

@SinceKotlin
class NoteCacheDataSourceImpl
@Inject
constructor(
        private val noteDaoService: NoteDaoService //TODO 만들어야 함
): NoteCacheDataSource {

    override suspend fun insertNote(note: Note): Long
            = noteDaoService.insertNote(note)

    override suspend fun deleteNote(primaryKey: String): Int
            = noteDaoService.deleteNote(primaryKey)

    override suspend fun deleteNotes(notes: List<Note>): Int
            = noteDaoService.deleteNotes(notes)

    override suspend fun updateNote(
            primary: String,
            newTitle: String,
            newBody: String
    ): Int
            = noteDaoService.updateNote(primary, newTitle, newBody)

    override suspend fun searchNote(
            query: String,
            filterAndOrder: String
            , page: Int
    ): List<Note>
            = noteDaoService.searchNote(query, filterAndOrder, page)

    override suspend fun searchNoteById(primaryKey: String): Note?
            = noteDaoService.searchNoteById(primaryKey)

    override suspend fun getNumNotes(): Int
            = noteDaoService.getNumNotes()

    override suspend fun insertNotes(notes: List<Note>): LongArray
            = noteDaoService.insertNotes(notes)

}