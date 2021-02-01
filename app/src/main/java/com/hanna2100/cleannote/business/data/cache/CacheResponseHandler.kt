package com.hanna2100.cleannote.business.data.cache

import com.hanna2100.cleannote.business.data.cache.CacheErrors.CACHE_DATA_NULL
import com.hanna2100.cleannote.business.domain.state.*

abstract class CacheResponseHandler<ViewState, Data>(
        private val response: CacheResult<Data?>,
        private val stateEvent: StateEvent?
) {
    suspend fun getResult(): DataState<ViewState>? {
        return when(response) {
            is CacheResult.GenericError -> {
                DataState.error(
                        response = Response(
                                message = "${stateEvent?.errorInfo()}\n\n" +
                                        "Reason: ${response.errorMessage}",
                                uiComponentType = UIComponentType.Dialog(),
                                messageType = MessageType.Error()
                        ),
                        stateEvent = stateEvent
                )
            }
            is CacheResult.Success -> {
                if(response.value == null) {
                    DataState.error(
                            response = Response(
                                    message = "${stateEvent?.errorInfo()}\n\n" +
                                            "Reason: ${CACHE_DATA_NULL}",
                                    uiComponentType = UIComponentType.Dialog(),
                                    messageType = MessageType.Error()
                            ),
                            stateEvent = stateEvent
                    )
                } else {
                    handleSuccess(result  = response.value)
                }
            }
        }
    }

    abstract fun handleSuccess(result: Data): DataState<ViewState>
}