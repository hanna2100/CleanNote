package com.hanna2100.cleannote.framwork.datasource.cache.util

import com.hanna2100.cleannote.business.domain.model.Note
import com.hanna2100.cleannote.business.domain.util.DateUtil
import com.hanna2100.cleannote.business.domain.util.EntityMapper
import com.hanna2100.cleannote.framwork.datasource.cache.model.NoteEntity
import javax.inject.Inject

class CacheMapper
@Inject
constructor(
    private val dateUtil: DateUtil
): EntityMapper<NoteEntity, Note> {

    fun entityListToNoteList(entities: List<NoteEntity>): List<Note> {
        val list = ArrayList<Note>()
        for(entity in entities) {
            list.add(mapFromEntity(entity))
        }
        return list
    }

    fun noteListToEntityList(notes: List<Note>): List<NoteEntity> {
        val list = ArrayList<NoteEntity>()
        for(note in notes) {
            list.add(mapToEntity(note))
        }
        return list
    }

    override fun mapFromEntity(entity: NoteEntity): Note {
        return Note(
            id = entity.id,
            title = entity.title,
            body = entity.body,
            created_at = entity.created_at,
            updated_at = entity.updated_at
        )
    }

    override fun mapToEntity(domainModel: Note): NoteEntity {
        return NoteEntity(
            id = domainModel.id,
            title = domainModel.title,
            body = domainModel.body,
            created_at = domainModel.created_at,
            updated_at = domainModel.updated_at
        )
    }

}