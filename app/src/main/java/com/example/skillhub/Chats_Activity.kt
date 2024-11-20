package com.example.skillhub

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Chats_Activity : AppCompatActivity() {

    private lateinit var chatsRecyclerView: RecyclerView
    private lateinit var chatListAdapter: ChatListAdapter
    private val chatsList = mutableListOf<Chat>()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chats)

        chatsRecyclerView = findViewById(R.id.chatsRecyclerView)
        chatsRecyclerView.layoutManager = LinearLayoutManager(this)

        chatListAdapter = ChatListAdapter(chatsList) { chat ->
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("chatPartnerId", chat.chatId.replace(currentUserId!!, "").replace("_", ""))
                putExtra("chatPartnerName", chat.chatName)
            }
            startActivity(intent)
        }
        chatsRecyclerView.adapter = chatListAdapter

        loadChats()
        findViewById<ImageView>(R.id.recommendationsIcon).setOnClickListener {
            startActivity(Intent(this, RecommendationsActivity::class.java))
        }

        findViewById<ImageView>(R.id.searchIcon).setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.chatsIcon).setOnClickListener {

        }

        findViewById<ImageView>(R.id.profileIcon).setOnClickListener {
            startActivity(Intent(this, Profile_Activity::class.java))
        }
    }

    private fun loadChats() {
        currentUserId ?: return
        FirebaseDatabase.getInstance().getReference("chatLists").child(currentUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatsList.clear()
                    for (chatSnapshot in snapshot.children) {
                        val chat = chatSnapshot.getValue(Chat::class.java)
                        if (chat != null) chatsList.add(chat)
                    }
                    chatListAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Chats_Activity, "Failed to load chats", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
