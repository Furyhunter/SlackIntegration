package com.teej107.slack;

import org.bukkit.event.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

import java.util.regex.Pattern;

/**
 * @author teej107
 * @since Sep 12, 2015
 */
public class ServerActivityListener implements Listener {
    private Slack plugin;

    public ServerActivityListener(Slack plugin) {
        this.plugin = plugin;
    }

    private static String normalize(String string) {
        StringBuilder sb = new StringBuilder();
        for (String s : string.split(Pattern.quote("_"))) {
            sb.append(s.charAt(0)).append(s.substring(1).toLowerCase()).append(" ");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        plugin.sendToSlack(SlackCommandSender.getInstance(), event.getJoinMessage());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent event) {
        plugin.sendToSlack(SlackCommandSender.getInstance(), event.getQuitMessage());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        if (plugin.isSendDeaths()) {
            plugin.sendToSlack(SlackCommandSender.getInstance(), event.getDeathMessage());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAchievementGet(PlayerAchievementAwardedEvent event) {
        if (plugin.isSendAchievements()) {
            plugin.sendToSlack(SlackCommandSender.getInstance(),
                    event.getPlayer().getName() + " has just earned the achievement [" + normalize(event.getAchievement().toString())
                            + "]");
        }
    }
}
