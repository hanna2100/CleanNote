package com.hanna2100.cleannote.framework.presentation.notedetail

import androidx.lifecycle.LiveData
import com.hanna2100.cleannote.business.domain.model.Note
import com.hanna2100.cleannote.business.domain.state.*
import com.hanna2100.cleannote.business.interactors.notedetail.NoteDetailInteractors
import com.hanna2100.cleannote.business.interactors.notedetail.UpdateNote.Companion.UPDATE_NOTE_FAILED
import com.hanna2100.cleannote.framework.datasource.cache.model.NoteEntity
import com.hanna2100.cleannote.framework.presentation.common.BaseViewModel
import com.hanna2100.cleannote.framework.presentation.notedetail.state.*
import com.hanna2100.cleannote.framework.presentation.notedetail.state.NoteDetailStateEvent.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

const val NOTE_DETAIL_ERROR_RETRIEVEING_SELECTED_NOTE = "Error retrieving selected note from bundle."
const val NOTE_DETAIL_SELECTED_NOTE_BUNDLE_KEY = "selectedNote"
const val NOTE_TITLE_CANNOT_BE_EMPTY = "Note title can not be empty."

@FlowPreview
@Singleton
class NoteDetailViewModel
@Inject
constructor(
    private val noteDetailInteractors: NoteDetailInteractors
): BaseViewModel<NoteDetailViewState>() {

    private val noteInteractionManager: NoteInteractionManager = NoteInteractionManager()

    val noteTitleInteractionState: LiveData<NoteInteractionState>
        get() = noteInteractionManager.noteTitleState

    val noteBodyInteractionState: LiveData<NoteInteractionState>
        get() = noteInteractionManager.noteBodyState

    val collapsingToolbarState: LiveData<CollapsingToolbarState>
        get() = noteInteractionManager.collapsingToolbarState

    override fun handleNewData(data: NoteDetailViewState) {

    }

    override fun setStateEvent(stateEvent: StateEvent) {
        val job: Flow<DataState<NoteDetailViewState>?> = when(stateEvent) {
            is UpdateNoteEvent -> {
                val pk = getNote()?.id
                if(!isNoteTitleNull() && pk != null) {
                    noteDetailInteractors.updateNote.updateNote(
                        note = getNote()!!,
                        stateEvent = stateEvent
                    )
                } else {
                    emitStateMessageEvent(
                        stateMessage = StateMessage(
                            response = Response(
                                message = UPDATE_NOTE_FAILED,
                                uiComponentType = UIComponentType.Dialog(),
                                messageType = MessageType.Error()
                            )
                        ),
                        stateEvent = stateEvent
                    )
                }
            }
            is DeleteNoteEvent -> {
                noteDetailInteractors.deleteNote.deleteNote(
                    note = stateEvent.note,
                    stateEvent = stateEvent
                )
            }
            is CreateStateMessageEvent -> {
                emitStateMessageEvent(
                    stateMessage = stateEvent.stateMessage,
                    stateEvent = stateEvent
                )
            }
            else -> {
                emitInvalidStateEvent(stateEvent)
            }
        }
        launchJob(stateEvent, job)
    }

    fun getNote(): Note? {
        return getCurrentViewStateOrNew().note
    }

    private fun isNoteTitleNull(): Boolean {
        val title = getNote()?.title
        if(title.isNullOrBlank()) {
            setStateEvent(
                CreateStateMessageEvent(
                    stateMessage = StateMessage(
                        response = Response(
                            message = NOTE_TITLE_CANNOT_BE_EMPTY,
                            uiComponentType = UIComponentType.Dialog(),
                            messageType = MessageType.Info()
                        )
                    )
                )
            )
            return true
        } else {
            return false
        }
    }

    fun beginPendingDelete(note: Note) {
        setStateEvent(
            DeleteNoteEvent(
                note = note
            )
        )
    }

    override fun initNewViewState(): NoteDetailViewState {
        return NoteDetailViewState()
    }

    fun setNote(note:Note?) {
        val update = getCurrentViewStateOrNew()
        update.note = note
        setViewState(update)
    }

    fun setCollapsingToolbarState(
        state: CollapsingToolbarState
    ) {
        return noteInteractionManager.setCollapsingToolbarState(state)
    }

    fun updateNote(title: String?, body: String?) {
        updateNoteTitle(title)
        updateNoteBody(body)
    }

    fun updateNoteBody(body: String?) {
        val update = getCurrentViewStateOrNew()
        val updatedNote = update.note?.copy(
                body = body?: ""
        )
        update.note = updatedNote
        setViewState(update)
    }

    fun updateNoteTitle(title: String?) {
        if(title == null) {
            setStateEvent(
                CreateStateMessageEvent(
                    stateMessage = StateMessage(
                        response = Response(
                            message = NoteEntity.nullTitleError(),
                            uiComponentType = UIComponentType.Dialog(),
                            messageType = MessageType.Error()
                        )
                    )
                )
            )
        }
        else {
            val update = getCurrentViewStateOrNew()
            val updatedNote = update.note?.copy(
                title = title
            )
            update.note = updatedNote
            setViewState(update)
        }
    }

    fun setNoteInteractionTitleState(state:NoteInteractionState) {
        noteInteractionManager.setNewNoteTitleState(state)
    }

    fun setNoteInteractionBodyState(state: NoteInteractionState) {
        noteInteractionManager.setNewNoteBodyState(state)
    }

    fun isToolbarCollapsed() = collapsingToolbarState.toString()
            .equals(CollapsingToolbarState.Collapsed().toString())

    fun setIsUpdatePending(isPending: Boolean) {
        val update = getCurrentViewStateOrNew()
        update.isUpdatePending = isPending
        setViewState(update)
    }

    fun getIsUpdatePending(): Boolean {
        return getCurrentViewStateOrNew().isUpdatePending?: false
    }

    fun isToolbarExpanded() = collapsingToolbarState.toString()
            .equals(CollapsingToolbarState.Expanded().toString())

    fun checkEditState() = noteInteractionManager.checkEditState()

    fun exitEditState() = noteInteractionManager.exitEditState()

    fun isEditingTitle() = noteInteractionManager.isEditingTitle()

    fun isEditingBody() = noteInteractionManager.isEditingBody()

    fun triggerNoteObservers(){
        getCurrentViewStateOrNew().note?.let { note ->
            setNote(note)
        }
    }

}