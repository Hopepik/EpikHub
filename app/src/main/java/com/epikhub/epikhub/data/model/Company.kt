package com.epikhub.epikhub.data.model

data class Company(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val phone: String = "",
    val email: String = "",
    val location: String = "",
    val logoUrl: String? = null,
    val status: String = "active",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isActive: Boolean get() = status.equals("active", ignoreCase = true)
}
