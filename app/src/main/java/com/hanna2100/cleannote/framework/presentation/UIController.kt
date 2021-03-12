package com.hanna2100.cleannote.framework.presentation

import com.hanna2100.cleannote.business.domain.state.DialogInputCaptureCallback
import com.hanna2100.cleannote.business.domain.state.Response
import com.hanna2100.cleannote.business.domain.state.StateMessageCallback

interface UIController  {

    fun displayProgressBar(isDisplayed: Boolean)

    fun hideSoftKeyboard()

    fun displayInputCaptureDialog(title: String, callback: DialogInputCaptureCallback)

    fun onResponseReceived(
        response: Response,
        stateMessageCallback: StateMessageCallback
    )

}