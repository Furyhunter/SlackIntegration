package slackintegration

interface OutgoingMessage {
    val payload: String
}

data class UserChatMessage(val user: String, val text: String, val channel: String) : OutgoingMessage {
    override val payload: String
        get() = """{"username": "$user", "text": "$text", "channel": "$channel"}"""
}

data class EventMessage(val text: String, val channel: String) : OutgoingMessage {
    override val payload: String
        get() = """{"text": "$text", "channel": "$channel", "username": "[Server]"}"""
}