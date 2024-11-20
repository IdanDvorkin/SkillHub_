package com.example.skillhub

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SignIn_Activity : AppCompatActivity() {

    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var signInButton: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Bind UI elements
        emailField = findViewById(R.id.emailField)
        passwordField = findViewById(R.id.passwordField)
        signInButton = findViewById(R.id.signInButton)

        // Get the email from the Intent (passed from the previous activity)
        val email = intent.getStringExtra("email")
        emailField.setText(email) // Pre-fill the email field

        // Sign-In Button action
        signInButton.setOnClickListener {
            val password = passwordField.text.toString().trim()

            // Check if inputs are valid
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Attempt to sign in the user
            signInUser(email ?: "", password)
        }
    }

    private fun signInUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-in successful, proceed to the profile activity
                    val intent = Intent(this, Profile_Activity::class.java)
                    startActivity(intent)
                    finish()  // Close the SignIn_Activity
                } else {
                    // Sign-in failed
                    Toast.makeText(this, "Sign-in failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
