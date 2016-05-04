package slackintegration

import org.json.simple.JSONObject

fun <K, V> Map<K, V>.toJSONString(): String = JSONObject(this).toJSONString()