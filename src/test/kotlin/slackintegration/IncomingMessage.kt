package slackintegration

import org.junit.Test
import org.junit.Assert.*
import java.net.URL

class IncomingMessageTest {
    @Test fun testFormToMessage() {
        val map = mapOf(
                "text" to "hello world",
                "user_name" to "junituser"
        )
        val msg = IncomingMessage.formToMessage(map)
        assertEquals(ChatMessage("junituser", "hello world"), msg)

        val map2 = mapOf(
                "text" to "hello world",
                "user_name" to "junituser",
                "command" to "/mc",
                "response_url" to "http://example.com/"
        )
        val msg2 = IncomingMessage.formToMessage(map2)
        assertEquals(CommandMessage("junituser", "hello", "world", URL("http://example.com/")), msg2)

        val map3 = emptyMap<String, String>()
        val msg3 = IncomingMessage.formToMessage(map3)
        assertEquals(null, msg3)
    }
}