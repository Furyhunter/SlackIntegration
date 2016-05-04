package slackintegration

import com.teej107.slack.Slack
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

class AsyncPlayerChatListener(private val plugin: Slack) : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onAsyncChat(event: AsyncPlayerChatEvent) {
        plugin.sendToSlack(event.player, event.message)
    }
}