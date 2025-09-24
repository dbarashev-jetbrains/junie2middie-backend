package org.jetbrains.edu.junie2middie

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.long
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object FailureConfig {
    @Volatile var failGetProbability: Double = 0.0
    @Volatile var responseDelayMs: Long = 0
    fun shouldFailGet(): Boolean = (failGetProbability > 0.0) && (kotlin.random.Random.nextDouble() < failGetProbability)
}

private class AppCli : CliktCommand(name = "j2m-backend", printHelpOnEmptyArgs = false) {
    private val failProb by option("--fail-prob", help = "Probability [0..1] to fail GET requests").double().default(0.0)
    private val delayMs by option("--delay-ms", help = "Delay in milliseconds applied to all GET responses").long().default(0L)

    override fun run() {
        require(failProb in 0.0..1.0) { "--fail-prob must be between 0.0 and 1.0" }
        require(delayMs >= 0) { "--delay-ms must be non-negative" }
        FailureConfig.failGetProbability = failProb
        FailureConfig.responseDelayMs = delayMs
        io.ktor.server.netty.EngineMain.main(emptyArray())
    }
}

fun main(args: Array<String>) = AppCli().main(args)

fun Application.module() {
    // Initialize database from configuration
    val cfg = environment.config
    val db = cfg.config("ktor.database")
    Database.init(
        ApplicationDatabaseConfig(
            jdbcUrl = db.property("jdbcUrl").getString(),
            user = db.property("user").getString(),
            password = db.property("password").getString(),
            maxPoolSize = db.propertyOrNull("maxPoolSize")?.getString()?.toIntOrNull() ?: 5
        )
    )

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    configureRouting()
}
