package `in`.uprep.app.ui.common

fun relativeTime(epochMillis: Long): String {
    if (epochMillis <= 0) return ""
    val seconds = (System.currentTimeMillis() - epochMillis) / 1000
    return when {
        seconds < 60 -> "just now"
        seconds < 3600 -> "${seconds / 60}m ago"
        seconds < 86400 -> "${seconds / 3600}h ago"
        seconds < 86400 * 30 -> "${seconds / 86400}d ago"
        seconds < 86400 * 365 -> "${seconds / (86400 * 30)}mo ago"
        else -> "${seconds / (86400 * 365)}y ago"
    }
}
