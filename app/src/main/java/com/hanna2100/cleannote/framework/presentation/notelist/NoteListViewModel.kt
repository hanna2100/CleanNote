package com.hanna2100.cleannote.framework.presentation.notelist

import android.content.SharedPreferences
import android.os.Parcelable
import com.hanna2100.cleannote.business.domain.model.Note
import com.hanna2100.cleannote.business.domain.model.NoteFactory
import com.hanna2100.cleannote.business.domain.state.DataState
import com.hanna2100.cleannote.business.domain.state.StateEvent
import com.hanna2100.cleannote.business.interactors.notelist.NoteListInteractors
import com.hanna2100.cleannote.framework.datasource.cache.database.NOTE_FILTER_DATE_CREATED
import com.hanna2100.cleannote.framework.datasource.cache.database.NOTE_ORDER_DESC
import com.hanna2100.cleannote.framework.datasource.preferences.PreferenceKeys.Companion.NOTE_FILTER
import com.hanna2100.cleannote.framework.datasource.preferences.PreferenceKeys.Companion.NOTE_ORDER
import com.hanna2100.cleannote.framework.presentation.common.BaseViewModel
import com.hanna2100.cleannote.framework.presentation.notelist.state.NoteListStateEvent.*
import com.hanna2100.cleannote.framework.presentation.notelist.state.NoteListViewState
import com.hanna2100.cleannote.util.printLogD
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow

const val DELETE_PENDING_ERROR = "There is already a pending delete operation."

@ExperimentalCoroutinesApi
@FlowPreview
class NoteListViewModel
constructor(
    private val noteListInteractors: NoteListInteractors,
    private val noteFactory: NoteFactory,
    private val editor: SharedPreferences.Editor,
    private val sharedPreferences: SharedPreferences
): BaseViewModel<NoteListViewState>() {

    init {
        setNoteFilter(
            sharedPreferences.getString(
                NOTE_FILTER,
                NOTE_FILTER_DATE_CREATED
            )
        )
        setNoteOrder(
            sharedPreferences.getString(
                NOTE_ORDER,
                NOTE_ORDER_DESC
            )
        )
    }

    override fun handleNewData(data: NoteListViewState) {
        data.let { viewState ->
            viewState.noteList?.let { noteList ->
                setNoteListData(noteList)
            }

            viewState.numNotesInCache?.let { numNotes ->
                setNumNotesInCache(numNotes)
            }

            viewState.newNote?.let { note ->
                setNote(note)
            }

            viewState.notePendingDelete?.let { restoredNote ->
                restoredNote.note?.let { note ->
                    setRestoredNoteId(note)
                }
                setNotePendingDelete(null)
            }
        }
    }

    // 노드 추가, 수정, 삭제 등 이벤트가 발생했을 때의 처리
    override fun setStateEvent(stateEvent: StateEvent) {

        val job: Flow<DataState<NoteListViewState>?> = when(stateEvent){

            is InsertNewNoteEvent -> {
                noteListInteractors.insertNewNote.insertNewNote(
                        title = stateEvent.title,
                        stateEvent = stateEvent
                )
            }

            is DeleteNoteEvent -> {
                noteListInteractors.deleteNote.deleteNote(
                        note = stateEvent.note,
                        stateEvent = stateEvent
                )
            }

            is DeleteMultipleNotesEvent -> {
                noteListInteractors.deleteMultipleNotes.deleteNotes(
                        notes = stateEvent.notes,
                        stateEvent = stateEvent
                )
            }

            is RestoreDeletedNoteEvent -> {
                noteListInteractors.restoreDeletedNote.restoreDeletedNote(
                        note = stateEvent.note,
                        stateEvent = stateEvent
                )
            }

            is SearchNotesEvent -> {
                if(stateEvent.clearLayoutManagerState){
                    clearLayoutManagerState()
                }
                noteListInteractors.searchNotes.searchNotes(
                        query = getSearchQuery(),
                        filterAndOrder = getOrder() + getFilter(),
                        page = getPage(),
                        stateEvent = stateEvent
                )
            }

            is GetNumNotesInCacheEvent -> {
                noteListInteractors.getNumNotes.getNumNotes(
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

    override fun initNewViewState(): NoteListViewState {
        return NoteListViewState()
    }

    fun getFilter(): String {
        return getCurrentViewStateOrNew().filter
                ?: NOTE_FILTER_DATE_CREATED
    }

    fun getOrder(): String {
        return getCurrentViewStateOrNew().order
                ?: NOTE_ORDER_DESC
    }

    fun getSearchQuery(): String {
        return getCurrentViewStateOrNew().searchQuery
                ?: return ""
    }

    private fun getPage(): Int{
        return getCurrentViewStateOrNew().page
                ?: return 1
    }

    fun getNoteListSize() = getCurrentViewStateOrNew().noteList?.size?: 0

    private fun getNumNotesInCache() = getCurrentViewStateOrNew().numNotesInCache?: 0

    // for debugging
    fun getActiveJobs() = dataChannelManager.getActiveJobs()

    fun getLayoutManagerState(): Parcelable? {
        return getCurrentViewStateOrNew().layoutManagerState
    }

    private fun findListPositionOfNote(note: Note?): Int {
        val viewState = getCurrentViewStateOrNew()
        viewState.noteList?.let { noteList ->
            for((index, item) in noteList.withIndex()){
                if(item.id == note?.id){
                    return index
                }
            }
        }
        return 0
    }

    fun isPaginationExhausted() = getNoteListSize() >= getNumNotesInCache()

    fun isQueryExhausted(): Boolean{
        printLogD(this.javaClass,
                "is query exhasuted? ${getCurrentViewStateOrNew().isQueryExhausted?: true}")
        return getCurrentViewStateOrNew().isQueryExhausted?: true
    }
}