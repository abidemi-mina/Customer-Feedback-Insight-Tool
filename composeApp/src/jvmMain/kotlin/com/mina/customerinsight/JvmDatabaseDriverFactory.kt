package com.mina.customerinsight

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

class JvmDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver {
        // Get user's home directory
        val dbPath = System.getProperty("user.home") + File.separator + ".customerinsight"

        // Create directory if it doesn't exist
        val dbDir = File(dbPath)
        if (!dbDir.exists()) {
            dbDir.mkdirs()
        }

        // Use file-based database
        val dbFile = File(dbDir, "feedback.db")
        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")

        // Create schema if it doesn't exist
        if (!dbFile.exists()) {
            FeedbackDB.Schema.create(driver)
        }

        return driver
    }
}