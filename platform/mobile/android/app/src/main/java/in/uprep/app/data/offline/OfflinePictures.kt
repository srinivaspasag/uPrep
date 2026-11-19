package `in`.uprep.app.data.offline

import android.content.Context
import `in`.uprep.app.data.net.NetworkConfig
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

// Pictures inside question text/options (tables and diagrams from Bulk
// Import, or added in the question editor) are written as
// `![picture](/uploads/<file>)` — the website renders them with
// components/MathText. For offline tests they're downloaded next to the
// cached questions when the test is downloaded, and NativeTestScreen shows
// them from disk. Only this site's /uploads/<safe name> files are fetched.
object OfflinePictures {
    val TOKEN = Regex("""!\[[^\]\n]*\]\((/uploads/[A-Za-z0-9._-]+)\)""")

    fun urlsIn(text: String): List<String> = TOKEN.findAll(text).map { it.groupValues[1] }.toList()

    fun fileFor(context: Context, url: String): File =
        File(File(context.filesDir, "offline-pictures"), url.substringAfterLast('/'))

    // Best effort: a picture that fails to download is shown as a note
    // instead, the test itself is still usable.
    fun cache(context: Context, texts: List<String>) {
        val dir = File(context.filesDir, "offline-pictures").apply { mkdirs() }
        val base = NetworkConfig.BASE_URL.trimEnd('/')
        for (url in texts.flatMap { urlsIn(it) }.distinct()) {
            val target = File(dir, url.substringAfterLast('/'))
            if (target.exists() && target.length() > 0) continue
            runCatching {
                val conn = URL(base + url).openConnection() as HttpURLConnection
                conn.connectTimeout = 10_000
                conn.readTimeout = 20_000
                try {
                    if (conn.responseCode != 200) return@runCatching
                    val tmp = File(dir, target.name + ".part")
                    conn.inputStream.use { input -> tmp.outputStream().use { input.copyTo(it) } }
                    tmp.renameTo(target)
                } finally {
                    conn.disconnect()
                }
            }
        }
    }
}
