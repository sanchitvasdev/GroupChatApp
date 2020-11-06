package com.sanchit.groupchatapp.models

import java.util.*

/**
 * Model class containing details of message from
 * a particular user
 *
 * @author Sanchit Vasdev
 * @version 1.0, 11/06/2020
 */
class Message(
    messageUser: String,
    messageText: String,
    messageUserId: String
) {
    private var messageUser: String
    private var messageText: String
    private var messageUserId: String
    private var messageTime: Long

    constructor() : this("", "", "")

    fun getMessageText(): String {
        return messageText
    }

    fun getMessageUserId(): String {
        return messageUserId
    }

    fun getMessageTime(): Long {
        return messageTime
    }

    init {
        this.messageUser = messageUser
        this.messageText = messageText
        messageTime = Date().time
        this.messageUserId = messageUserId
    }
}

