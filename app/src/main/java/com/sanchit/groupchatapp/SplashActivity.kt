package com.sanchit.groupchatapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth


/**
 * This activity class opens up other app activities.
 *
 * @author Sanchit Vasdev
 * @version 11/06/2020
 */
class SplashActivity : AppCompatActivity() {

    private val auth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (auth.currentUser == null) {
            /**
             * If the user does not exists, open MainActivity for sign in and sign up.
             */
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            /**
             * If the user exists, directly open ChatRoomActivity.
             */
            startActivity(Intent(this, ChatRoomActivity::class.java))
        }
        finish()
    }
}