package com.example.memorymapp.viewmodel

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memorymapp.data.Memory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

class MemoryViewModel : ViewModel() {

    var description by mutableStateOf("")
    var lat by mutableDoubleStateOf(0.0)
    var lng by mutableDoubleStateOf(0.0)
    var address by mutableStateOf("Fetching address...")
    var isLoading by mutableStateOf(false)

    private val _memories = MutableStateFlow<List<Memory>>(emptyList())
    val memories: StateFlow<List<Memory>> = _memories

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase
        .getInstance("https://memorymapp-neziha-2026-default-rtdb.europe-west1.firebasedatabase.app")
        .getReference("Memories")

    init {
        if (auth.currentUser != null) {
            fetchMemories()
        }
    }

    fun fetchMemories() {
        val userId = auth.currentUser?.uid ?: return
        database.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Memory>()
                for (data in snapshot.children) {
                    data.getValue(Memory::class.java)?.let { list.add(it) }
                }
                _memories.value = list.sortedByDescending { it.timestamp }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FETCH_DEBUG", "Error: ${error.message}")
            }
        })
    }

    fun resetForm() {
        description = ""
        lat = 0.0
        lng = 0.0
        address = "Fetching address..."
    }

    fun clearData() {
        _memories.value = emptyList()
        resetForm()
    }

    fun fetchAddress(context: Context, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    address = "${addr.subLocality ?: ""}, ${addr.subAdminArea ?: ""}, ${addr.adminArea ?: ""}"
                        .replace(Regex(",\\s*,"), ",")
                        .trim().removePrefix(",").trim().removeSuffix(",")
                } else {
                    address = "Unknown Location"
                }
            } catch (e: Exception) {
                address = "Location service not ready"
            }
        }
    }

    fun saveMemory(date: String, onComplete: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        isLoading = true

        val memoryId = database.child(userId).push().key ?: return
        val memory = Memory(
            id = memoryId,
            description = description,
            date = date,
            lat = lat,
            lng = lng,
            address = address,
            timestamp = System.currentTimeMillis()
        )

        database.child(userId).child(memoryId).setValue(memory)
            .addOnSuccessListener {
                isLoading = false
                resetForm() // 🛠️ Artık hata vermeyecek
                onComplete()
            }
            .addOnFailureListener {
                isLoading = false
            }
    }
}