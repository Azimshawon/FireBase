package com.example.firebase

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_fire_store.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FireStore : AppCompatActivity() {

    private val personCollectionRef = Firebase.firestore.collection("persons")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fire_store)

        btnUploadData.setOnClickListener {
            val firstName = etFirstName.text.toString()
            val lastName = etLastName.text.toString()
            val age = etAge.text.toString().toInt()
            val person = Person(firstName, lastName, age)
            savePerson(person)
        }

        subscribeToRealtimeUpdates()

        /*btnRetrieveData.setOnClickListener {
            retrievePersons()
        }*/
    }

    private  fun subscribeToRealtimeUpdates(){
        personCollectionRef.addSnapshotListener { value, error ->
            error?.let {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }
            value?.let {
                val stringBuilder = StringBuilder()
                for (document in it) {
                    val person = document.toObject<Person>()
                    stringBuilder.append("$person\n")
                }
                tvPersons.text = stringBuilder.toString()
            }
        }
    }

    private fun retrievePersons() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val querySnapshot = personCollectionRef.get().await()
            val stringBuilder = StringBuilder()
            for (document in querySnapshot.documents) {
                val person = document.toObject<Person>()
                stringBuilder.append("$person\n")
            }
            withContext(Dispatchers.Main) {
                tvPersons.text = stringBuilder.toString()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@FireStore, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun savePerson(person: Person) = CoroutineScope(Dispatchers.IO).launch {
        try {
            personCollectionRef.add(person).await()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@FireStore, "Successfully saved data ", Toast.LENGTH_LONG)
                    .show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@FireStore, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

}