package slackintegration

import com.teej107.slack.Slack
import com.teej107.slack.SlackCommandSender
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
            return name.split("_").reduce { a, r ->
                a + r[0] + r.substring(1).toLowerCase() + " "
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onJoin(event: PlayerJoinEvent) {
        plugin.sendToSlack(null, event.joinMessage)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onQuit(event: PlayerQuitEvent) {
        plugin.sendToSlack(null, event.quitMessage)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onDeath(event: PlayerDeathEvent) {
        if (plugin.isSendDeaths) {
            plugin.sendToSlack(null, event.deathMessage)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onAchievementGet(event: PlayerAchievementAwardedEvent) {
        if (plugin.isSendAchievements) {
            plugin.sendToSlack(null,
                    "${event.player.name} has just earned the achievement [${normalizeName(event.achievement.toString())}]")

        }
    }
}