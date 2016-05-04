package com.teej107.slack;

import com.google.common.io.ByteStreams;
import com.sun.net.httpserver.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.unbescape.html.HtmlEscape;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * @author teej107
 * @since Sep 12, 2015
 */
public class SlackSender implements HttpHandler {
    private Plugin plugin;
    private final String token, format;
    private HttpServer server;

    public SlackSender(Plugin plugin, int port, String token, String format) throws IOException {
        this.plugin = plugin;
        this.token = token;
        this.format = format;
        InetSocketAddress address = new InetSocketAddress(Bukkit.getIp(), port);
        server = HttpServer.create(address, 0);
        server.createContext("/", this);
        server.setExecutor(Executors.newCachedThreadPool());
    }

    public void setEnabled(boolean enabled) {
        if (enabled) {
            server.start();
        } else {
            server.stop(0);
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        if (requestMethod.equals("POST")) {
            handleSlackMessage(exchange);
        }
    }

    private void handleSlackMessage(HttpExchange exchange) throws IOException {
        byte[] bytes = ByteStreams.toByteArray(exchange.getRequestBody());
        String fromBytes = new String(bytes, "UTF-8");
        String[] contents = fromBytes.split(Pattern.quote("&"));
        HashMap<String, String> map = new HashMap<String, String>();
        for (String s : contents) {
            String[] array = s.split(Pattern.quote("="));
            if (array.length == 2) {
                map.put(array[0], array[1]);
            }
        }
        String token = map.get("token");
        if (token == null || !this.token.equals(token))
            return;

        String username = map.get("user_name");
        if (username != null && username.equals("slackbot"))
            return;

        String text = map.get("text");
        if (text == null)
            return;
        else {
            text = HtmlEscape.unescapeHtml(URLDecoder.decode(text, "UTF-8"));
        }

        exchange.sendResponseHeaders(200, 0);
        PrintWriter w = new PrintWriter(exchange.getResponseBody());
        w.println("{}");
        w.flush();
        w.close();

        exchange.close();

        final String broadcast = String.format(ChatColor.translateAlternateColorCodes('&', format), username, text);
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage(broadcast);
            }
        }.runTask(plugin);
    }
}
