package com.hanna2100.cleannote.framework.presentation

import android.app.Application
import com.hanna2100.cleannote.di.AppComponent
import com.hanna2100.cleannote.di.DaggerAppComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
// 단위테스트등, 다른곳에서 상속받아 쓰기 위해 open class 로 정의
open class BaseApplication : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        initAppComponent()
    }

    open fun initAppComponent() {
        appComponent = DaggerAppComponent.factory().create(this)
    }
}