package slackintegration

import java.io.DataOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class PostTask(val url: URL, val msg: OutgoingMessage) : Runnable {
    override fun run() {
        try {
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = 5000
            connection.useCaches = false
            connection.doInput = true
            connection.doOutput = true
            connection.addRequestProperty("content-type", "application/json")

            val wr = DataOutputStream(
                    connection.outputStream)
            wr.writeUTF(msg.payload)
            wr.flush()
            wr.close()

            connection.inputStream.close()
            connection.disconnect()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}