package com.hanna2100.cleannote.framework.presentation.splash

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.hanna2100.cleannote.R
import com.hanna2100.cleannote.business.domain.state.DialogInputCaptureCallback
import com.hanna2100.cleannote.framework.datasource.network.implementation.NoteFirestoreServiceImpl.Companion.EMAIL
import com.hanna2100.cleannote.framework.presentation.BaseApplication
import com.hanna2100.cleannote.framework.presentation.common.BaseNoteFragment
import com.hanna2100.cleannote.util.printLogD
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject
import javax.inject.Singleton

@FlowPreview
@ExperimentalCoroutinesApi
class SplashFragment
constructor(
    private val viewModelFactory: ViewModelProvider.Factory
): BaseNoteFragment(R.layout.fragment_splash) {

    val viewModel: SplashViewModel by viewModels {
        viewModelFactory
    }

    override fun inject() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkFirebaseAuth()
    }

    private fun checkFirebaseAuth() {
        if(FirebaseAuth.getInstance().currentUser == null) {
            displayCapturePassword()
        } else {
            subscribeObservers()
        }
    }

    private fun displayCapturePassword() {
        uiController.displayInputCaptureDialog(
            getString(R.string.text_enter_password),
            object : DialogInputCaptureCallback {
                override fun onTextCaptured(text: String) {
                    FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(EMAIL, text)
                        .addOnCompleteListener {
                            if(it.isSuccessful) {
                                printLogD(this.javaClass, "Signing in to Firebase: ${it.result}")
                                subscribeObservers()
                            } else {
                                printLogD(this.javaClass, "Failed signing in to Firebase")
                            }
                        }
                }
            }
        )
    }

    private fun subscribeObservers() {
        viewModel.hasSyncBeenExecuted().observe(viewLifecycleOwner, Observer { hasSyncBeenExecuted ->
            if(hasSyncBeenExecuted){
                navNoteListFragment()
            }
        })
    }

    private fun navNoteListFragment(){
        findNavController().navigate(R.id.action_splashFragment_to_noteListFragment)
    }

}