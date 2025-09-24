package org.jetbrains.edu.junie2middie

import kotlinx.serialization.Serializable

@Serializable
data class Contact(
    val id: Long = 0,
    val name: String,
    val status: String,
    val avatarUrl: String
)

@Serializable
data class Message(
    val id: Long,
    val content: String,
    val senderId: Long,
    val receiverId: Long,
    val timestamp: String
)

@Serializable
data class ContactsResponse(val contacts: List<Contact>)

@Serializable
data class MessagesResponse(val messages: List<Message>)
