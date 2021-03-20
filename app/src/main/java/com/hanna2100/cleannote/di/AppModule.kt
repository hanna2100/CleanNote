package com.hanna2100.cleannote.di

import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.hanna2100.cleannote.business.data.cache.abstraction.NoteCacheDataSource
import com.hanna2100.cleannote.business.data.cache.implementation.NoteCacheDataSourceImpl
import com.hanna2100.cleannote.business.data.network.abstraction.NoteNetworkDataSource
import com.hanna2100.cleannote.business.data.network.implementation.NoteNetworkDataSourceImpl
import com.hanna2100.cleannote.business.domain.model.NoteFactory
import com.hanna2100.cleannote.business.domain.util.DateUtil
import com.hanna2100.cleannote.business.interactors.common.DeleteNote
import com.hanna2100.cleannote.business.interactors.notedetail.NoteDetailInteractors
import com.hanna2100.cleannote.business.interactors.notedetail.UpdateNote
import com.hanna2100.cleannote.business.interactors.notelist.*
import com.hanna2100.cleannote.business.interactors.splash.SyncDeletedNotes
import com.hanna2100.cleannote.business.interactors.splash.SyncNotes
import com.hanna2100.cleannote.framework.datasource.cache.abstraction.NoteDaoService
import com.hanna2100.cleannote.framework.datasource.cache.database.NoteDao
import com.hanna2100.cleannote.framework.datasource.cache.database.NoteDatabase
import com.hanna2100.cleannote.framework.datasource.cache.implementation.NoteDaoServiceImpl
import com.hanna2100.cleannote.framework.datasource.cache.util.CacheMapper
import com.hanna2100.cleannote.framework.datasource.network.abstraction.NoteFirestoreService
import com.hanna2100.cleannote.framework.datasource.network.implementation.NoteFirestoreServiceImpl
import com.hanna2100.cleannote.framework.datasource.network.mappers.NetworkMapper
import com.hanna2100.cleannote.framework.presentation.splash.NoteNetworkSyncManager
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@FlowPreview
@Module
object AppModule  {

    @JvmStatic
    @Singleton
    @Provides
    fun provideDateFormat(): SimpleDateFormat {
        val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)
        sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        return sdf
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideDateUtil(dateFormat: SimpleDateFormat): DateUtil {
        return DateUtil(
                dateFormat
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideSharedPrefsEditor(
            sharedPreferences: SharedPreferences
    ): SharedPreferences.Editor {
        return sharedPreferences.edit()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteFactory(dateUtil: DateUtil): NoteFactory {
        return NoteFactory(
            dateUtil
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteDAO(noteDatabase: NoteDatabase): NoteDao {
        return noteDatabase.noteDao()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteCacheMapper(dateUtil: DateUtil): CacheMapper {
        return CacheMapper(dateUtil)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteNetworkMapper(dateUtil: DateUtil): NetworkMapper {
        return NetworkMapper(dateUtil)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteDaoService(
            noteDao: NoteDao,
            noteEntityMapper: CacheMapper,
            dateUtil: DateUtil
    ): NoteDaoService {
        return NoteDaoServiceImpl(noteDao, noteEntityMapper, dateUtil)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteCacheDataSource(
            noteDaoService: NoteDaoService
    ): NoteCacheDataSource {
        return NoteCacheDataSourceImpl(noteDaoService)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideFirestoreService(
            firebaseAuth: FirebaseAuth,
            firebaseFirestore: FirebaseFirestore,
            networkMapper: NetworkMapper
    ): NoteFirestoreService {
        return NoteFirestoreServiceImpl(
                firebaseAuth,
                firebaseFirestore,
                networkMapper
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteNetworkDataSource(
            firestoreService: NoteFirestoreServiceImpl
    ): NoteNetworkDataSource {
        return NoteNetworkDataSourceImpl(
                firestoreService
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideSyncNotes(
            noteCacheDataSource: NoteCacheDataSource,
            noteNetworkDataSource: NoteNetworkDataSource
    ): SyncNotes {
        return SyncNotes(
                noteCacheDataSource,
                noteNetworkDataSource
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideSyncDeletedNotes(
            noteCacheDataSource: NoteCacheDataSource,
            noteNetworkDataSource: NoteNetworkDataSource
    ): SyncDeletedNotes {
        return SyncDeletedNotes(
                noteCacheDataSource,
                noteNetworkDataSource
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteDetailInteractors(
            noteCacheDataSource: NoteCacheDataSource,
            noteNetworkDataSource: NoteNetworkDataSource
    ): NoteDetailInteractors {
        return NoteDetailInteractors(
                DeleteNote(noteCacheDataSource, noteNetworkDataSource),
                UpdateNote(noteCacheDataSource, noteNetworkDataSource)
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteListInteractors(
            noteCacheDataSource: NoteCacheDataSource,
            noteNetworkDataSource: NoteNetworkDataSource,
            noteFactory: NoteFactory
    ): NoteListInteractors {
        return NoteListInteractors(
                InsertNewNote(noteCacheDataSource, noteNetworkDataSource, noteFactory),
                DeleteNote(noteCacheDataSource, noteNetworkDataSource),
                SearchNotes(noteCacheDataSource),
                GetNumNotes(noteCacheDataSource),
                RestoreDeletedNote(noteCacheDataSource, noteNetworkDataSource),
                DeleteMultipleNotes(noteCacheDataSource, noteNetworkDataSource)
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteNetworkSyncManager(
            syncNotes: SyncNotes,
            syncDeletedNotes: SyncDeletedNotes
    ): NoteNetworkSyncManager {
        return NoteNetworkSyncManager(syncNotes, syncDeletedNotes)
    }

}