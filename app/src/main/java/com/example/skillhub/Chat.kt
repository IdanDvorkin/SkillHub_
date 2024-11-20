package com.example.skillhub

data class Chat(
    val chatId: String = "",
    val chatName: String = "",
    val lastMessage: String = "",
    val participants: Map<String, Boolean> = emptyMap() // Map of user IDs to participation status
)
