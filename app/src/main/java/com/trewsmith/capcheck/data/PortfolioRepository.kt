package com.trewsmith.capcheck.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.trewsmith.capcheck.data.model.PortfolioHolding
import kotlinx.coroutines.tasks.await

class PortfolioRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun userCollection() = db.collection("users")
        .document(auth.currentUser!!.uid)
        .collection("portfolio")

    suspend fun getHoldings(): List<PortfolioHolding> {
        return userCollection().get().await()
            .toObjects(PortfolioHolding::class.java)
    }

    suspend fun addHolding(holding: PortfolioHolding) {
        val updatedHolding = holding.copy(lastModified = System.currentTimeMillis())
        userCollection().document(holding.symbol).set(updatedHolding).await()
    }

    suspend fun removeHolding(symbol: String) {
        userCollection().document(symbol).delete().await()
    }
}
