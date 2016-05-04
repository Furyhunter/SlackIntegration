package slackintegration

import com.teej107.slack.SlackReceiver
import com.teej107.slack.SlackSender
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin
import org.json.simple.JSONObject
import java.io.IOException
import java.net.URL

class Slack : JavaPlugin() {
    private var _enabled: Boolean = false
    private var slackSender: SlackSender? = null
    private var slackReceiver: SlackReceiver? = null

    private fun FileConfiguration.setSlackPluginDefaults() {
        addDefault("webhook-url", "no-url")
        addDefault("port", 25107)
        addDefault("token", "TOKENMISSING")
        addDefault("channels", emptyList<String>())
        addDefault("slack-to-server-format", "(%s) %s")
        addDefault("send-achievements", true)
        addDefault("send-deaths", true)
        options().copyDefaults(true)
        saveConfig()
    }

    private fun load() {
        slackSender?.setEnabled(false)
        if (webhookUrl == "no-url") {
            logger.severe("No URL specified! Go to the config and specify one, then run '/slack reload'")
            return
        }
        try {
            slackReceiver = SlackReceiver(URL(webhookUrl))
        } catch (e: IOException) {
            logger.severe("Unable to initialize slack receiver, disabling plugin\n${e.toString()}")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }
        try {
            slackSender = SlackSender(this, port, token, slackToServerFormat)
            slackSender?.setEnabled(true)
        } catch (e: IOException) {
            logger.severe("Unable to initialize slack sender, disabling plugin\n${e.toString()}")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }
    }

    override fun onEnable() {
        getConfig().setSlackPluginDefaults()
        load()

        val pm = Bukkit.getPluginManager()
        pm.registerEvents(AsyncPlayerChatListener(this), this)
        pm.registerEvents(ServerActivityListener(this), this)
        getCommand("slack").executor = SlackCommand(this)
        _enabled = true
    }

    override fun onDisable() {
        _enabled = false
    }

    override fun reloadConfig() {
        super.reloadConfig()
        if (_enabled) {
            load()
        }
    }

    var sendAchievements: Boolean
        get() = config.getBoolean("send-achievements")
        set(value) = config.set("send-achievements", value)

    var sendDeaths: Boolean
        get() = config.getBoolean("send-deaths")
        set(value) = config.set("send-deaths", value)

    var webhookUrl: String
        get() = config.getString("webhook-url")
        set(value) = config.set("webhook-url", value)

    var channels: List<String>
        get() = config.getStringList("channels")
        set(value) = config.set("channels", value)

    var port: Int
        get() = config.getInt("port")
        set(value) = config.set("port", value)

    var token: String
        get() = config.getString("token")
        set(value) = config.set("token", value)

    var slackToServerFormat: String
        get() = config.getString("slack-to-server-format")
        set(value) = config.set("slack-to-server-format", value)

    fun sendToSlack(sender: CommandSender, text: String) {
        val json = JSONObject()
        with(json) {
            put("text", ChatColor.stripColor(text))
            put("username", sender.name)
            put("icon_url", "https://minotar.net/avatar/${sender.name}.png")
        }
        for (c in channels) {
            json.put("channel", c)
            slackReceiver?.send(json.toJSONString())
        }
    }

    fun sendNotificationToSlack(text: String) {
        val json = JSONObject()
        with(json) {
            put("text", text)
            put("username", "[Server]")
        }
        for (c in channels) {
            json.put("channel", c)
            slackReceiver?.send(json.toJSONString())
        }
    }
}