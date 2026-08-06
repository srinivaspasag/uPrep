package `in`.uprep.app.data.session

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

// Salted PBKDF2 hash for the offline-login fallback (see SessionStore /
// LoginViewModel) — lets a previously-logged-in user get back in with no
// connectivity, without ever storing the plaintext password.
object OfflineAuth {
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH = 256

    fun randomSaltHex(): String {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) }
    }

    fun hash(password: String, saltHex: String): String {
        val salt = saltHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec)
        return key.encoded.joinToString("") { "%02x".format(it) }
    }
}
