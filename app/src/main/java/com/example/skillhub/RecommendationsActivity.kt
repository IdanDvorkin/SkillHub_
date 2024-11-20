package com.example.skillhub

import SearchAdapter
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlin.collections.ArrayList

class RecommendationsActivity : AppCompatActivity() {

    private lateinit var recommendationsRecyclerView: RecyclerView
    private lateinit var noMatchesTextView: TextView
    private lateinit var database: DatabaseReference
    private lateinit var recommendationsAdapter: SearchAdapter
    private val recommendedUsersList = ArrayList<User>()
    private val currentUserId: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recommendations)

        recommendationsRecyclerView = findViewById(R.id.recommendationsRecyclerView)
        noMatchesTextView = findViewById(R.id.noMatchesTextView)

        recommendationsRecyclerView.layoutManager = LinearLayoutManager(this)
        recommendationsAdapter = SearchAdapter(recommendedUsersList)
        recommendationsRecyclerView.adapter = recommendationsAdapter

        database = FirebaseDatabase.getInstance().getReference("users")

        loadRecommendations()
        findViewById<ImageView>(R.id.recommendationsIcon).setOnClickListener {

        }

        findViewById<ImageView>(R.id.searchIcon).setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.chatsIcon).setOnClickListener {
            startActivity(Intent(this, Chats_Activity::class.java))
        }

        findViewById<ImageView>(R.id.profileIcon).setOnClickListener {
            startActivity(Intent(this, Profile_Activity::class.java))
        }

    }

    private fun loadRecommendations() {
        // Clear any previous matches
        recommendedUsersList.clear()
        recommendationsAdapter.notifyDataSetChanged()
        noMatchesTextView.visibility = View.GONE

        database.child(currentUserId).get().addOnSuccessListener { currentUserSnapshot ->
            val currentUser = currentUserSnapshot.getValue(User::class.java)
            if (currentUser != null) {
                findMutualMatches(currentUser)
            }
        }.addOnFailureListener {
            noMatchesTextView.visibility = View.VISIBLE
            noMatchesTextView.text = "Failed to load recommendations."
        }
    }

    private fun findMutualMatches(currentUser: User) {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                recommendedUsersList.clear()

                for (userSnapshot in dataSnapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    if (user != null && userSnapshot.key != currentUserId) {
                        // Check for mutual match
                        val isMutualMatch = (user.canTeach == currentUser.wantToLearn) &&
                                (user.wantToLearn == currentUser.canTeach)

                        if (isMutualMatch) {
                            recommendedUsersList.add(user)
                        }
                    }
                }

                if (recommendedUsersList.isEmpty()) {
                    noMatchesTextView.visibility = View.VISIBLE
                    noMatchesTextView.text = "No mutual matches found."
                } else {
                    noMatchesTextView.visibility = View.GONE
                }
                recommendationsAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                noMatchesTextView.visibility = View.VISIBLE
                noMatchesTextView.text = "Failed to load recommendations."
            }
        })
    }
}
