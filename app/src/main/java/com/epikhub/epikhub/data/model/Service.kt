package com.epikhub.epikhub.data.model

data class Service(
    val id: String = "",
    val companyId: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "General",
    val price: Double = 0.0,
    val imageUrl: String? = null,
    val isActive: Boolean = true,
    val companyActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
