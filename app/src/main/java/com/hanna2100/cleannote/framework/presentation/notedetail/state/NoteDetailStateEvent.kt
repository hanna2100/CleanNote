package com.hanna2100.cleannote.framework.presentation.notedetail.state

import com.hanna2100.cleannote.business.domain.model.Note
import com.hanna2100.cleannote.business.domain.state.StateEvent
import com.hanna2100.cleannote.business.domain.state.StateMessage

sealed class NoteDetailStateEvent : StateEvent {

    class UpdateNoteEvent: NoteDetailStateEvent() {
        override fun errorInfo(): String {
            return "Error updating note."
        }

        override fun eventName(): String {
            return "UpdateNoteEvent"
        }

        override fun shouldDisplayProgressBar(): Boolean = true
    }

    class DeleteNoteEvent(
        val note: Note
    ): NoteDetailStateEvent() {
        override fun errorInfo(): String {
            return "Error deleting note"
        }

        override fun eventName(): String {
            return "DeleteNoteEvent"
        }

        override fun shouldDisplayProgressBar(): Boolean = true
    }

    class CreateStateMessageEvent(
        val stateMessageEvent: StateMessage
    ): NoteDetailStateEvent() {
        override fun errorInfo(): String {
            return "Error creating a new state message."
        }

        override fun eventName(): String {
            return "CreateStateMessageEvent"
        }

        override fun shouldDisplayProgressBar(): Boolean = false
    }
}