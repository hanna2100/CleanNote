package com.hanna2100.cleannote.business.interactors.notelist

import com.hanna2100.cleannote.business.interactors.common.DeleteNote
import com.hanna2100.cleannote.framwork.presentation.notelist.state.NoteListViewState

class NoteListInteractors (
    val insertNewNote: InsertNewNote,
    val deleteNote: DeleteNote<NoteListViewState>,
    val searchNotes: SearchNotes,
    val getNumNotes: GetNumNotes,
    val restoreDeletedNote: RestoreDeletedNote,
    val deleteNoteMultipleNotes: DeleteMultipleNotes
) {

}