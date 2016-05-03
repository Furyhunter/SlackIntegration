package com.teej107.slack;

import org.bukkit.event.*;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * @author teej107
 * @since Sep 12, 2015
 */
public class AsyncPlayerChatListener implements Listener {
    private Slack plugin;

    public AsyncPlayerChatListener(Slack plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAsyncChat(AsyncPlayerChatEvent event) {
        plugin.sendToSlack(event.getPlayer(), event.getMessage());
    }
}
