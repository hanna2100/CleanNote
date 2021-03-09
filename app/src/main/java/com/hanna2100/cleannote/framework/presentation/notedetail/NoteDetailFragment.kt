package com.hanna2100.cleannote.framework.presentation.notedetail

import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.hanna2100.cleannote.R
import com.hanna2100.cleannote.framework.presentation.common.BaseNoteFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class NoteDetailFragment
constructor(
    private val factory: ViewModelProvider.Factory
): BaseNoteFragment(R.layout.fragment_note_detail) {

    val viewModel: NoteDetailViewModel by viewModels {
        factory
    }

    override fun inject() {
        TODO("Not yet implemented")
    }

}
