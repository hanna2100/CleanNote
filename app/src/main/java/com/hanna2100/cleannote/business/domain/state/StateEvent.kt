package com.hanna2100.cleannote.business.domain.state

interface StateEvent {
    fun errorInfo(): String
    fun eventName(): String
    fun shouldDisplayProgressBar(): Boolean
}