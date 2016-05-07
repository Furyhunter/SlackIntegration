package slackintegration

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin
import org.unbescape.html.HtmlEscape
import java.io.IOException
import java.net.URL
import java.net.URLDecoder
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Slack : JavaPlugin() {
    companion object {
        const val OUTGOING_WEBHOOK_URL = "outgoing-webhook-url"
        const val PORT = "port"
        const val INCOMING_TOKENS = "incoming-tokens"
        const val CHANNELS = "channels"
        const val SLACK_TO_SERVER_FORMAT = "slack-to-server-format"
        const val SEND_ACHIEVEMENTS = "send-achievements"
        const val SEND_DEATHS = "send-deaths"
        const val ALLOW_WHITELIST_COMMAND = "allow-whitelist-command"
    }

    private var _enabled: Boolean = false

    private var slackHttpd: SlackHTTPD? = null

    var outExecutor: ExecutorService? = Executors.newSingleThreadExecutor()

    private fun FileConfiguration.setSlackPluginDefaults() {
        addDefault(OUTGOING_WEBHOOK_URL, "no-url")
        addDefault(PORT, 25107)
        addDefault(INCOMING_TOKENS, listOf("TOKEN1", "TOKEN2"))
        addDefault(CHANNELS, emptyList<String>())
        addDefault(SLACK_TO_SERVER_FORMAT, "(%s) %s")
        addDefault(SEND_ACHIEVEMENTS, true)
        addDefault(SEND_DEATHS, true)
        addDefault(ALLOW_WHITELIST_COMMAND, true)
        options().copyDefaults(true)
        saveConfig()
    }

    private fun load() {
        slackHttpd?.stop()
        outExecutor?.shutdown()

        if (webhookUrl == "no-url") {
            logger.severe("No URL specified! Go to the config and specify one, then run '/slack reload'")
            return
        }

        outExecutor = Executors.newSingleThreadExecutor()

        try {
            slackHttpd = SlackHTTPD(this)
            slackHttpd?.start()
        } catch (e: IOException) {
            logger.severe("Unable to initialize slack receiver http server\n${e.toString()}")
            return
        }
    }

    override fun onEnable() {
        config.setSlackPluginDefaults()
        load()

        val pm = Bukkit.getPluginManager()
        pm.registerEvents(AsyncPlayerChatListener(this), this)
        pm.registerEvents(ServerActivityListener(this), this)
        getCommand("slack").executor = SlackCommand(this)
        _enabled = true
    }

    override fun onDisable() {
        _enabled = false
        slackHttpd?.stop()
        outExecutor?.shutdown()
    }

    override fun reloadConfig() {
        super.reloadConfig()
        if (_enabled) {
            load()
        }
    }

    var sendAchievements: Boolean
        get() = config.getBoolean(SEND_ACHIEVEMENTS)
        set(value) = config.set(SEND_ACHIEVEMENTS, value)

    var sendDeaths: Boolean
        get() = config.getBoolean(SEND_DEATHS)
        set(value) = config.set(SEND_DEATHS, value)

    var webhookUrl: String
        get() = config.getString(OUTGOING_WEBHOOK_URL)
        set(value) = config.set(OUTGOING_WEBHOOK_URL, value)

    var channels: List<String>
        get() = config.getStringList(CHANNELS)
        set(value) = config.set(CHANNELS, value)

    var port: Int
        get() = config.getInt(PORT)
        set(value) = config.set(PORT, value)

    var tokens: List<String>
        get() = config.getStringList(INCOMING_TOKENS)
        set(value) = config.set(INCOMING_TOKENS, value)

    var slackToServerFormat: String
        get() = config.getString(SLACK_TO_SERVER_FORMAT)
        set(value) = config.set(SLACK_TO_SERVER_FORMAT, value)

    var allowWhitelistCommand: Boolean
        get() = config.getBoolean(ALLOW_WHITELIST_COMMAND)
        set(value) = config.set(ALLOW_WHITELIST_COMMAND, value)

    fun sendUserChat(sender: CommandSender, text: String) {
        for (c in channels) {
            val msg = UserChatMessage(sender.name, ChatColor.stripColor(text), c)
            val task = PostTask(URL(webhookUrl), msg)
            outExecutor?.submit(task)
        }
    }

    fun sendNotification(text: String) {
        for (c in channels) {
            val msg = EventMessage(text, c)
            val task = PostTask(URL(webhookUrl), msg)
            outExecutor?.submit(task)
        }
    }

    fun handleIncomingMessage(m: IncomingMessage) {
        when (m) {
            is ChatMessage -> {
                val escapedText = HtmlEscape.unescapeHtml(URLDecoder.decode(m.text, "UTF-8"))
                val fromSlackText = slackToServerFormat.format(m.username, escapedText)
                Bukkit.broadcastMessage(fromSlackText)
            }
            is CommandMessage -> {
                when (m.subcmd) {
                    "whitelist" -> {
                        handleWhitelistCommand(m)
                    }
                    "players" -> {
                        val playerNames = ArrayList(Bukkit.getServer().onlinePlayers).map {"*${it.displayName}*"}
                        val playerNameString = playerNames.joinToString(", ")
                        val msg = "There are ${playerNames.size} players online. Names: $playerNameString"
                        outExecutor?.submit(PostTask(m.responseUrl, MapMessage(mapOf(
                                "text" to msg
                        ))))
                    }
                    "hello" -> {
                        outExecutor?.submit(PostTask(m.responseUrl, MapMessage(mapOf(
                                "text" to "Hello world!"
                        ))))
                    }
                }
            }
            is UnknownMessage -> {
                logger.severe("Unknown slack message received: $m")
                Bukkit.broadcastMessage("(slack) I received an unknown message; please check the server log for info")
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun handleWhitelistCommand(m: CommandMessage) {
        if (!allowWhitelistCommand) {
            outExecutor?.submit(PostTask(m.responseUrl, MapMessage(mapOf(
                    "text" to "Whitelisting from Slack is disabled. Please contact a server administrator."
            ))))
            return
        }

        // TODO replace with explicit UUID retrieval
        val op: OfflinePlayer? = Bukkit.getOfflinePlayer(m.subText)
        if (op == null) {
            outExecutor?.submit(PostTask(m.responseUrl, MapMessage(mapOf(
                    "text" to "The username \"${m.subText}\" does not exist."
            ))))
            return
        }
        val wl = op.isWhitelisted
        if (!wl) {
            op.isWhitelisted = true
            outExecutor?.submit(PostTask(m.responseUrl, MapMessage(mapOf(
                    "text" to "The Minecraft user \"${op.name}\" has been whitelisted."
            ))))
        } else {
            outExecutor?.submit(PostTask(m.responseUrl, MapMessage(mapOf(
                    "text" to "The username \"${op.name}\" was already whitelisted."
            ))))
        }
    }
}