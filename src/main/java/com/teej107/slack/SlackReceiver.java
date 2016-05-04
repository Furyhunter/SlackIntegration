package com.teej107.slack;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * @author teej107
 * @since Sep 12, 2015
 */
public class SlackReceiver implements Runnable {
    private URL url;
    private ExecutorService service;
    private Queue<String> queue;

    public SlackReceiver(URL url) throws IOException {
        this.url = url;
        service = Executors.newSingleThreadExecutor();
        queue = new ConcurrentLinkedQueue<String>();
    }

    public void send(String s) {
        queue.add(s);
        service.submit(this);
    }

    @Override
    public void run() {
        String s = queue.poll();
        if (s == null)
            return;

        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            String payload = "payload="
                    + URLEncoder.encode(s, "UTF-8");

            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes(payload);
            wr.flush();
            wr.close();

            //Seem useless but it isn't :P
            connection.getInputStream().close();
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
