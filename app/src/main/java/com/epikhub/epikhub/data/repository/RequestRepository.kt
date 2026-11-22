package com.epikhub.epikhub.data.repository

import com.epikhub.epikhub.data.model.RequestStatus
import com.epikhub.epikhub.data.model.ServiceRequest
import com.epikhub.epikhub.domain.model.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

enum class SyncState {
    QUEUED_LOCALLY,
    SUBMITTED_TO_SERVER,
    SYNC_FAILED
}

data class CustomerRequestItemUiState(
    val request: ServiceRequest,
    val syncState: SyncState = SyncState.SUBMITTED_TO_SERVER
)

class RequestRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        fun buildRequestMap(
            id: String,
            customerId: String,
            companyId: String,
            serviceId: String,
            serviceName: String,
            servicePrice: Double,
            customerName: String,
            customerPhone: String,
            description: String,
            createdAt: Long = System.currentTimeMillis(),
            updatedAt: Long = System.currentTimeMillis()
        ): Map<String, Any> {
            return mapOf(
                "id" to id,
                "customerId" to customerId,
                "companyId" to companyId,
                "serviceId" to serviceId,
                "serviceName" to serviceName,
                "servicePrice" to servicePrice,
                "customerName" to customerName.trim(),
                "customerPhone" to customerPhone.trim(),
                "description" to description.trim(),
                "status" to RequestStatus.PENDING.name,
                "createdAt" to createdAt,
                "updatedAt" to updatedAt
            )
        }
    }

    suspend fun submitServiceRequest(
        customerId: String,
        companyId: String,
        serviceId: String,
        serviceName: String,
        servicePrice: Double,
        customerName: String,
        customerPhone: String,
        description: String,
        customRequestId: String? = null
    ): Resource<String> {
        return try {
            val docRef = if (!customRequestId.isNullOrBlank()) {
                firestore.collection("serviceRequests").document(customRequestId)
            } else {
                firestore.collection("serviceRequests").document()
            }
            val now = System.currentTimeMillis()
            val requestMap = buildRequestMap(
                id = docRef.id,
                customerId = customerId,
                companyId = companyId,
                serviceId = serviceId,
                serviceName = serviceName,
                servicePrice = servicePrice,
                customerName = customerName,
                customerPhone = customerPhone,
                description = description,
                createdAt = now,
                updatedAt = now
            )

            // Non-blocking write: writes synchronously to local LevelDB cache
            val task = docRef.set(requestMap)
            // Task listeners run asynchronously when server acknowledges or rejects
            task.addOnFailureListener {
                // Logged for background telemetry
            }

            Resource.Success(docRef.id)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to submit request", e)
        }
    }

    fun getCustomerRequestsFlow(customerId: String): Flow<Resource<List<CustomerRequestItemUiState>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = firestore.collection("serviceRequests")
            .whereEqualTo("customerId", customerId)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Error loading requests"))
                    return@addSnapshotListener
                }

                val items = snapshot?.documents?.mapNotNull { doc ->
                    val req = doc.toObject(ServiceRequest::class.java)?.copy(id = doc.id)
                    if (req != null) {
                        val hasPending = doc.metadata.hasPendingWrites()
                        val syncState = if (hasPending) SyncState.QUEUED_LOCALLY else SyncState.SUBMITTED_TO_SERVER
                        CustomerRequestItemUiState(request = req, syncState = syncState)
                    } else null
                }?.sortedByDescending { it.request.createdAt } ?: emptyList()

                trySend(Resource.Success(items))
            }
        awaitClose { listener.remove() }
    }

    fun getCompanyRequestsFlow(companyId: String): Flow<Resource<List<ServiceRequest>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = firestore.collection("serviceRequests")
            .whereEqualTo("companyId", companyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Error loading company requests"))
                    return@addSnapshotListener
                }
                val requests = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ServiceRequest::class.java)?.copy(id = doc.id)
                }?.sortedByDescending { it.createdAt } ?: emptyList()
                trySend(Resource.Success(requests))
            }
        awaitClose { listener.remove() }
    }

    suspend fun getRequestById(requestId: String): Resource<ServiceRequest> {
        return try {
            val doc = firestore.collection("serviceRequests").document(requestId).get().await()
            val request = doc.toObject(ServiceRequest::class.java)?.copy(id = doc.id)
            if (request != null) {
                Resource.Success(request)
            } else {
                Resource.Error("Request not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to fetch request details", e)
        }
    }

    suspend fun updateRequestStatus(requestId: String, newStatus: RequestStatus): Resource<Unit> {
        return try {
            firestore.collection("serviceRequests").document(requestId).update(
                "status", newStatus.name,
                "updatedAt", System.currentTimeMillis()
            ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update request status", e)
        }
    }
}
