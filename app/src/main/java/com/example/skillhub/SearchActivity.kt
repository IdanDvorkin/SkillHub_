package com.example.skillhub


import SearchAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import android.view.View
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth


class SearchActivity : AppCompatActivity() {

    private lateinit var searchView: androidx.appcompat.widget.SearchView
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var noResultsTextView: TextView
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var database: DatabaseReference
    private val searchResultsList = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        searchView = findViewById(R.id.searchView)
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView)
        noResultsTextView = findViewById(R.id.noResultsTextView)

        searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
        searchAdapter = SearchAdapter(searchResultsList)
        searchResultsRecyclerView.adapter = searchAdapter

        database = FirebaseDatabase.getInstance().getReference("users")
        findViewById<ImageView>(R.id.recommendationsIcon).setOnClickListener {
            startActivity(Intent(this, RecommendationsActivity::class.java))
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

        // Set up the search action
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    searchForSkill(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

    }

    private fun searchForSkill(skill: String) {
        // Clear previous search results
        searchResultsList.clear()
        searchAdapter.notifyDataSetChanged()
        noResultsTextView.visibility = View.GONE

        // Get the current user ID
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        // Query Firebase for users with the "Can Teach" field matching the search skill
        database.orderByChild("canTeach").equalTo(skill)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Populate the RecyclerView with results, excluding the current user
                        for (snapshot in dataSnapshot.children) {
                            val user = snapshot.getValue(User::class.java)
                            if (user != null && snapshot.key != currentUserId) { // Exclude current user by checking the key
                                searchResultsList.add(user)
                            }
                        }
                        searchAdapter.notifyDataSetChanged()
                    }

                    // Show "No results" message if list is empty
                    if (searchResultsList.isEmpty()) {
                        noResultsTextView.visibility = View.VISIBLE
                    } else {
                        noResultsTextView.visibility = View.GONE
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle possible errors here
                }
            })
    }


}
