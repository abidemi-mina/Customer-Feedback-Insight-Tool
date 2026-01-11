package com.mina.customerinsight.data

import com.mina.customerinsight.FeedbackDB
import com.mina.customerinsight.AdminEntity


class AuthService(private val database: FeedbackDB) {

    fun hashPassword(password: String): String = password

    suspend fun registerAdmin(email: String, password: String): Boolean {
        return try {
            val hashedPassword = hashPassword(password)
            val id = System.currentTimeMillis().toString()

            // Use feedbackDBQueries for ALL queries
            database.feedbackDBQueries.insertAdmin(
                id = id,
                email = email,
                hashedPassword = hashedPassword,
                businessProfileId = null,
                createdAt = System.currentTimeMillis()
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun loginAdmin(email: String, password: String): AdminEntity? {
        val hashedPassword = hashPassword(password)
        return database.feedbackDBQueries.getAdminByCredentials(email, hashedPassword)
            .executeAsOneOrNull()
    }

    suspend fun saveBusinessProfile(
        adminId: String,
        businessName: String,
        businessType: String,
        description: String,
        categories: List<String>
    ): Boolean {
        return try {
            val id = System.currentTimeMillis().toString()
            val categoriesJson = categories.joinToString(",")

            database.feedbackDBQueries.insertProfile(
                id = id,
                adminId = adminId,
                businessName = businessName,
                businessType = businessType,
                description = description,
                categories = categoriesJson,
                createdAt = System.currentTimeMillis()
            )

            database.feedbackDBQueries.updateAdminProfile(
                businessProfileId = id,
                id = adminId
            )
            true
        } catch (e: Exception) {
            false
        }
    }
}