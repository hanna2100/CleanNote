package com.hanna2100.cleannote.framework.presentation.splash

import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.hanna2100.cleannote.R
import com.hanna2100.cleannote.framework.presentation.common.BaseNoteFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class SplashFragment
constructor(
    private val factory: ViewModelProvider.Factory
) : BaseNoteFragment(R.layout.fragment_splash){

    val viewModel: SplashViewModel by viewModels{
        factory
    }

    override fun inject() {
        TODO("Not yet implemented")
    }

}