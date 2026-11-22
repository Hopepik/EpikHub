package com.epikhub.epikhub.data.repository

import com.epikhub.epikhub.data.model.User
import com.epikhub.epikhub.domain.model.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val currentUserId: String? get() = auth.currentUser?.uid

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    suspend fun login(email: String, pass: String): Resource<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), pass).await()
            val uid = result.user?.uid ?: return Resource.Error("User ID not found")
            getUserProfile(uid)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Login failed", e)
        }
    }

    suspend fun registerCustomer(fullName: String, phone: String, email: String, pass: String): Resource<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), pass).await()
            val userObj = result.user ?: return Resource.Error("Registration failed")
            val uid = userObj.uid

            // Refresh ID token to ensure Auth token context is attached before Firestore write
            userObj.getIdToken(true).await()

            val userMap = mutableMapOf<String, Any?>(
                "uid" to uid,
                "fullName" to fullName.trim(),
                "email" to email.trim(),
                "phone" to phone.trim(),
                "role" to "customer",
                "companyId" to null,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("users").document(uid).set(userMap).await()

            val newUser = User(
                uid = uid,
                fullName = fullName.trim(),
                email = email.trim(),
                phone = phone.trim(),
                role = "customer",
                companyId = null
            )
            Resource.Success(newUser)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Registration failed", e)
        }
    }

    suspend fun createMissingCustomerProfile(fullName: String, phone: String): Resource<User> {
        val userObj = auth.currentUser ?: return Resource.Error("User not authenticated")
        val uid = userObj.uid
        val email = userObj.email ?: ""

        return try {
            userObj.getIdToken(true).await()

            val userMap = mutableMapOf<String, Any?>(
                "uid" to uid,
                "fullName" to fullName.trim(),
                "email" to email.trim(),
                "phone" to phone.trim(),
                "role" to "customer",
                "companyId" to null,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("users").document(uid).set(userMap).await()

            val newUser = User(
                uid = uid,
                fullName = fullName.trim(),
                email = email.trim(),
                phone = phone.trim(),
                role = "customer",
                companyId = null
            )
            Resource.Success(newUser)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to create customer profile", e)
        }
    }

    suspend fun getUserProfile(uid: String): Resource<User> {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            val user = doc.toObject(User::class.java)
            if (user != null && doc.exists()) {
                Resource.Success(user)
            } else {
                Resource.Error("PROFILE_MISSING")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to fetch user profile", e)
        }
    }

    suspend fun updateUserProfile(uid: String, fullName: String, phone: String): Resource<Unit> {
        return try {
            val updates = mapOf(
                "fullName" to fullName.trim(),
                "phone" to phone.trim(),
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("users").document(uid).update(updates).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update profile", e)
        }
    }

    fun logout() {
        auth.signOut()
    }
}
