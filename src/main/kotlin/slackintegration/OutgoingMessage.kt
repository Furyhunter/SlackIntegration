package slackintegration

interface OutgoingMessage {
    val payload: String
}

data class UserChatMessage(val user: String, val text: String, val channel: String) : OutgoingMessage {
    override val payload: String
        get() = """{"username": "$user", "text": "$text", "channel": "$channel", "icon_url": "https://minotar.net/avatar/$user.png"}"""
}

data class EventMessage(val text: String, val channel: String) : OutgoingMessage {
    override val payload: String
        get() = """{"text": "$text", "channel": "$channel", "username": "[Server]"}"""
}

data class MapMessage(val map: Map<Any, Any>) : OutgoingMessage {
    override val payload: String
        get() = map.toJSONString()
}