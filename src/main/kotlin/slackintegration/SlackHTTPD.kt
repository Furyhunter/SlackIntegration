package slackintegration

import fi.iki.elonen.NanoHTTPD
import org.bukkit.scheduler.BukkitRunnable

class SlackHTTPD(val plugin: Slack) : NanoHTTPD(plugin.port) {

    override fun serve(session: IHTTPSession?): Response? {
        super.serve(session)

        if (session == null) return null

        if (session.method != Method.POST) {
            return errorResponse("Invalid method")
        }

        val formMap = session.parms

        val rToken = formMap["token"]
        val username = formMap["user_name"]
        val text = formMap["text"]

        if (rToken !in plugin.tokens) {
            plugin.logger.warning("Incoming tried to use token $rToken but it was invalid;\n\nrequest form map is $formMap")
            return errorResponse("Invalid token")
        }

        if (username == "slackbot" || username.isNullOrEmpty()) {
            // short circuit, we don't need to do anything with messages from slackbot
            return emptyResponse()
        }

        if (text.isNullOrEmpty()) {
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

    fun emptyResponse() = newFixedLengthResponse(Response.Status.OK, "application/json", "{}")
    fun errorResponse(text: String) = newFixedLengthResponse(Response.Status.FORBIDDEN, "text/html", text)
}