package com.example.firebase

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
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
            val person = getOldPerson()
            savePerson(person)
        }

        //subscribeToRealtimeUpdates()

        btnRetrieveData.setOnClickListener {
            retrievePersons()
        }

        btnDeletePerson.setOnClickListener {
            val person = getOldPerson()
            deletePerson(person)
        }

        btnUpdatePerson.setOnClickListener {
            val oldPerson = getOldPerson()
            val newPersonMap = getNewPersonMap()
            updatePerson(oldPerson, newPersonMap)
        }

        btnBatchWrite.setOnClickListener {
            changeName("CKdZvXuKadFz3f0u3UGC", "Alan", "Mask")
        }

        btnTransaction.setOnClickListener {
            birthday("CKdZvXuKadFz3f0u3UGC")
        }
    }

    private fun getOldPerson(): Person {
        val firstName = etFirstName.text.toString()
        val lastName = etLastName.text.toString()
        val age = etAge.text.toString().toInt()
        return Person(firstName, lastName, age)
    }

    private fun getNewPersonMap(): Map<String, Any> {
        val firstName = etNewFirstName.text.toString()
        val lastName = etNewLastName.text.toString()
        val age = etNewAge.text.toString()
        val map = mutableMapOf<String, Any>()
        if (firstName.isNotEmpty()) {
            map["firstName"] = firstName
        }
        if (lastName.isNotEmpty()) {
            map["lastName"] = lastName
        }
        if (age.isNotEmpty()) {
            map["age"] = age.toInt()
        }
        return map
    }

    private fun deletePerson(person: Person) = CoroutineScope(Dispatchers.IO).launch {
        val personQuery = personCollectionRef
            .whereEqualTo("firstName", person.firstName)
            .whereEqualTo("lastName", person.lastName)
            .whereEqualTo("age", person.age)
            .get()
            .await()

        if (personQuery.documents.isNotEmpty()) {
            for (document in personQuery) {
                try {

                    personCollectionRef.document(document.id).delete().await()

//                    personCollectionRef.document(document.id).update(mapOf(
//                        "firstName" to FieldValue.delete()
//                    ))

                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@FireStore, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@FireStore, "No person matched the query", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun birthday(personId: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            Firebase.firestore.runTransaction {
                val personRef = personCollectionRef.document(personId)
                val person = it.get(personRef)
                val newAge = person["age"] as Long + 1
                it.update(personRef, "age", newAge)
                null
            }.await()
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@FireStore, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun changeName(
        personId: String,
        newFirstName: String,
        newLastName: String
    ) = CoroutineScope(Dispatchers.IO).launch {
        try {
            Firebase.firestore.runBatch {
                val personRef = personCollectionRef.document(personId)
                it.update(personRef, "firstName", newFirstName)
                it.update(personRef, "lastName", newLastName)
            }.await()
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@FireStore, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updatePerson(person: Person, newPersonMap: Map<String, Any>) =
        CoroutineScope(Dispatchers.IO).launch {
            val personQuery = personCollectionRef
                .whereEqualTo("firstName", person.firstName)
                .whereEqualTo("lastName", person.lastName)
                .whereEqualTo("age", person.age)
                .get()
                .await()

            if (personQuery.documents.isNotEmpty()) {
                for (document in personQuery) {
                    try {

                        personCollectionRef.document(document.id).set(
                            newPersonMap,
                            SetOptions.merge()
                        ).await()

                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@FireStore, e.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FireStore, "No person matched the query", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }

    private fun subscribeToRealtimeUpdates() {
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

        val fromAge = etFrom.text.toString().toInt()
        val toAge = etTo.text.toString().toInt()

        try {
            val querySnapshot = personCollectionRef
                .whereGreaterThan("age", fromAge)
                .whereLessThan("age", toAge)
                .orderBy("age")
                .get()
                .await()

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