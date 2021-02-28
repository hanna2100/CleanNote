package com.hanna2100.cleannote.framework.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.hanna2100.cleannote.R
import com.hanna2100.cleannote.util.printLogD
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as BaseApplication).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        printLogD(this.javaClass, "firebaseauth: $firebaseAuth")
    }
}