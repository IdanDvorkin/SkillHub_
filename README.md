SkillHub - Exchange Skills with Ease
SkillHub is an innovative platform where users can exchange skills without monetary transactions. Users can connect, chat, and collaborate with others who share complementary skills.

Features
User Profile: Each user has a profile displaying their name, skills they can teach, and skills they want to learn.
Search: Users can search for others based on the skills they want to learn.
Chat: Real-time chat functionality to connect and communicate with other users.
Recommendations: Get recommendations for users whose skill sets match.
Navigation Bar: Easy access to Recommendations, Search, Chats, and Profile pages.


Project Structure
Main Activities
Profile_Activity: Manage your profile details, including skills and profile picture.
SearchActivity: Search for users based on their skills.
Chats_Activity: View the list of all your chats.
ChatActivity: Real-time chat with another user.
RecommendationsActivity: View users recommended based on complementary skills

data class User(
    val id: String = "",
    val name: String = "",
    val canTeach: String = "",
    val wantToLearn: String = ""
)

data class Chat(
    val chatId: String = "",
    val chatName: String = "",
    val lastMessage: String = "",
    val participants: Map<String, Boolean> = emptyMap()
)


data class Message(
    val sender: String = "",
    val messageText: String = "",
    val timestamp: String = ""
)

How to Use
Sign Up: Create your profile, specifying the skills you can teach and want to learn.
Search: Use the search bar to find users based on their skills.
Start a Chat: Connect with users by clicking on "Chat Now."
Recommendations: Explore recommended users whose skills align with yours.
View Chats: Access your chat history and continue conversations
