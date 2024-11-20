package com.example.skillhub

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import android.media.ExifInterface
import com.google.firebase.database.core.view.View

class Profile_Activity : AppCompatActivity() {

    private lateinit var userNameEditText: EditText
    private lateinit var wantToLearnField: EditText
    private lateinit var canTeachField: EditText
    private lateinit var profileImageView: ImageView
    private lateinit var uploadButton: Button
    private lateinit var signOutButton: Button
    private lateinit var saveChangesButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String
    private lateinit var database: DatabaseReference

    private val PICK_IMAGE = 1
    private val READ_EXTERNAL_STORAGE_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        userNameEditText = findViewById(R.id.userName)
        wantToLearnField = findViewById(R.id.wantToLearnField)
        canTeachField = findViewById(R.id.canTeachField)
        profileImageView = findViewById(R.id.profilePicture)
        uploadButton = findViewById(R.id.uploadButton)
        signOutButton = findViewById(R.id.signOutButton)
        saveChangesButton = findViewById(R.id.saveChangesButton)

        val user = auth.currentUser
        if (user != null) {
            userId = user.uid
            loadUserData()
            loadProfileImageFromInternalStorage()
        }

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
            // Already on Profile page, no action needed or add a refresh
        }


        uploadButton.setOnClickListener {
            openImagePicker()
        }

        signOutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, Login_Activity::class.java))
            finish()
        }

        saveChangesButton.setOnClickListener {
            saveUserData()
        }
    }


    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            userId = currentUser.uid // Assign the user ID from the FirebaseAuth user

            // Now retrieve data from Firebase Database
            database.child("users").child(userId).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    userNameEditText.setText(snapshot.child("name").value as? String)
                    wantToLearnField.setText(snapshot.child("wantToLearn").value as? String)
                    canTeachField.setText(snapshot.child("canTeach").value as? String)
                } else {
                    Toast.makeText(this, "No data found for user", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show()
        }
    }


    private fun saveUserData() {
        val name = userNameEditText.text.toString().trim()
        val wantToLearn = wantToLearnField.text.toString().trim()
        val canTeach = canTeachField.text.toString().trim()

        // Check if we have an existing user ID; if not, generate one
        if (userId.isEmpty()) {
            val newUserRef = database.child("users").push() // Generate new ID
            userId = newUserRef.key ?: "" // Retrieve and set the new ID
        }

        // Map user data, including the ID if it was generated
        val userUpdates = mapOf(
            "id" to userId,
            "name" to name,
            "wantToLearn" to wantToLearn,
            "canTeach" to canTeach
        )

        // Save the data to the user's unique ID node
        database.child("users").child(userId).updateChildren(userUpdates)
            .addOnSuccessListener {
                Toast.makeText(this, "Changes saved successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save changes", Toast.LENGTH_SHORT).show()
            }
    }


    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri = data.data!!
            profileImageView.setImageURI(imageUri)
            saveProfileImageToInternalStorage(imageUri)
        }
    }

    private fun saveProfileImageToInternalStorage(imageUri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
            var bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Rotate the bitmap if required
            val correctedBitmap = rotateImageIfRequired(bitmap, imageUri)

            val file = File(filesDir, "$userId-profile.jpg")
            FileOutputStream(file).use { out ->
                correctedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                out.flush()
            }
            Toast.makeText(this, "Profile image saved locally!", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadProfileImageFromInternalStorage() {
        val file = File(filesDir, "$userId-profile.jpg")
        if (file.exists()) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            profileImageView.setImageBitmap(bitmap)
        } else {
            Toast.makeText(this, "No saved profile image found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun rotateImageIfRequired(bitmap: Bitmap, imageUri: Uri): Bitmap {
        val inputStream = contentResolver.openInputStream(imageUri)
        val exif = inputStream?.let { ExifInterface(it) }
        inputStream?.close()

        return when (exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
            else -> bitmap
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
