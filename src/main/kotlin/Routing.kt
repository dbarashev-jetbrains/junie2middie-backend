package org.jetbrains.edu.junie2middie

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay

fun Application.configureRouting() {
    val repo = Repository()

    routing {
        get("/") {
            if (FailureConfig.responseDelayMs > 0) delay(FailureConfig.responseDelayMs)
            if (FailureConfig.shouldFailGet()) throw RuntimeException("Simulated GET failure")
            call.respondText("OK")
        }
        // 1. GET /contacts
        get("/contacts") {
            if (FailureConfig.responseDelayMs > 0) delay(FailureConfig.responseDelayMs)
            if (FailureConfig.shouldFailGet()) throw RuntimeException("Simulated GET failure")
            val contacts = repo.listContacts()
            call.respond(ContactsResponse(contacts))
        }
        // 2. GET /contact/{id}/messages
        get("/contact/{person1id}/messages/{person2id}") {
            if (FailureConfig.responseDelayMs > 0) delay(FailureConfig.responseDelayMs)
            if (FailureConfig.shouldFailGet()) throw RuntimeException("Simulated GET failure")
            val id = call.parameters["person1id"]?.toLongOrNull()
            if (id == null) {
                call.respondText("Invalid contact id", status = io.ktor.http.HttpStatusCode.BadRequest)
                return@get
            }
            val id2 = call.parameters["person2id"]?.toLongOrNull()
            if (id2 == null) {
                call.respondText("Invalid contact id", status = io.ktor.http.HttpStatusCode.BadRequest)
                return@get
            }
            val messages = repo.getMessagesWithContact(id, id2)
            call.respond(MessagesResponse(messages))
        }
        // 3. POST /message
        post("/message") {
            val payload = call.receive<Message>()
            val created = repo.createMessage(payload)
            call.respond(created)
        }
        // 4. POST /contact
        post("/contact") {
            val payload = call.receive<Contact>()
            val created = repo.createContact(payload)
            call.respond(created)
        }
    }
}
