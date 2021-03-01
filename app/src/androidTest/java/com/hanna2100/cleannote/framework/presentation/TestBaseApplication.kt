package com.hanna2100.cleannote.framework.presentation

import com.hanna2100.cleannote.di.DaggerTestAppComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class TestBaseApplication : BaseApplication() {
    override fun initAppComponent() {
        appComponent = DaggerTestAppComponent.factory().create(this)
    }
}