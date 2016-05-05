package slackintegration

import org.junit.Test
import org.junit.Assert.*

class SlackHTTPDTest {
    @Test fun testFormStringToMap() {
        val formString = "user_name=test&\n" +
                "password=a%20b"
        val map = SlackHTTPD.formStringToMap(formString)

        assertEquals(mapOf("user_name" to "test", "password" to "a b"), map)
    }
}