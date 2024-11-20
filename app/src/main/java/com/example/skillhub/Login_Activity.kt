package com.example.skillhub

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

class Login_Activity : AppCompatActivity() {

    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize the FirebaseUI sign-in launcher
        initSignInLauncher()

        // Start FirebaseUI login flow
        startSignInFlow()
    }

    private fun initSignInLauncher() {
        // Register for the sign-in result
        signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val response = IdpResponse.fromResultIntent(result.data)

            if (result.resultCode == RESULT_OK) {
                // Successfully signed in
                val user = FirebaseAuth.getInstance().currentUser
                startActivity(Intent(this, Profile_Activity::class.java))
                finish()  // Close Login_Activity
            } else {
                // Sign-in failed
                response?.error?.let {
                    Toast.makeText(this, "Sign-in error: ${it.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun startSignInFlow() {
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),   // Email login
           // AuthUI.IdpConfig.GoogleBuilder().build(),  // Google login
            AuthUI.IdpConfig.PhoneBuilder().build()    // Phone login
            // Add more providers if needed (Facebook, etc.)
        )

        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.drawable.app_logo) // Optional: Add your logo
            //.setTheme(R.style.AppTheme)    // Optional: Apply a custom theme
            .build()

        signInLauncher.launch(signInIntent)
    }
}
