import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.skillhub.ChatActivity
import com.example.skillhub.R
import com.example.skillhub.User
import com.example.skillhub.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SearchAdapter(private val users: List<User>) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private val currentUserName = FirebaseAuth.getInstance().currentUser?.displayName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder.nameTextView.text = user.name
        holder.teachesWantsTextView.text = "Teaches: ${user.canTeach} | Wants: ${user.wantToLearn}"

        // Set up Chat Now button click
        holder.chatNowButton.setOnClickListener {
            if (user.id != currentUserId) {  // Only allow chat if it's a different user
                val context = holder.itemView.context

                // Generate a unique chat ID
                val chatId = generateChatId(currentUserName.orEmpty(), user.name)
                val chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId)

                // Create or update chat data
                val chatData = Chat(
                    chatId = chatId,
                    chatName = user.name,
                    lastMessage = "" // Initially no last message
                )

                // Update chatLists for both users
                val chatListRef = FirebaseDatabase.getInstance().getReference("chatLists")
                chatListRef.child(currentUserName.orEmpty()).child(user.name).setValue(chatData)
                chatListRef.child(user.name).child(currentUserName.orEmpty()).setValue(chatData)

                // Navigate to ChatActivity
                val intent = Intent(context, ChatActivity::class.java).apply {
                    putExtra("chatId", chatId)
                    putExtra("chatPartner", user.name)
                }
                context.startActivity(intent)
            } else {
                Toast.makeText(holder.itemView.context, "You can't chat with yourself!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount() = users.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val teachesWantsTextView: TextView = itemView.findViewById(R.id.teachesWantsTextView)
        val chatNowButton: Button = itemView.findViewById(R.id.chatNowButton)
    }

    // Helper function to generate unique chat ID
    private fun generateChatId(user1: String, user2: String): String {
        val sortedUsers = listOf(user1, user2).sorted()
        return "${sortedUsers[0]}_${sortedUsers[1]}"
    }
}
