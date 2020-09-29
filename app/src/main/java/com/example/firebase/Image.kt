package com.example.firebase

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_image.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

private const val REQUEST_CODE_IMAGE_PIC = 0

class Image : AppCompatActivity() {

    var curFile: Uri? = null
    val imageRef = Firebase.storage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        ivImage.setOnClickListener {
            Intent(Intent.ACTION_GET_CONTENT).also {
                it.type = "image/*"
                startActivityForResult(it, REQUEST_CODE_IMAGE_PIC)
            }
        }

        btnUploadImage.setOnClickListener {
            uploadImageToStorage("myImage")
        }

        btnUploadImage.setOnClickListener {
            downloadImage("myImage")
        }

        btnDeleteImage.setOnClickListener {
            deleteImage("myImage")
        }

        listFile()


    }

    private fun listFile() =  CoroutineScope(Dispatchers.IO).launch {
        try {
            val images = imageRef.child("images/").listAll().await()
            val imagesUrls = mutableListOf<String>()
            for (image in images.items) {
                val url = image.downloadUrl.await()
                imagesUrls.add(url.toString())
            }
            withContext(Dispatchers.Main) {
                val imageAdapter = ImageAdapter(imagesUrls)
                rvImages.apply {
                    adapter = imageAdapter
                    layoutManager = LinearLayoutManager(this@Image)
                }
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@Image, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun deleteImage(filename: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            imageRef.child("images/$filename").delete().await()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@Image, "Successfully deleted image", Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception){
            withContext(Dispatchers.Main) {
                Toast.makeText(this@Image, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun downloadImage(filename: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val maxDownloadSize = 5L * 1024 * 1024
            val bytes = imageRef.child("images/$filename").getBytes(maxDownloadSize).await()
            val btmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

            withContext(Dispatchers.Main){
                ivImage.setImageBitmap(btmap)
            }

        } catch (e: Exception){
            withContext(Dispatchers.Main) {
                Toast.makeText(this@Image, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun uploadImageToStorage(filename: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            curFile?.let {
                imageRef.child("images/$filename").putFile(it).await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Image, "Successfully uploaded image", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception){
            withContext(Dispatchers.Main) {
                Toast.makeText(this@Image, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_IMAGE_PIC){
            data?.data?.let {
                curFile = it
                ivImage.setImageURI(curFile)
            }
        }
    }
}