package com.hanna2100.cleannote.di

import androidx.lifecycle.ViewModelProvider
import com.hanna2100.cleannote.business.domain.util.DateUtil
import com.hanna2100.cleannote.framework.presentation.common.NoteFragmentFactory
import com.hanna2100.cleannote.framework.presentation.common.NoteViewModelFactory
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@FlowPreview
@ExperimentalCoroutinesApi
@Module
object NoteFragmentFactoryModule  {

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteFragmentFactory(
        viewModelFactory: ViewModelProvider.Factory,
        dateUtil: DateUtil
    ): NoteFragmentFactory {
        return NoteFragmentFactory(
            viewModelFactory, dateUtil
        )
    }
}