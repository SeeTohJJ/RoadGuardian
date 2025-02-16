package com.example.test.Model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HazardViewModel : ViewModel() {
    private val _hazardLocations = MutableStateFlow<List<LatLng>>(emptyList())
    val hazardLocations: StateFlow<List<LatLng>> = _hazardLocations

    private val db: DatabaseReference = FirebaseDatabase.getInstance().getReference("Cases")
    private var hazardListener: ValueEventListener? = null

    init {
        // Set up the real-time listener when the ViewModel is created
        setupHazardListener()
    }

    // Function to fetch hazard data from Firebase Realtime Database
    fun fetchHazardData() {
        viewModelScope.launch {
            try {
                db.get().addOnSuccessListener { dataSnapshot ->
                    val locations = parseSnapshot(dataSnapshot)
                    _hazardLocations.value = locations
                }.addOnFailureListener { e ->
                    Log.w("RealtimeDatabase", "Error fetching hazard data: ", e)
                }
            } catch (e: Exception) {
                Log.w("RealtimeDatabase", "Error fetching hazard data: ", e)
            }
        }
    }

    // Function to set up a real-time listener on Firebase Realtime Database
    private fun setupHazardListener() {
        hazardListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val locations = parseSnapshot(snapshot)
                _hazardLocations.value = locations
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("RealtimeDatabase", "Error listening to changes: ", error.toException())
            }
        }
        db.addValueEventListener(hazardListener!!)
    }

    // Helper function to process DataSnapshot and update StateFlow
    private fun parseSnapshot(snapshot: DataSnapshot): List<LatLng> {
        return snapshot.children.mapNotNull { document ->
            val status = document.child("status").getValue(String::class.java)
            if (status?.trim()?.equals("Repair Completed", ignoreCase = true) != true) {
                val latString = document.child("latitude").getValue(String::class.java)
                val lngString = document.child("longitude").getValue(String::class.java)

                val lat = latString?.toDoubleOrNull()
                val lng = lngString?.toDoubleOrNull()

                if (lat != null && lng != null) {
                    LatLng(lat, lng)
                } else {
                    null
                }
            } else {
                null
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Remove the Firebase Realtime Database listener when ViewModel is cleared
        hazardListener?.let { db.removeEventListener(it) }
    }
}
