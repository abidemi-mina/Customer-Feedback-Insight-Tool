package com.mina.customerinsight

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

class JvmDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver {
        val dbPath = System.getProperty("user.home") + File.separator + ".customerinsight"
        val dbDir = File(dbPath)

        if (!dbDir.exists()) {
            println("Creating database directory: ${dbDir.absolutePath}")
            dbDir.mkdirs()
        }

        val dbFile = File(dbDir, "feedback.db")
        println("Database file path: ${dbFile.absolutePath}")

        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")

        println("Creating database schema...")
        FeedbackDB.Schema.create(driver)
        println("Schema created successfully")

        return driver
    }
}