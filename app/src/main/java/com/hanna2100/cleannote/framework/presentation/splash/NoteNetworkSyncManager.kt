package com.hanna2100.cleannote.framework.presentation.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hanna2100.cleannote.business.interactors.splash.SyncDeletedNotes
import com.hanna2100.cleannote.business.interactors.splash.SyncNotes
import com.hanna2100.cleannote.util.printLogD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteNetworkSyncManager
@Inject
constructor(
    private val syncNotes: SyncNotes,
    private val syncDeletedNotes: SyncDeletedNotes
) {
    private val _hasSyncBeenExecuted: MutableLiveData<Boolean> = MutableLiveData(false)

    val hasSyncBeenExecuted: LiveData<Boolean>
        get() = _hasSyncBeenExecuted

    fun executeDataSync(coroutineScope: CoroutineScope) {
        if(_hasSyncBeenExecuted.value!!) {
            return
        }

        val syncJob = coroutineScope.launch {
            launch {
                printLogD(this.javaClass, "삭제한 노트 싱크 맞추기")
                syncDeletedNotes.syncDeletedNotes()
            }.join()

            launch {
                printLogD(this.javaClass, "노트 싱크 맞추기")
                syncNotes.syncNotes()
            }
        }
        syncJob.invokeOnCompletion {
            CoroutineScope(Main).launch {
                _hasSyncBeenExecuted.value = true
            }
        }
    }
}