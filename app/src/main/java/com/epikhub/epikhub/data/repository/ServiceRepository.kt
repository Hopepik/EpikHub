package com.epikhub.epikhub.data.repository

import com.epikhub.epikhub.data.model.Service
import com.epikhub.epikhub.domain.model.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ServiceRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun getActiveServicesForCompanyFlow(companyId: String): Flow<Resource<List<Service>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = firestore.collection("services")
            .whereEqualTo("companyId", companyId)
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Error loading services"))
                    return@addSnapshotListener
                }
                val services = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Service::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(Resource.Success(services))
            }
        awaitClose { listener.remove() }
    }

    fun getAllServicesForCompanyFlow(companyId: String): Flow<Resource<List<Service>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = firestore.collection("services")
            .whereEqualTo("companyId", companyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Error loading services"))
                    return@addSnapshotListener
                }
                val services = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Service::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(Resource.Success(services))
            }
        awaitClose { listener.remove() }
    }

    suspend fun getServiceById(serviceId: String): Resource<Service> {
        return try {
            val doc = firestore.collection("services").document(serviceId).get().await()
            val service = doc.toObject(Service::class.java)?.copy(id = doc.id)
            if (service != null) {
                Resource.Success(service)
            } else {
                Resource.Error("Service not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to get service details", e)
        }
    }

    suspend fun createService(service: Service): Resource<String> {
        return try {
            val docRef = firestore.collection("services").document()
            val newService = service.copy(id = docRef.id)
            docRef.set(newService).await()
            Resource.Success(docRef.id)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to create service", e)
        }
    }

    suspend fun updateService(service: Service): Resource<Unit> {
        return try {
            val updates = mutableMapOf<String, Any?>(
                "name" to service.name.trim(),
                "description" to service.description.trim(),
                "category" to service.category.trim(),
                "price" to service.price,
                "isActive" to service.isActive,
                "updatedAt" to System.currentTimeMillis()
            )
            if (!service.imageUrl.isNullOrBlank()) {
                updates["imageUrl"] = service.imageUrl.trim()
            } else {
                updates["imageUrl"] = null
            }

            firestore.collection("services").document(service.id).update(updates).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update service", e)
        }
    }

    suspend fun toggleServiceActive(serviceId: String, isActive: Boolean): Resource<Unit> {
        return try {
            firestore.collection("services").document(serviceId).update(
                "isActive", isActive,
                "updatedAt", System.currentTimeMillis()
            ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to toggle service status", e)
        }
    }
}
