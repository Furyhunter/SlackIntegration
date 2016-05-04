package slackintegration

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.IOException
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Slack : JavaPlugin() {
    private var _enabled: Boolean = false

    private var slackHttpd: SlackHTTPD? = null

    var outExecutor: ExecutorService? = Executors.newSingleThreadExecutor()

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
        slackHttpd?.stop()
        outExecutor?.shutdown()

        if (webhookUrl == "no-url") {
            logger.severe("No URL specified! Go to the config and specify one, then run '/slack reload'")
            return
        }

        outExecutor = Executors.newSingleThreadExecutor()

        try {
            slackHttpd = SlackHTTPD(this, token, port)
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
}