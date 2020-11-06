package com.sanchit.groupchatapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.app_bar.*


/**
 * This activity class contains sign in
 * and sign up fragments to be used
 * for respective purposes.
 *
 * @author Sanchit Vasdev
 * @version 1/0, 11/06/2020
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /**
         * Setting your own Action Bar.
         */
        setSupportActionBar(toolbar_main)

        val navController = Navigation.findNavController(this, R.id.nav_host_fragment1)

    }
}