package com.hanna2100.cleannote.di

import com.hanna2100.cleannote.business.TempTest
import com.hanna2100.cleannote.framework.datasource.network.NoteFirestoreServiceTests
import com.hanna2100.cleannote.framework.presentation.TestBaseApplication
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        TestModule::class
    ]
)
interface TestAppComponent : AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: TestBaseApplication): TestAppComponent
    }

    fun inject(tempTest: TempTest)
    fun inject(noteFirestoreServiceTests: NoteFirestoreServiceTests)
}