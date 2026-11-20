package `in`.uprep.app.data.api

import org.json.JSONObject
import retrofit2.Response

// The server's own error text from a failed call ({"error": "..."}), e.g.
// "Aira is closed while your test … is in progress" — Retrofit only parses
// the body of successful responses, so read errorBody here.
fun Response<*>.serverError(): String? =
    runCatching { errorBody()?.string()?.let { JSONObject(it).optString("error").takeIf { s -> s.isNotBlank() } } }.getOrNull()
