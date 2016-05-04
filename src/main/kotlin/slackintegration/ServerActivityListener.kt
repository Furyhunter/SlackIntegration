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
            return name.split("_").map {it.capitalize()}.joinToString(" ")
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onJoin(event: PlayerJoinEvent) {
        plugin.sendNotificationToSlack("*${event.player.name}* joined the game.")
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onQuit(event: PlayerQuitEvent) {
        plugin.sendNotificationToSlack("*${event.player.name}* left the game.")
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onDeath(event: PlayerDeathEvent) {
        if (plugin.sendDeaths) {
            plugin.sendNotificationToSlack(event.deathMessage)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onAchievementGet(event: PlayerAchievementAwardedEvent) {
        if (plugin.sendAchievements) {
            plugin.sendNotificationToSlack("*${event.player.name}* earned the achievement *${normalizeName(event.achievement.toString())}*")
        }
    }
}