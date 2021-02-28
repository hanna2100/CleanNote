package com.hanna2100.cleannote.di

import com.hanna2100.cleannote.framwork.presentation.BaseApplication
import com.hanna2100.cleannote.framwork.presentation.MainActivity
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@Singleton
@FlowPreview
@ExperimentalCoroutinesApi
@Component(
    modules = [
        AppModule::class,
        ProductionModule::class
    ]
)
interface AppComponent{

    @Component.Factory
    interface Factory{

        fun create(@BindsInstance app: BaseApplication): AppComponent
    }

    fun inject(mainActivity: MainActivity)
}