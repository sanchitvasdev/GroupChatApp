package com.sanchit.groupchatapp

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.sanchit.groupchatapp.adapters.ChatAdapter
import com.sanchit.groupchatapp.models.Message
import com.squareup.picasso.Picasso
import com.vanniktech.emoji.EmojiEditText
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.google.GoogleEmojiProvider
import kotlinx.android.synthetic.main.activity_chat_room.*
import kotlinx.android.synthetic.main.app_bar.toolbar_main

/**
 * This activity class represents a chat room
 * where all registered users can chat directly.
 *
 * @author Sanchit Vasdev
 * @version 1.0, 11/06/2020
 */

class ChatRoomActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    View.OnClickListener {

    var auth: FirebaseAuth? = null
    var user: FirebaseUser? = null
    var database: FirebaseFirestore? = null
    var query: Query? = null
    private var adapter: FirestoreRecyclerAdapter<Message, ChatAdapter.ChatHolder>? = null
    private var input: EmojiEditText? = null
    private var userId: String? = null
    private var userName: String? = null
    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * Installing EmojiManager.
         */
        EmojiManager.install(GoogleEmojiProvider())

        setContentView(R.layout.activity_chat_room)

        /**
         * Setting your own Action Bar.
         */
        setSupportActionBar(toolbar_main)

        drawer = findViewById(R.id.drawer_Layout)

        toggle = ActionBarDrawerToggle(
            this,
            drawer,
            toolbar_main,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        /**
         * Adding Toggle for drawer actions.
         */
        drawer.addDrawerListener(toggle)

        /**
         * Configuring Action Bar.
         */
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        val navigationView: NavigationView = findViewById(R.id.nav_View)
        navigationView.setNavigationItemSelectedListener(this)

        /**
         * Setting actions for emoji popup.
         */
        val emojiPopup = EmojiPopup.Builder.fromRootView(rootView).build(msgEdtv)
        smileBtn.setOnClickListener {
            emojiPopup.toggle()
        }

        val sendBtn: ImageView = findViewById(R.id.sendBtn)
        sendBtn.setOnClickListener(this)

        input = findViewById(R.id.msgEdtv)

        /**
         * Setting layoutManager to the Recycler view.
         */
        val recyclerView = findViewById<RecyclerView>(R.id.msgRv)
        recyclerView.layoutManager = LinearLayoutManager(applicationContext)

        auth = FirebaseAuth.getInstance()
        user = auth!!.currentUser

        /**
         * Check if user has signed in before else redirect to sign in page.
         */
        if (user == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        val navHeaderView: View = navigationView.getHeaderView(0)
        val nav_header_Name = navHeaderView.findViewById(R.id.nav_header_Name) as TextView
        val nav_header_imageView =
            navHeaderView.findViewById(R.id.nav_header_imageView) as ShapeableImageView
        val nav_header_Joineddate =
            navHeaderView.findViewById(R.id.nav_header_Joineddate) as TextView

        /**
         * Setting profile details of the user in drawer header.
         */
        database = FirebaseFirestore.getInstance()
        val docRef = database!!.collection("users").document(auth!!.uid.toString())
        docRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot != null) {
                nav_header_Name.setText(documentSnapshot.data?.get("name").toString())
                Picasso.get().load(documentSnapshot.data?.get("thumbImage").toString())
                    .into(nav_header_imageView)
                nav_header_Joineddate.setText(documentSnapshot.data?.get("joinedDate").toString())
            } else {
                Toast.makeText(this, "ERROR OCCURRED", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener { exception ->
            Log.d("TAG", exception.toString())
        }

        userId = user!!.uid
        userName = user!!.displayName
        /**
         * Ordering messages according to their time.
         */
        query = database!!.collection("messages").orderBy("messageTime")

        adapter = ChatAdapter(this, query!!, userId)
        recyclerView.adapter = adapter
    }

    /**
     * Adding message into database if it is not empty.
     *
     * @param v Takes a view for applying its clicking action.
     */
    override fun onClick(v: View) {
        if (v.id == R.id.sendBtn) {
            val message = input!!.text.toString()
            if (TextUtils.isEmpty(message)) {
                Toast.makeText(this@ChatRoomActivity, "Type Something", Toast.LENGTH_LONG).show()
                return
            }
            database!!.collection("messages").add(Message(userName!!, message, userId!!))
            input!!.setText("")
        }
    }

    /**
     * Instructs adapter to start on starting activity.
     */
    override fun onStart() {
        super.onStart()
        if (adapter != null) adapter!!.startListening()
    }

    /**
     * Instructs adapter to stop on stopping activity.
     */
    override fun onStop() {
        super.onStop()
        if (adapter != null) adapter!!.stopListening()
    }

    /**
     * Syncing toggle when an action changes.
     *
     * @param savedInstanceState Current state of the activity.
     */
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    /**
     * Informing toggle about the changed configuration.
     *
     * @param newConfig Changed configuration of the activity.
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }

    /**
     * Implementing back pressed action of the drawer layout.
     */
    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    /**
     * Specifying the tasks for a particular item of the navigation menu when it's clicked.
     *
     * @param item Represents a particular item of navigation menu.
     * @return <code>true</code> if the action is successful, or
     *         <code>false</code> if the action fails.
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logout) {
            auth!!.signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            Toast.makeText(this, "You have been logged out", Toast.LENGTH_LONG).show()
        }
        return true
    }
}