package com.hanna2100.cleannote.business.domain.state

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.android.parcel.IgnoredOnParcel

const val MESSAGE_STACK_BUNDLE_KEY = "com.hanna2100.cleannote.business.domain.state"

class MessageStack: ArrayList<StateMessage>() {

    @IgnoredOnParcel
    private val _stateMessage: MutableLiveData<StateMessage?> = MutableLiveData()

    @IgnoredOnParcel
    val stateMessage: LiveData<StateMessage?>
        get() = _stateMessage

    fun isStackEmpty(): Boolean {
        return size == 0
    }

    override fun addAll(elements: Collection<StateMessage>): Boolean {
        for (element in elements) {
            add(element)
        }
        return true
    }

    override fun add(element: StateMessage): Boolean {
        if (this.contains(element)) {
            return false
        }
        val transaction = super.add(element)
        if (this.size == 1) {
            setStateMessage(stateMessage = element)
        }
        return transaction
    }

    private fun setStateMessage(stateMessage: StateMessage?) {
        _stateMessage.value = stateMessage
    }
}