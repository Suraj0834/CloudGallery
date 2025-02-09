package com.example.calmapp

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private var lastClickTime: Long = 0
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // Check if the user is already signed in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // If the user is signed in, navigate to the GalleryActivity
            startActivity(Intent(this, GalleryActivity::class.java))
            finish() // Prevent going back to MainActivity
            return
        }

        val heartAnimation = findViewById<LottieAnimationView>(R.id.heartAnimation)

        heartAnimation.setOnClickListener {
            if (SystemClock.elapsedRealtime() - lastClickTime < 300) { // Double tap detection
                val intent = Intent(this, RoseActivity::class.java)
                startActivity(intent)
                finish() // Prevent going back to MainActivity
            }
            lastClickTime = SystemClock.elapsedRealtime()
        }
    }
}