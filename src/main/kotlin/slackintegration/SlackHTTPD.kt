package slackintegration

import com.google.common.io.ByteStreams
import fi.iki.elonen.NanoHTTPD
import org.bukkit.scheduler.BukkitRunnable
import java.net.URLDecoder
import java.nio.charset.Charset

class SlackHTTPD(val plugin: Slack) : NanoHTTPD(plugin.port) {
    companion object {
        fun formStringToMap(form: String): Map<String, String> {
            val formParameters = form.split("&")
            val formMap: Map<String, String> = with(mutableMapOf<String, String>()) {
                for (p in formParameters) {
                    val s = p.split("=").map{it.trim()}
                    if (s.size == 2) put(s[0], URLDecoder.decode(s[1], "UTF-8"))
                }
                this
            }
            return formMap
        }
    }
    override fun serve(session: IHTTPSession?): Response? {
        if (session == null) return null

        if (session.method != Method.POST) {
            return errorResponse("Invalid method")
        }

        val body = String(ByteStreams.toByteArray(session.inputStream), Charset.forName("UTF-8"))
        val formMap = formStringToMap(body)

        val rToken = formMap["token"]
        val username = formMap["user_name"]
        val text = formMap["text"]

        if (rToken !in plugin.tokens) {
            return errorResponse("Invalid token")
        }

        if (username == "slackbot" || username.isNullOrEmpty()) {
            // short circuit, we don't need to do anything with messages from slackbot
            plugin.logger.info("Ignoring message from slackbot or empty username")
            return emptyResponse()
        }

        if (text.isNullOrEmpty()) {
            plugin.logger.info("Ignoring empty text")
            return emptyResponse()
        }

        // val formattedText = ???

        val inc = IncomingMessage.formToMessage(formMap)
        val task = object : BukkitRunnable() {
            override fun run() {
                plugin.handleIncomingMessage(inc)
            }
        }
        task.runTask(plugin)

        return emptyResponse()
    }

    fun emptyResponse() = newFixedLengthResponse(Response.Status.OK, "text/javascript", "{}")
    fun errorResponse(text: String) = newFixedLengthResponse(Response.Status.FORBIDDEN, "text/html", text)
}