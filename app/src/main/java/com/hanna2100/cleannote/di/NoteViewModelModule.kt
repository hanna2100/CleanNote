package com.hanna2100.cleannote.di

import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import com.hanna2100.cleannote.business.domain.model.NoteFactory
import com.hanna2100.cleannote.business.interactors.notedetail.NoteDetailInteractors
import com.hanna2100.cleannote.business.interactors.notelist.NoteListInteractors
import com.hanna2100.cleannote.framework.presentation.common.NoteViewModelFactory
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@FlowPreview
@Module
object NoteViewModelModule {

    @Singleton
    @JvmStatic
    @Provides
    fun provideNoteViewModelFactory(
        noteListInteractors: NoteListInteractors,
        noteDetailInteractors: NoteDetailInteractors,
        noteFactory: NoteFactory,
        editor: SharedPreferences.Editor,
        sharedPreferences: SharedPreferences
    ): ViewModelProvider.Factory {
        return NoteViewModelFactory(
            noteListInteractors = noteListInteractors,
            noteDetailInteractors = noteDetailInteractors,
            noteFactory = noteFactory,
            editor = editor,
            sharedPreferences = sharedPreferences
        )
    }
}