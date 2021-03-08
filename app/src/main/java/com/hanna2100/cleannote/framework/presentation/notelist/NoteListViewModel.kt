package com.hanna2100.cleannote.framework.presentation.notelist

import android.content.SharedPreferences
import com.hanna2100.cleannote.business.domain.model.NoteFactory
import com.hanna2100.cleannote.business.domain.state.StateEvent
import com.hanna2100.cleannote.business.interactors.notelist.NoteListInteractors
import com.hanna2100.cleannote.framework.presentation.common.BaseViewModel
import com.hanna2100.cleannote.framework.presentation.notelist.state.NoteListViewState

class NoteListViewModel
constructor(
    private val noteListInteractors: NoteListInteractors,
    private val noteFactory: NoteFactory,
    private val editor: SharedPreferences.Editor,
    private val sharedPreferences: SharedPreferences
): BaseViewModel<NoteListViewState>() {
    override fun handleNewData(data: NoteListViewState) {

    }

    override fun setStateEvent(stateEvent: StateEvent) {

    }

    override fun initNewViewState(): NoteListViewState {
        return NoteListViewState()
    }

}