package com.hanna2100.cleannote.framwork.presentation.notedetail.state

import android.os.Parcelable
import com.hanna2100.cleannote.business.domain.model.Note
import com.hanna2100.cleannote.business.domain.state.ViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NoteDetailViewState(
    var note: Note? = null,
    var isUpdatePending: Boolean? = null
): Parcelable, ViewState