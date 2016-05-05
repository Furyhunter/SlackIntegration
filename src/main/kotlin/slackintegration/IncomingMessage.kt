package slackintegration

import java.net.URL

interface IncomingMessage {
    companion object {
        fun formToMessage(form: Map<String, String>): IncomingMessage {
            if (form["command"] == "/mc") {
                val args = form["text"]?.split(" ")
                val urlText = form["response_url"]
                val url = if (urlText == null) null else URL(urlText)
                val username = form["user_name"]

                if (args != null && username != null && url != null)
                    return CommandMessage(username, args[0], args.subList(1, args.size).joinToString(" "), url)
            } else {
                val username = form["user_name"]
                val text = form["text"]
                if (username != null && text != null)
                    return ChatMessage(username, text)
            }

            return UnknownMessage(form)
        }
    }
}

data class ChatMessage(val username: String, val text: String) : IncomingMessage
data class CommandMessage(val username: String, val subcmd: String, val subText: String, val responseUrl: URL) : IncomingMessage
data class UnknownMessage(val form: Map<String, String>) : IncomingMessage