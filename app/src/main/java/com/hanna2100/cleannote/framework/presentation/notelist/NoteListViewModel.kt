package com.hanna2100.cleannote.framework.presentation.notelist

import android.content.SharedPreferences
import android.os.Parcelable
import com.hanna2100.cleannote.business.domain.model.Note
import com.hanna2100.cleannote.business.domain.model.NoteFactory
import com.hanna2100.cleannote.business.domain.state.*
import com.hanna2100.cleannote.business.interactors.notelist.NoteListInteractors
import com.hanna2100.cleannote.framework.datasource.cache.database.NOTE_FILTER_DATE_CREATED
import com.hanna2100.cleannote.framework.datasource.cache.database.NOTE_ORDER_DESC
import com.hanna2100.cleannote.framework.datasource.preferences.PreferenceKeys.Companion.NOTE_FILTER
import com.hanna2100.cleannote.framework.datasource.preferences.PreferenceKeys.Companion.NOTE_ORDER
import com.hanna2100.cleannote.framework.presentation.common.BaseViewModel
import com.hanna2100.cleannote.framework.presentation.notedetail.state.NoteDetailStateEvent
import com.hanna2100.cleannote.framework.presentation.notelist.state.NoteListStateEvent
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

        val job: Flow<DataState<NoteListViewState>?> = when (stateEvent) {

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
                if (stateEvent.clearLayoutManagerState) {
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

    private fun getPage(): Int {
        return getCurrentViewStateOrNew().page
                ?: return 1
    }

    fun getNoteListSize() = getCurrentViewStateOrNew().noteList?.size ?: 0

    private fun getNumNotesInCache() = getCurrentViewStateOrNew().numNotesInCache ?: 0

    // 디버깅용
    fun getActiveJobs() = dataChannelManager.getActiveJobs()

    fun getLayoutManagerState(): Parcelable? {
        return getCurrentViewStateOrNew().layoutManagerState
    }

    private fun findListPositionOfNote(note: Note?): Int {
        val viewState = getCurrentViewStateOrNew()
        viewState.noteList?.let { noteList ->
            for ((index, item) in noteList.withIndex()) {
                if (item.id == note?.id) {
                    return index
                }
            }
        }
        return 0
    }

    fun isPaginationExhausted() = getNoteListSize() >= getNumNotesInCache()

    fun isQueryExhausted(): Boolean {
        printLogD(this.javaClass,
                "is query exhasuted? ${getCurrentViewStateOrNew().isQueryExhausted ?: true}")
        return getCurrentViewStateOrNew().isQueryExhausted ?: true
    }

    private fun setNoteListData(notesList: ArrayList<Note>) {
        val update = getCurrentViewStateOrNew()
        update.noteList = notesList
        setViewState(update)
    }

    fun setQueryExhausted(isExhausted: Boolean) {
        val update = getCurrentViewStateOrNew()
        update.isQueryExhausted = isExhausted
        setViewState(update)
    }

    // 리사이클러뷰 혹은 다이얼로그에서 생성가능.
    fun setNote(note: Note?) {
        val update = getCurrentViewStateOrNew()
        update.newNote = note
        setViewState(update)
    }

    fun setQuery(query: String?) {
        val update = getCurrentViewStateOrNew()
        update.searchQuery = query
        setViewState(update)
    }


    // 노트가 삭제되었다가 복구되면 id를 다시설정해야함
    private fun setRestoredNoteId(restoredNote: Note) {
        val update = getCurrentViewStateOrNew()
        update.noteList?.let { noteList ->
            for ((index, note) in noteList.withIndex()) {
                if (note.title.equals(restoredNote.title)) {
                    noteList.remove(note)
                    noteList.add(index, restoredNote)
                    update.noteList = noteList
                    break
                }
            }
        }
        setViewState(update)
    }

    private fun removePendingNoteFromList(note: Note?) {
        val update = getCurrentViewStateOrNew()
        val list = update.noteList
        if (list?.contains(note) == true) {
            list.remove(note)
            update.noteList = list
            setViewState(update)
        }
    }

    fun setNotePendingDelete(note: Note?) {
        val update = getCurrentViewStateOrNew()
        if (note != null) {
            update.notePendingDelete = NoteListViewState.NotePendingDelete(
                    note = note,
                    listPosition = findListPositionOfNote(note)
            )
        } else {
            update.notePendingDelete = null
        }
        setViewState(update)
    }

    private fun setNumNotesInCache(numNotes: Int) {
        val update = getCurrentViewStateOrNew()
        update.numNotesInCache = numNotes
        setViewState(update)
    }

    fun createNewNote(
            id: String? = null,
            title: String,
            body: String? = null
    ) = noteFactory.createSingleNote(id, title, body)

    private fun resetPage() {
        val update = getCurrentViewStateOrNew()
        update.page = 1
        setViewState(update)
    }

    fun clearList() {
        printLogD(this.javaClass, "clearList")
        val update = getCurrentViewStateOrNew()
        update.noteList = ArrayList()
        setViewState(update)
    }

    // 테스트를 하기위한 코드
    fun clearSearchQuery() {
        setQuery("")
        clearList()
        loadFirstPage()
    }

    private fun incrementPageNumber() {
        val update = getCurrentViewStateOrNew()
        val page = update.copy().page ?: 1
        update.page = page.plus(1)
        setViewState(update)
    }

    fun setLayoutManagerState(layoutManagerState: Parcelable) {
        val update = getCurrentViewStateOrNew()
        update.layoutManagerState = layoutManagerState
        setViewState(update)
    }

    fun clearLayoutManagerState() {
        val update = getCurrentViewStateOrNew()
        update.layoutManagerState = null
        setViewState(update)
    }

    fun setNoteFilter(filter: String?) {
        filter?.let {
            val update = getCurrentViewStateOrNew()
            update.filter = filter
            setViewState(update)
        }
    }

    fun setNoteOrder(order: String?) {
        val update = getCurrentViewStateOrNew()
        update.order = order
        setViewState(update)
    }

    fun saveFilterOptions(filter: String, order: String) {
        editor.putString(NOTE_FILTER, filter)
        editor.apply()

        editor.putString(NOTE_ORDER, order)
        editor.apply()
    }

    fun isDeletePending(): Boolean {
        val pendingNote = getCurrentViewStateOrNew().notePendingDelete
        if (pendingNote != null) {
            setStateEvent(
                NoteDetailStateEvent.CreateStateMessageEvent(
                    stateMessageEvent = StateMessage(
                        response = Response(
                            message = DELETE_PENDING_ERROR,
                            uiComponentType = UIComponentType.Toast(),
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

    fun undoDelete() {
        // viewState 교체
        val update = getCurrentViewStateOrNew()
        update.notePendingDelete?.let { note ->
            if (note.listPosition != null && note.note != null) {
                update.noteList?.add(
                    note.listPosition as Int,
                    note.note as Note
                )
                setStateEvent(RestoreDeletedNoteEvent(note.note as Note))
            }
        }
        setViewState(update)
    }

    fun beginPendingDelete(note: Note) {
        setNotePendingDelete(note)
        removePendingNoteFromList(note)
        setStateEvent(
            DeleteNoteEvent(
                note = note
            )
        )
    }

    fun loadFirstPage() {
        setQueryExhausted(false)
        resetPage()
        setStateEvent(SearchNotesEvent())
        printLogD(this.javaClass,
                "loadFirstPage: ${getCurrentViewStateOrNew().searchQuery}")
    }

    fun nextPage() {
        if (!isQueryExhausted()) {
            printLogD(this.javaClass, "attempting to load next page...")
            clearLayoutManagerState()
            incrementPageNumber()
            setStateEvent(SearchNotesEvent())
        }
    }

    fun retrieveNumNotesInCache() {
        setStateEvent(GetNumNotesInCacheEvent())
    }

    fun refreshSearchQuery() {
        setQueryExhausted(false)
        setStateEvent(SearchNotesEvent(false))
    }

}