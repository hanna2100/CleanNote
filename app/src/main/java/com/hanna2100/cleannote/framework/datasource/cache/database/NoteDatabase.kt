package com.hanna2100.cleannote.framework.datasource.cache.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hanna2100.cleannote.framework.datasource.cache.model.NoteEntity

@Database(entities = [NoteEntity::class], version = 1)
abstract class NoteDatabase: RoomDatabase() {
    abstract fun noteDao(): NoteDao
    companion object {
        const val DATABASE_NAME: String = "note_db"
    }
} 