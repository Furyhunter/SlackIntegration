package slackintegration

import java.io.DataOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class PostTask(val url: URL, val msg: OutgoingMessage) : Runnable {
    override fun run() {
        try {
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = 5000
            connection.useCaches = false
            connection.doInput = true
            connection.doOutput = true

            val payload = "payload=" + URLEncoder.encode(msg.payload, "UTF-8")

            val wr = DataOutputStream(
                    connection.outputStream)
            wr.writeBytes(payload)
            wr.flush()
            wr.close()

            connection.inputStream.close()
            connection.disconnect()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}