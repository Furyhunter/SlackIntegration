package slackintegration

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class SlackCommand(val plugin: Slack) : CommandExecutor {
    override fun onCommand(sender: CommandSender?, command: Command?, label: String?, args: Array<out String>?): Boolean {
        if (args == null) return false
        if (sender == null) return false
        if (command == null) return false
        if (label == null) return false

        if (args.isEmpty()) return false

        if (args[0].equals("format", ignoreCase = true)) {
            if (args.size > 1) {
                plugin.slackToServerFormat = args.joinToString(" ")
                plugin.saveConfig()
            }
        }
        when (args[0].toLowerCase()) {
            "format" -> {
                if (args.size > 1) with(plugin) {
                    slackToServerFormat = args.joinToString(" ")
                }
            }
            "reload" -> {
                plugin.reloadConfig()
                sender.sendMessage("${ChatColor.GRAY}[${plugin.name}] ${ChatColor.GREEN}reloaded")
                return true
            }
            "url" -> {
                if (args.size >= 2) with(plugin) {
                    webhookUrl = args[1]
                    saveConfig()
                }
                sender.sendMessage("Webhook URL: ${plugin.webhookUrl}")
                return true
            }
            "add", "remove" -> {
                sender.sendMessage("This command is unimplemented in the Kotlin port")
                return true
            }
            "channels" -> {
                sender.sendMessage(plugin.channels.toString())
            }
            "port" -> {
                if (args.size >= 2) {
                    try {
                        val n = args[1].toInt()
                        with(plugin) {
                            port = n
                            saveConfig()
                        }
                    } catch (e: NumberFormatException) {
                        sender.sendMessage("Port must be a number")
                        return true
                    }
                }
                sender.sendMessage("Port: ${plugin.port}")
                return true
            }
            "deaths" -> {
                if (args.size >= 2) {
                    val b = args[1].toBoolean()
                    with(plugin) {
                        sendDeaths = b
                        saveConfig()
                    }
                }
                sender.sendMessage("Deaths: ${plugin.sendDeaths}")
                return true
            }
            "achievements" -> {
                if (args.size >= 2) {
                    val b = args[1].toBoolean()
                    with(plugin) {
                        sendAchievements = b
                        saveConfig()
                    }
                }
                sender.sendMessage("Achievements: ${plugin.sendAchievements}")
                return true
            }
        }
        return false
    }
}