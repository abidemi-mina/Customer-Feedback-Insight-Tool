package com.mina.customerinsight

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

// In AndroidDatabaseDriverFactory.kt
class AndroidDatabaseDriverFactory(private val context: Context) : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            FeedbackDB.Schema,
            context,
            "feedback.db"  // This is correct for file-based storage
        )
    }
}