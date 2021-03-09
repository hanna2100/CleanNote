package com.hanna2100.cleannote.framework.presentation.notelist

import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.hanna2100.cleannote.R
import com.hanna2100.cleannote.business.domain.util.DateUtil
import com.hanna2100.cleannote.framework.presentation.common.BaseNoteFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class NoteListFragment
constructor(
        private val factory: ViewModelProvider.Factory,
        private val dateUtil: DateUtil
): BaseNoteFragment(R.layout.fragment_note_list){

    val viewModel: NoteListViewModel by viewModels {
        factory
    }

    override fun inject() {
        TODO("Not yet implemented")
    }

}