package com.hanna2100.cleannote.framework.presentation.notedetail

import com.hanna2100.cleannote.business.domain.state.StateEvent
import com.hanna2100.cleannote.business.interactors.notedetail.NoteDetailInteractors
import com.hanna2100.cleannote.framework.presentation.common.BaseViewModel
import com.hanna2100.cleannote.framework.presentation.notedetail.state.NoteDetailViewState
import javax.inject.Inject

class NoteDetailViewModel
@Inject
constructor(
    private val noteDetailInteractors: NoteDetailInteractors
): BaseViewModel<NoteDetailViewState>() {
    override fun handleNewData(data: NoteDetailViewState) {

    }

    override fun setStateEvent(stateEvent: StateEvent) {

    }

    override fun initNewViewState(): NoteDetailViewState {
        return NoteDetailViewState()
    }

}