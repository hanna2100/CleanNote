package com.hanna2100.cleannote.di

import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.hanna2100.cleannote.framwork.datasource.cache.database.NoteDatabase
import com.hanna2100.cleannote.framwork.presentation.BaseApplication
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@FlowPreview
@Module
object ProductionModule  {

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteDb(app: BaseApplication) : NoteDatabase {
        return Room
                .databaseBuilder(app, NoteDatabase::class.java, NoteDatabase.DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
}