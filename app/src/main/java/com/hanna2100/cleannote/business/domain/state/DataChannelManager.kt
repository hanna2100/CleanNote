package com.hanna2100.cleannote.business.domain.state

import com.hanna2100.cleannote.util.printLogD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

abstract class DataChannelManager<ViewState> {

    private val dataChannel = BroadcastChannel<DataState<ViewState>>(Channel.BUFFERED)
    private var channelScope: CoroutineScope? = null
    private val stateEventManager: StateEventManager = StateEventManager()

    val messageStack = MessageStack()
    val shouldDisplayProgressBar = stateEventManager.shouldDisplayProgressBar

    fun setupChannel() {
        cancelJobs()
        initChannel()
    }

    private fun initChannel() {
        dataChannel
            .asFlow()
            .onEach { dataState ->
                withContext(Main) {
                    dataState.data?.let { data ->
                        handleNewData(data)
                    }
                    dataState.stateMessage?.let { stateMessage ->
                        handleNewStateMessage(stateMessage)
                    }
                    dataState.stateEvent?.let { stateEvent ->
                        removeStateEvent(stateEvent)
                    }
                }
            }
            .launchIn(getChannelScope())
    }

    private fun handleNewStateMessage(stateMessage: StateMessage) {
        messageStack.add(stateMessage)
    }

    private fun getChannelScope(): CoroutineScope {
        return channelScope?: setUpNewChannelScope(CoroutineScope(IO))
    }

    private fun setUpNewChannelScope(coroutineScope: CoroutineScope): CoroutineScope {
        channelScope = coroutineScope
        return channelScope as CoroutineScope
    }

    abstract fun handleNewData(data: ViewState)

    fun removeStateEvent(stateEvent: StateEvent?) {
        stateEventManager.removeStateEvent(stateEvent)
    }

    fun cancelJobs() {
        if (channelScope != null) {
            if (channelScope?.isActive == true) {
               channelScope?.cancel()
            }
            channelScope = null
        }
        clearActiveStateEventCounter()
    }

    fun clearActiveStateEventCounter() {
        stateEventManager.clearActiveStateEventCounter()
    }

    private fun offerToDataChannel(dataState: DataState<ViewState>) {
        dataChannel.let {
            if (!it.isClosedForSend) {
                it.offer(dataState)
            }
        }
    }

    fun launchJob(
        stateEvent: StateEvent,
        jobFunction: Flow<DataState<ViewState>?> // 각 화면별 ViewModel 에서 정의한 ViewState 별
    ) {
        // 이벤트를 실행할 수 있는 상태인지 체크(이미 진행중/후인 이벤트일 수 있음)
        if(canExecuteNewStateEvent(stateEvent)) {
            printLogD(this.javaClass, "launching job: ${stateEvent.eventName()}")
            addStateEvent(stateEvent)
            jobFunction.onEach { dataState ->
                dataState?.let { dState ->
                    offerToDataChannel(dState)
                }
            }.launchIn(getChannelScope())
        }
    }

    private fun addStateEvent(stateEvent: StateEvent) {
        stateEventManager.addStateEvent(stateEvent)
    }

    private fun canExecuteNewStateEvent(stateEvent: StateEvent): Boolean {
        if (isJobAlreadyActive(stateEvent)) {
            return false
        } else if (!isMessageStackEmpty()) {
            return false
        }
        return true
    }

    private fun isMessageStackEmpty(): Boolean {
        return messageStack.isStackEmpty()
    }

    private fun isJobAlreadyActive(stateEvent: StateEvent): Boolean {
        return isStateEventActive(stateEvent)
    }

    private fun isStateEventActive(stateEvent: StateEvent): Boolean {
        return stateEventManager.isStateEventActive(stateEvent)
    }

    // 디버깅용
    fun getActiveJobs() = stateEventManager.getActiveJobNames()

    fun clearStateMessage(index: Int = 0) {
        messageStack.removeAt(index)
    }

    fun clearAllStateMessages() {
        messageStack.clear()
    }

    fun printStateMessages() {
        for(message in messageStack) {
            printLogD(this.javaClass, "${message.response.message}")
        }
    }

    
}