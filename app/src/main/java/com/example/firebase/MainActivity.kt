package com.example.firebase

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

const val REQUEST_CODE_SIGN_IN = 0

class MainActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = FirebaseAuth.getInstance()

        btnRegister.setOnClickListener {
            registerUser()
        }

        btnLogin.setOnClickListener {
            loginUser()
        }

        btnUpdateProfile.setOnClickListener {
            updateProfile()
        }

//        googleID.setOnClickListener {
//            val option = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.webclient_id))
//                .requestEmail()
//                .build()
//            val signInClient = GoogleSignIn.getClient(this, option)
//            signInClient.signInIntent.also {
//                startActivityForResult(it, REQUEST_CODE_SIGN_IN )
//            }
//        }
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == REQUEST_CODE_SIGN_IN){
//            val account = GoogleSignIn.getSignedInAccountFromIntent(data).result
//            account?.let {
//                googleAuthForFirebase(it)
//            }
//        }
//    }

//    private fun googleAuthForFirebase(account: GoogleSignInAccount) {
//        val credentials = GoogleAuthProvider.getCredential(account.idToken, null)
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                auth.signInWithCredential(credentials).await()
//                withContext(Dispatchers.Main){
//                    Toast.makeText(this@MainActivity, "Successfully logged in", Toast.LENGTH_LONG).show()
//                }
//            } catch (e:Exception){
//                withContext(Dispatchers.Main){
//                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
//                }
//            }
//        }
//    }

    private fun updateProfile(){
        auth.currentUser?.let { user ->
            val username = etUsername.text.toString()
            val photoURI = Uri.parse("android.resource://$packageName/${R.drawable.logo_black_square}")
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .setPhotoUri(photoURI)
                .build()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    user.updateProfile(profileUpdates).await()
                    withContext(Dispatchers.Main){
                        checkLoggedInState()
                        Toast.makeText(this@MainActivity, "successfully updated user profile", Toast.LENGTH_LONG).show()
                    }
                }catch (e: Exception){
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        checkLoggedInState()
    }

    private fun registerUser(){
        val email = etEmailRegister.text.toString()
        val password = etPasswordRegister.text.toString()
        if (email.isNotEmpty() && password.isNotEmpty()){
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    auth.createUserWithEmailAndPassword(email, password).await()
                    withContext(Dispatchers.Main){
                        checkLoggedInState()
                    }
                }catch (e: Exception){
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun loginUser(){
        val email = etEmailLogin.text.toString()
        val password = etPasswordLogin.text.toString()
        if (email.isNotEmpty() && password.isNotEmpty()){
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    auth.signInWithEmailAndPassword(email, password).await()
                    withContext(Dispatchers.Main){
                        checkLoggedInState()
                    }
                }catch (e: Exception){
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun checkLoggedInState() {
        val user = auth.currentUser
        if (user == null){
            tvLoggedIn.text = "You are not logged in"
        } else {
            val intent = Intent(this, FireStore::class.java)
            startActivity(intent)
//            etUsername.setText(user.displayName)
//            ivProfilePicture.setImageURI(user.photoUrl)
        }
    }
}