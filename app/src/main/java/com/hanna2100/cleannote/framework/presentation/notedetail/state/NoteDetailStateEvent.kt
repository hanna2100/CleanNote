package com.hanna2100.cleannote.framework.presentation.notedetail.state

import com.hanna2100.cleannote.business.domain.state.StateEvent

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

    class DeleteNoteEvent: NoteDetailStateEvent() {
        override fun errorInfo(): String {
            return "Error deleting note"
        }

        override fun eventName(): String {
            return "DeleteNoteEvent"
        }

        override fun shouldDisplayProgressBar(): Boolean = true
    }

    class CreateStateMessageEvent: NoteDetailStateEvent() {
        override fun errorInfo(): String {
            return "Error creating a new state message."
        }

        override fun eventName(): String {
            return "CreateStateMessageEvent"
        }

        override fun shouldDisplayProgressBar(): Boolean = false
    }
}