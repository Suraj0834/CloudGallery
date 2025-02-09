package com.example.calmapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.Glide
import com.example.calmapp.databinding.ActivityGallery2Binding


class GalleryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGallery2Binding
    private lateinit var adapter: GalleryAdapter
    private val firestoreHelper = FirestoreHelper()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseStorage = FirebaseStorage.getInstance()
    private var imageUri: Uri? = null
    private lateinit var currentUserId: String

    private val storiesList = mutableListOf<Story>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGallery2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentUser = firebaseAuth.currentUser
        currentUserId = currentUser?.uid ?: ""

        adapter = GalleryAdapter(storiesList)
        binding.RecyclerView.layoutManager = LinearLayoutManager(this)
        binding.RecyclerView.adapter = adapter

        fetchUserStories()

        binding.fabAddStory.setOnClickListener {
            showAddStoryDialog()
        }
    }

    private fun fetchUserStories() {
        firestoreHelper.getUserStories(currentUserId) { snapshot ->
            snapshot?.let {
                storiesList.clear()
                for (document in it.documents) {
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val caption = document.getString("caption") ?: ""
                    val userId = document.getString("userId") ?: ""
                    val story = Story(imageUrl, caption, userId)
                    storiesList.add(story)
                }
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun showAddStoryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dailog_add_story, null)
        val editTextCaption = dialogView.findViewById<EditText>(R.id.captionText)
        val imageViewSelect = dialogView.findViewById<ImageView>(R.id.galleryImage)
        val selectImageButton = dialogView.findViewById<Button>(R.id.selectImageButton)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add New Story")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val caption = editTextCaption.text.toString()
                if (caption.isNotEmpty() && imageUri != null) {
                    uploadImage(imageUri!!, caption)
                } else {
                    Toast.makeText(this, "Please add an image and caption", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        dialog.show()
    }

    private fun uploadImage(imageUri: Uri, caption: String) {
        val storageRef = firebaseStorage.reference.child("images/${System.currentTimeMillis()}.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveStoryToFirestore(uri.toString(), caption)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveStoryToFirestore(imageUrl: String, caption: String) {
        firestoreHelper.saveStory(currentUserId, imageUrl, caption)
            .addOnSuccessListener {
                Toast.makeText(this, "Story added", Toast.LENGTH_SHORT).show()
                fetchUserStories()  // Refresh the stories list
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add story", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            // Update the ImageView in the dialog
            val dialogView = layoutInflater.inflate(R.layout.dailog_add_story, null)
            val imageViewSelect = dialogView.findViewById<ImageView>(R.id.galleryImage)
            imageViewSelect.setImageURI(imageUri)
        }
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
}