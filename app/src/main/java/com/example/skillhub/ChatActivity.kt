package com.example.skillhub

import ChatAdapter
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.Date
import java.util.Locale

class ChatActivity : AppCompatActivity() {

    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var database: DatabaseReference
    private val messagesList = mutableListOf<Message>()
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var currentUserId: String
    private var userName: String = "Anonymous"
    private lateinit var chatPartnerId: String
    private lateinit var chatPartnerName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        chatPartnerId = intent.getStringExtra("chatPartnerId") ?: "Unknown"
        chatPartnerName = intent.getStringExtra("chatPartnerName") ?: "Unknown"

        // Generate chat ID and get reference
        val chatId = generateChatId(currentUserId, chatPartnerId)
        database = FirebaseDatabase.getInstance().getReference("chats").child(chatId)

        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView)

        chatAdapter = ChatAdapter(messagesList)
        messagesRecyclerView.adapter = chatAdapter
        messagesRecyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch current user's name
        fetchUserName()

        loadMessages()

        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(chatId, messageText)
                messageEditText.text.clear()
            }
        }
    }

    private fun fetchUserName() {
        FirebaseDatabase.getInstance().getReference("users").child(currentUserId)
            .child("name")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        userName = snapshot.value as String
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ChatActivity, "Failed to fetch username", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun generateChatId(user1: String, user2: String): String {
        return if (user1 < user2) "${user1}_${user2}" else "${user2}_${user1}"
    }

    private fun sendMessage(chatId: String, messageText: String) {
        val messageId = database.push().key ?: return
        val timestamp = formatTimestamp(System.currentTimeMillis()) // Format timestamp with minutes
        val message = Message(sender = userName, messageText = messageText, timestamp = timestamp)

        database.child(messageId).setValue(message)
            .addOnSuccessListener {
                messagesRecyclerView.scrollToPosition(messagesList.size - 1)

                // Update chat list for both users
                val chatData = Chat(chatId, chatPartnerName, messageText)
                updateChatList(chatId, chatData)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
            }
    }

    private fun formatTimestamp(timestamp: Long): String {
        val dateFormat = java.text.SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }





    private fun updateChatList(chatId: String, chatData: Chat) {
        val chatListRef = FirebaseDatabase.getInstance().getReference("chatLists")
        chatListRef.child(currentUserId).child(chatPartnerId).setValue(chatData)
        chatListRef.child(chatPartnerId).child(currentUserId).setValue(chatData)
    }

    private fun loadMessages() {
        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java)
                if (message != null) {
                    messagesList.add(message)
                    chatAdapter.notifyItemInserted(messagesList.size - 1)
                    messagesRecyclerView.scrollToPosition(messagesList.size - 1)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ChatActivity, "Failed to load messages", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
