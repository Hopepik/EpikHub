package com.epikhub.epikhub.data.model

enum class RequestStatus(val displayName: String) {
    PENDING("Pending"),
    ACCEPTED("Accepted"),
    REJECTED("Rejected"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    companion object {
        fun fromString(value: String?): RequestStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: PENDING
        }
    }
}

data class ServiceRequest(
    val id: String = "",
    val customerId: String = "",
    val companyId: String = "",
    val serviceId: String = "",
    val serviceName: String = "",
    val servicePrice: Double = 0.0,
    val customerName: String = "",
    val customerPhone: String = "",
    val description: String = "",
    val status: String = RequestStatus.PENDING.name,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val requestStatus: RequestStatus get() = RequestStatus.fromString(status)
}
