package slackintegration

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerAchievementAwardedEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent


class ServerActivityListener(val plugin: Slack) : Listener {
    companion object {
        fun normalizeName(name: String): String {
            return name.split("_").map {it.toLowerCase().capitalize()}.joinToString(" ")
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onJoin(event: PlayerJoinEvent) {
        plugin.sendNotification("*${event.player.name}* joined the game.")
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onQuit(event: PlayerQuitEvent) {
        plugin.sendNotification("*${event.player.name}* left the game.")
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onDeath(event: PlayerDeathEvent) {
        if (plugin.sendDeaths) {
            plugin.sendNotification(event.deathMessage)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onAchievementGet(event: PlayerAchievementAwardedEvent) {
        if (plugin.sendAchievements) {
            plugin.sendNotification("*${event.player.name}* earned the achievement _${normalizeName(event.achievement.toString())}_")
        }
    }
}