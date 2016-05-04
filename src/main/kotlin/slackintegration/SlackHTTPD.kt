package slackintegration

import com.google.common.io.ByteStreams
import fi.iki.elonen.NanoHTTPD
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import org.unbescape.html.HtmlEscape
import java.net.URLDecoder
import java.nio.charset.Charset

class SlackHTTPD(val plugin: Slack, val token: String, val port: Int) : NanoHTTPD(port) {
    override fun serve(session: IHTTPSession?): Response? {
        if (session == null) return null

        if (session.method != Method.POST) {
            return errorResponse("Invalid method")
        }

        val body = String(ByteStreams.toByteArray(session.inputStream), Charset.forName("UTF-8"))
        val formParameters = body.split("&")
        val formMap: Map<String, String> = with(mutableMapOf<String, String>()) {
            for (p in formParameters) {
                val s = p.split("=")
                if (s.size == 2) put(s[0], s[1])
            }
            this
        }

        val rToken = formMap.get("token")
        val username = formMap.get("user_name")
        val text = formMap.get("text")

        if (rToken != token) {
            return errorResponse("Invalid token")
        }
        if (username == "slackbot") {
            // short circuit, we don't need to do anything with messages from slackbot
            return emptyResponse
        }
        if (username == null) {
            return emptyResponse
        }

        if (text == null) {
            return emptyResponse
        }

        val escapedText = HtmlEscape.unescapeHtml(URLDecoder.decode(text, "UTF-8"))
        val fromSlackText = plugin.slackToServerFormat.format(username, escapedText)
        // val formattedText = ???

        val task = object : BukkitRunnable() {
            override fun run() {
                Bukkit.broadcastMessage(fromSlackText)
            }
        }

        task.runTask(plugin)

        return emptyResponse
    }

    val emptyResponse = newFixedLengthResponse(Response.Status.OK, "text/javascript", "{}")
    fun errorResponse(text: String) = newFixedLengthResponse(Response.Status.FORBIDDEN, "text/html", text)
}