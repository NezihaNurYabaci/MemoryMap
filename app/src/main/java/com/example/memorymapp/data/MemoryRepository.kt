package com.example.memorymapp.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MemoryRepository {
    private val db = FirebaseFirestore.getInstance()
    private val memoriesCollection = db.collection("memories")

    fun getMemories(): Flow<List<Memory>> = callbackFlow {
        val subscription = memoriesCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val memories = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Memory::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(memories)
            }
        awaitClose { subscription.remove() }
    }

    fun saveMemory(memory: Memory, onComplete: (Boolean) -> Unit) {
        memoriesCollection.add(memory)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}