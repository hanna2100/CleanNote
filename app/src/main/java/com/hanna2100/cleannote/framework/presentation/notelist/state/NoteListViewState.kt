package com.hanna2100.cleannote.framework.presentation.notelist.state

import android.os.Parcelable
import com.hanna2100.cleannote.business.domain.model.Note
import com.hanna2100.cleannote.business.domain.state.ViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NoteListViewState(
    var noteList: ArrayList<Note>? = null,
    var newNote: Note? = null, // + 버튼을 눌러 새로 작성될 노트
    var notePendingDelete: NotePendingDelete? = null, // 스와이프로 삭제하려는 노트(undo)
    var searchQuery: String? = null,
    var page: Int? = null,
    var isQueryExhausted: Boolean? = null,
    var filter: String? = null,
    var order: String? = null,
    var layoutManagerState: Parcelable? = null,
    var numNotesInCache: Int? = null
): Parcelable, ViewState {

    @Parcelize
    data class NotePendingDelete(
        var note: Note? = null,
        var listPosition: Int? = null
    ): Parcelable
}