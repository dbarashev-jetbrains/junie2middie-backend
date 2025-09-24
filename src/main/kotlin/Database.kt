package org.jetbrains.edu.junie2middie

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import java.sql.ResultSet

object Database {
    private lateinit var dataSource: HikariDataSource

    fun init(config: ApplicationDatabaseConfig) {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.jdbcUrl
            username = config.user
            password = config.password
            maximumPoolSize = config.maxPoolSize
            driverClassName = "org.sqlite.JDBC"
        }
        dataSource = HikariDataSource(hikariConfig)
        createTablesIfNotExists()
    }

    fun getConnection(): Connection = dataSource.connection

    private fun createTablesIfNotExists() {
        getConnection().use { conn ->
            conn.createStatement().use { st ->
                st.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS contacts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        status TEXT NOT NULL,
                        avatar_url TEXT NOT NULL
                    );
                    """.trimIndent()
                )
                st.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS messages (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        content TEXT NOT NULL,
                        sender_id INTEGER NOT NULL,
                        receiver_id INTEGER NOT NULL,
                        timestamp TEXT NOT NULL
                    );
                    """.trimIndent()
                )
            }
        }
    }
}

fun <T> ResultSet.mapList(mapper: (ResultSet) -> T): List<T> {
    val list = mutableListOf<T>()
    while (next()) {
        list.add(mapper(this))
    }
    return list
}

data class ApplicationDatabaseConfig(
    val jdbcUrl: String,
    val user: String,
    val password: String,
    val maxPoolSize: Int = 5,
)
