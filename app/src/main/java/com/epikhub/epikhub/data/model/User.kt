package com.epikhub.epikhub.data.model

enum class UserRole {
    CUSTOMER,
    COMPANY_ADMIN;

    companion object {
        fun fromString(value: String?): UserRole {
            return when (value?.lowercase()) {
                "company_admin", "admin" -> COMPANY_ADMIN
                else -> CUSTOMER
            }
        }
    }
}

data class User(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "customer",
    val companyId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val userRole: UserRole get() = UserRole.fromString(role)
    val isCompanyAdmin: Boolean get() = userRole == UserRole.COMPANY_ADMIN
    val isCustomer: Boolean get() = userRole == UserRole.CUSTOMER
}
