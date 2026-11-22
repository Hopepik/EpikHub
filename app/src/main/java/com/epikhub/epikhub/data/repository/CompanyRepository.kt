package com.epikhub.epikhub.data.repository

import com.epikhub.epikhub.data.model.Company
import com.epikhub.epikhub.domain.model.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CompanyRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun getActiveCompaniesFlow(): Flow<Resource<List<Company>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = firestore.collection("companies")
            .whereEqualTo("status", "active")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Error loading companies"))
                    return@addSnapshotListener
                }
                val companies = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Company::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(Resource.Success(companies))
            }
        awaitClose { listener.remove() }
    }

    suspend fun getCompanyById(companyId: String): Resource<Company> {
        return try {
            val doc = firestore.collection("companies").document(companyId).get().await()
            val company = doc.toObject(Company::class.java)?.copy(id = doc.id)
            if (company != null) {
                Resource.Success(company)
            } else {
                Resource.Error("Company not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to get company details", e)
        }
    }

    suspend fun updateCompanyProfile(
        companyId: String,
        name: String,
        description: String,
        phone: String,
        email: String,
        location: String,
        logoUrl: String?
    ): Resource<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "name" to name.trim(),
                "description" to description.trim(),
                "phone" to phone.trim(),
                "email" to email.trim(),
                "location" to location.trim(),
                "updatedAt" to System.currentTimeMillis()
            )
            if (logoUrl != null) {
                updates["logoUrl"] = logoUrl
            }
            firestore.collection("companies").document(companyId).update(updates).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update company profile", e)
        }
    }
}
