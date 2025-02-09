package com.example.calmapp

import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.QuerySnapshot
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser

class FirestoreHelper {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Function to sign up a new user
    fun signUp(email: String, password: String, onComplete: (FirebaseUser?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Successfully signed up
                    val user = auth.currentUser
                    onComplete(user)
                } else {
                    // Sign-up failed
                    onComplete(null)
                }
            }
    }

    // Function to sign in an existing user
    fun signIn(email: String, password: String, onComplete: (FirebaseUser?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Successfully signed in
                    val user = auth.currentUser
                    onComplete(user)
                } else {
                    // Sign-in failed
                    onComplete(null)
                }
            }
    }

    // Function to save a story (image URL and caption) to Firestore
    fun saveStory(userId: String, imageUrl: String, caption: String): Task<DocumentReference> {
        val data = hashMapOf(
            "imageUrl" to imageUrl,
            "caption" to caption,
            "userId" to userId
        )
        return db.collection("stories")
            .add(data)
    }

    // Function to retrieve stories for a specific user in real-time
    fun getUserStories(userId: String, onComplete: (QuerySnapshot?) -> Unit) {
        val storiesRef: CollectionReference = db.collection("stories")
        storiesRef.whereEqualTo("userId", userId).addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                onComplete(null)
            } else {
                onComplete(snapshot)
            }
        }
    }
}