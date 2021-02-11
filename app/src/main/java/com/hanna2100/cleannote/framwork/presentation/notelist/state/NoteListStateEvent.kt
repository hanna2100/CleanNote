package com.hanna2100.cleannote.framwork.presentation.notelist.state

import com.hanna2100.cleannote.business.domain.model.Note
import com.hanna2100.cleannote.business.domain.state.StateEvent
import com.hanna2100.cleannote.business.domain.state.StateMessage

sealed class NoteListStateEvent: StateEvent {

    class InsertNewNoteEvent(
        val title: String
    ): NoteListStateEvent() {
        override fun errorInfo(): String {
            return "새로운 노트 생성 실패."
        }

        override fun eventName(): String {
            return this.javaClass::getSimpleName.name
        }

        override fun shouldDisplayProgressBar(): Boolean = true

    }

    // 테스트용
    class InsertMultipleNotesEvent(
        val numNotes: Int
    ): NoteListStateEvent() {
        override fun errorInfo(): String {
            return "여러개의 노트 생성 실패."
        }

        override fun eventName(): String {
            return this.javaClass::getSimpleName.name
        }

        override fun shouldDisplayProgressBar(): Boolean = true

    }

    class DeleteNoteEvent(
        val note: Note
    ): NoteListStateEvent() {
        override fun errorInfo(): String {
            return "노트 삭제 실패."
        }

        override fun eventName(): String {
            return this.javaClass::getSimpleName.name
        }

        override fun shouldDisplayProgressBar(): Boolean = true

    }

    class DeleteMultipleNotesEvent(
        val notes: ArrayList<Note>
    ): NoteListStateEvent() {
        override fun errorInfo(): String {
            return "여러개의 노트 삭제 실패."
        }

        override fun eventName(): String {
            return this.javaClass::getSimpleName.name
        }

        override fun shouldDisplayProgressBar(): Boolean = true

    }

    class RestoreDeletedNoteEvent(
        val note: Note
    ): NoteListStateEvent() {
        override fun errorInfo(): String {
            return "삭제된 노트 되돌리기 실패."
        }

        override fun eventName(): String {
            return this.javaClass::getSimpleName.name
        }

        override fun shouldDisplayProgressBar(): Boolean = false

    }

    class SearchNotesEvent(
        val clearLayoutManagerState: Boolean = true
    ): NoteListStateEvent() {
        override fun errorInfo(): String {
            return "노트 리스트 가져오기 실패."
        }

        override fun eventName(): String {
            return this.javaClass::getSimpleName.name
        }

        override fun shouldDisplayProgressBar(): Boolean = true

    }

    class GetNumNotesInCacheEvent: NoteListStateEvent() {
        override fun errorInfo(): String {
            return "캐시에 있는 노트 갯수 가져오기 실패."
        }

        override fun eventName(): String {
            return this.javaClass::getSimpleName.name
        }

        override fun shouldDisplayProgressBar(): Boolean = true

    }

    class CreateStateMessageEvent(
        val stateMessage: StateMessage
    ): NoteListStateEvent() {
        override fun errorInfo(): String {
            return "새로운 상태메세지 생성 실패."
        }

        override fun eventName(): String {
            return this.javaClass::getSimpleName.name
        }

        override fun shouldDisplayProgressBar(): Boolean = false

    }

}