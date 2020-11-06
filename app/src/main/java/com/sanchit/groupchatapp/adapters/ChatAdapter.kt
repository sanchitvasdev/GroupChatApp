package com.sanchit.groupchatapp.adapters

import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.sanchit.groupchatapp.R
import com.sanchit.groupchatapp.models.Message
import com.squareup.picasso.Picasso


/**
 * This class inflates and holds up layouts
 * on the ChatRoomActivity depending according
 * to various scenarios.
 *
 * @author Sanchit Vasdev
 * @version 1.0, 11/06/2020
 */
private const val CHAT_SEND_VIEW_TYPE = 1
private const val CHAT_RECV_VIEW_TYPE = 2

class ChatAdapter(context: Context, query: Query, userID: String?) :
    FirestoreRecyclerAdapter<Message, ChatAdapter.ChatHolder>(
        FirestoreRecyclerOptions.Builder<Message>()
            .setQuery(query, Message::class.java)
            .build()
    ) {

    private var userId: String? = userID
    private var database: FirebaseFirestore? = null
    private var auth: FirebaseAuth? = null

    /**
     * If message userId matches current userid, set view type 1 else set view type 2.
     *
     * @param position Getting position of a particular message.
     * @return <code>1</code>, or
     *         <code>2</code>
     */
    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).getMessageUserId() == userId) {
            CHAT_SEND_VIEW_TYPE
        } else CHAT_RECV_VIEW_TYPE
    }

    /**
     *  Inflating two different layouts, one for messages from others and the other for user's messages.
     *
     *  @param parent Parent of the layouts.
     *  @param viewType viewType of the layouts.
     *  @return Returns a specific view to the ChatHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {
        lateinit var view: View
        if (viewType === CHAT_SEND_VIEW_TYPE) {
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.chat_send_message, parent, false)
        } else {
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.chat_recv_message, parent, false)
        }
        return ChatHolder(view)
    }

    /**
     * Bind values from Message class to the viewHolder.
     * @param holder Holder of the adapter.
     * @param position Position of each message
     * @param model Bind the specified class model with holder.
     */
    override fun onBindViewHolder(holder: ChatHolder, position: Int, model: Message) {

        val text = holder.text
        val username = holder.username
        val time = holder.time
        val image = holder.image

        val id = getItem(position).getMessageUserId()
        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        /**
         * Setting details of the sender user in the message.
         */
        lateinit var docRef: DocumentReference
        if (userId.equals(id)) {
            docRef = database!!.collection("users").document(userId!!)
        } else {
            docRef = database!!.collection("users").document(id)
        }
        docRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot != null) {
                Picasso.get().load(documentSnapshot.data?.get("thumbImage").toString()).into(image)
                username.setText(documentSnapshot.data?.get("name").toString())
            } else {
                Log.d("TAG", "ERROR OCCURRED")
            }
        }.addOnFailureListener { exception ->
            Log.d("TAG", exception.toString())
        }
        text.setText(model.getMessageText())
        time.setText(DateFormat.format("dd MMM  (h:mm a)", model.getMessageTime()))
    }

    /**
     * Holds the place in the recycler view for the itemView mentioned.
     *
     * @param itemView Takes the view passed.
     */
    class ChatHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var text: TextView
        var username: TextView
        var time: TextView
        var image: ImageView

        init {
            text = itemView.findViewById(R.id.text)
            username = itemView.findViewById(R.id.userName)
            time = itemView.findViewById(R.id.time)
            image = itemView.findViewById(R.id.userimgView)
        }
    }
}