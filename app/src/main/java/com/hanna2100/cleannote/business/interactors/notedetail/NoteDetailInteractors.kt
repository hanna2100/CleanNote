package com.hanna2100.cleannote.business.interactors.notedetail

import com.hanna2100.cleannote.business.interactors.common.DeleteNote
import com.hanna2100.cleannote.framework.presentation.notedetail.state.NoteDetailViewState

class NoteDetailInteractors (
    val deleteNote: DeleteNote<NoteDetailViewState>,
    val updateNote: UpdateNote
)