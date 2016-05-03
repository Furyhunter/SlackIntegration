package com.teej107.slack;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.*;

import java.util.List;

/**
 * @author teej107
 * @since Sep 12, 2015
 */
public class SlackCommand implements CommandExecutor {
    private Slack plugin;

    public SlackCommand(Slack plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 0)
            return false;

        if (strings[0].equalsIgnoreCase("format")) {
            if (strings.length > 1) {
                plugin.setSlackToServerFormat(StringUtils.join(strings, " ", 1, strings.length));
                plugin.saveConfig();
            }
            commandSender.sendMessage("Format: " + plugin.getSlackToServerFormat());
            return true;
        }

        if (strings.length > 2)
            return false;

        if (strings[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            commandSender.sendMessage(ChatColor.GRAY + "[" + plugin.getName() + "] " + ChatColor.GREEN + "reloaded");
            return true;
        }

        if (strings[0].equalsIgnoreCase("url")) {
            if (strings.length == 2) {
                plugin.setWebhookUrl(strings[1]);
                plugin.saveConfig();
            }
            commandSender.sendMessage("Webhook URL: " + plugin.getWebhookUrl());
            return true;
        }
        if (strings[0].equalsIgnoreCase("token")) {
            if (strings.length == 2) {
                plugin.setToken(strings[1]);
                plugin.saveConfig();
            }
            commandSender.sendMessage("Token: " + plugin.getToken());
            return true;
        }
        if ((strings[0].equalsIgnoreCase("add") || strings[0].equalsIgnoreCase("remove")) && strings.length == 2) {
            List<String> list = plugin.getChannels();
            if (strings[0].equalsIgnoreCase("add")) {
                list.add(strings[1]);
                commandSender.sendMessage("Added channel: " + strings[1]);
            } else {
                if (list.remove(strings[1])) {
                    commandSender.sendMessage("Removed channel: " + strings[1]);
                } else {
                    commandSender.sendMessage("The channel \'" + strings[1] + "\' doesn't exist");
                }
            }
            plugin.setChannels(list);
            plugin.saveConfig();
            return true;
        }
        if (strings[0].equalsIgnoreCase("channels")) {
            List<String> list = plugin.getChannels();
            commandSender.sendMessage(list.toArray(new String[list.size()]));
            return true;
        }
        if (strings[0].equalsIgnoreCase("port")) {
            if (strings.length == 2) {
                try {
                    int n = Integer.parseInt(strings[1]);
                    plugin.setPort(n);
                    plugin.saveConfig();
                } catch (NumberFormatException e) {
                    commandSender.sendMessage("Port must be a number");
                    return true;
                }
            }
            commandSender.sendMessage("Port: " + plugin.getPort());
            return true;
        }
        if (strings[0].equalsIgnoreCase("deaths")) {
            if (strings.length == 2) {
                Boolean b = getBoolean(strings[1]);
                if (b == null)
                    return false;
                plugin.setSendDeaths(b);
                plugin.saveConfig();
            }
            commandSender.sendMessage("Deaths: " + plugin.isSendDeaths());
            return true;
        }
        if (strings[0].equalsIgnoreCase("achievements")) {
            if (strings.length == 2) {
                Boolean b = getBoolean(strings[1]);
                if (b == null)
                    return false;
                plugin.setSendAchievements(b);
                plugin.saveConfig();
            }
            commandSender.sendMessage("Achievements: " + plugin.isSendAchievements());
            return true;
        }
        return false;
    }

    private Boolean getBoolean(String s) {
        if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false"))
            return Boolean.valueOf(s);
        return null;
    }
}
