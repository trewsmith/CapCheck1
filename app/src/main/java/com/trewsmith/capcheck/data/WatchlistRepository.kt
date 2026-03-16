package com.trewsmith.capcheck.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class WatchlistItem(val symbol: String = "", val name: String = "")

class WatchlistRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun userCollection() = db.collection("users")
        .document(auth.currentUser!!.uid)
        .collection("watchlist")

    suspend fun getWatchlist(): List<WatchlistItem> {
        return userCollection().get().await()
            .toObjects(WatchlistItem::class.java)
    }

    suspend fun addToWatchlist(symbol: String, name: String) {
        userCollection().document(symbol).set(WatchlistItem(symbol, name)).await()
    }

    suspend fun removeFromWatchlist(symbol: String) {
        userCollection().document(symbol).delete().await()
    }
}
