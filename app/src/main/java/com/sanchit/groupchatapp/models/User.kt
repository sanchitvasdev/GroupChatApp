package com.sanchit.groupchatapp.models

/**
 * Model class containing details of a particular user.
 *
 * @author Sanchit Vasdev
 * @version 1.0, 11/06/2020
 */
data class User(
    val name: String,
    val state: String,
    val city: String,
    val imageUrl: String,
    val thumbImage: String,
    val uid: String,
    val joinedDate: String
)