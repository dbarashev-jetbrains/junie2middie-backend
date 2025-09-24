package org.jetbrains.edu.junie2middie

import java.sql.Timestamp
import java.time.LocalDateTime

class Repository {
    fun listContacts(): List<Contact> = Database.getConnection().use { conn ->
        conn.createStatement().use { st ->
            val rs = st.executeQuery("SELECT id, name, status, avatar_url FROM contacts ORDER BY id")
            rs.mapList { r ->
                Contact(
                    id = r.getLong("id"),
                    name = r.getString("name"),
                    status = r.getString("status"),
                    avatarUrl = r.getString("avatar_url"),
                )
            }
        }
    }

    fun createContact(contact: Contact): Contact = Database.getConnection().use { conn ->
        if (contact.id > 0) {
            conn.prepareStatement(
                "INSERT INTO contacts (id, name, status, avatar_url) VALUES (?, ?, ?, ?) ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, status = EXCLUDED.status, avatar_url = EXCLUDED.avatar_url RETURNING id",
            ).use { ps ->
                ps.setLong(1, contact.id)
                ps.setString(2, contact.name)
                ps.setString(3, contact.status)
                ps.setString(4, contact.avatarUrl)
                val rs = ps.executeQuery()
                rs.next()
                val id = rs.getLong(1)
                contact.copy(id = id)
            }
        } else {
            conn.prepareStatement(
                "INSERT INTO contacts (name, status, avatar_url) VALUES (?, ?, ?) RETURNING id",
            ).use { ps ->
                ps.setString(1, contact.name)
                ps.setString(2, contact.status)
                ps.setString(3, contact.avatarUrl)
                val rs = ps.executeQuery()
                rs.next()
                val id = rs.getLong(1)
                contact.copy(id = id)
            }
        }
    }

    fun getMessagesWithContact(contactId: Long, meId: Long = 0L): List<Message> = Database.getConnection().use { conn ->
        conn.prepareStatement(
            """
            SELECT id, content, sender_id, receiver_id, timestamp
            FROM messages
            WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)
            ORDER BY timestamp
            """.trimIndent()
        ).use { ps ->
            ps.setLong(1, contactId)
            ps.setLong(2, meId)
            ps.setLong(3, meId)
            ps.setLong(4, contactId)
            val rs = ps.executeQuery()
            rs.mapList { r ->
                val senderId = r.getLong("sender_id")
                Message(
                    id = r.getLong("id"),
                    content = r.getString("content"),
                    senderId = senderId,
                    receiverId = r.getLong("receiver_id"),
                    timestamp = r.getString("timestamp")
                )
            }
        }
    }

    fun createMessage(message: Message): Message = Database.getConnection().use { conn ->
        conn.prepareStatement(
            "INSERT INTO messages (content, sender_id, receiver_id, timestamp) VALUES (?, ?, ?, ?) RETURNING id, timestamp",
        ).use { ps ->
            ps.setString(1, message.content)
            ps.setLong(2, message.senderId)
            ps.setLong(3, message.receiverId)
            // Accept both ISO strings and fallback to now if parse fails
            val tsString = try {
                LocalDateTime.parse(message.timestamp).toString()
            } catch (e: Exception) {
                LocalDateTime.now().toString()
            }
            ps.setString(4, tsString)
            val rs = ps.executeQuery()
            rs.next()
            val id = rs.getLong("id")
            val actualTs = rs.getString("timestamp")
            message.copy(id = id, timestamp = actualTs)
        }
    }
}
