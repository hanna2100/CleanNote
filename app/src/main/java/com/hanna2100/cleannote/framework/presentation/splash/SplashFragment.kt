package com.hanna2100.cleannote.framework.presentation.splash

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.hanna2100.cleannote.R
import com.hanna2100.cleannote.framework.presentation.BaseApplication
import com.hanna2100.cleannote.framework.presentation.common.BaseNoteFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class SplashFragment
constructor(
    private val viewModelFactory: ViewModelProvider.Factory
): BaseNoteFragment(R.layout.fragment_splash) {

    val viewModel: SplashViewModel by viewModels {
        viewModelFactory
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navNoteListFragment()
    }

    private fun navNoteListFragment(){
        findNavController().navigate(R.id.action_splashFragment_to_noteListFragment)
    }

    override fun inject() {
        activity?.run {
            (application as BaseApplication).appComponent
        }?: throw Exception("AppComponent is null.")
    }

}