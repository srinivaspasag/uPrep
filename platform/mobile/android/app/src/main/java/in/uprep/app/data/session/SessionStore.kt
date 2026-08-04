package `in`.uprep.app.data.session

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "uprep_session")

data class UserSession(
    val id: String,
    val orgId: String,
    val memberId: String?,
    val firstName: String,
    val lastName: String,
    val profile: String,
    val isSuperAdmin: Boolean
) {
    val isStaff: Boolean
        get() = profile in setOf("MANAGER", "TEACHER", "EDITOR", "SALESPERSON")
}

// Persists the logged-in user's profile snapshot (mirrors what the web app
// keeps in sessionStorage — see platform/web/lib/session.ts — but durable,
// since a native app doesn't get a fresh "tab" per launch). The actual auth
// credential (the uprep_auth cookie) is stored separately in CookieStore,
// used directly by AppCookieJar.
class SessionStore(private val context: Context) {

    val session: Flow<UserSession?> = context.dataStore.data.map { prefs ->
        val id = prefs[KEY_ID] ?: return@map null
        UserSession(
            id = id,
            orgId = prefs[KEY_ORG_ID] ?: "",
            memberId = prefs[KEY_MEMBER_ID],
            firstName = prefs[KEY_FIRST_NAME] ?: "",
            lastName = prefs[KEY_LAST_NAME] ?: "",
            profile = prefs[KEY_PROFILE] ?: "STUDENT",
            isSuperAdmin = prefs[KEY_IS_SUPER_ADMIN] ?: false
        )
    }

    suspend fun current(): UserSession? = session.first()

    suspend fun save(s: UserSession) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ID] = s.id
            prefs[KEY_ORG_ID] = s.orgId
            s.memberId?.let { prefs[KEY_MEMBER_ID] = it }
            prefs[KEY_FIRST_NAME] = s.firstName
            prefs[KEY_LAST_NAME] = s.lastName
            prefs[KEY_PROFILE] = s.profile
            prefs[KEY_IS_SUPER_ADMIN] = s.isSuperAdmin
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }

    // Offline login fallback: after every successful ONLINE login, cache a
    // salted PBKDF2 hash of the credential (never the plaintext password) so
    // a later login attempt with no connectivity can be verified locally
    // against the same identifier+password, restoring the already-cached
    // UserSession above without a network round-trip.
    suspend fun saveOfflineCredential(identifier: String, password: String) {
        val salt = OfflineAuth.randomSaltHex()
        context.dataStore.edit { prefs ->
            prefs[KEY_OFFLINE_IDENTIFIER] = identifier
            prefs[KEY_OFFLINE_SALT] = salt
            prefs[KEY_OFFLINE_HASH] = OfflineAuth.hash(password, salt)
        }
    }

    suspend fun verifyOfflineCredential(identifier: String, password: String): Boolean {
        val prefs = context.dataStore.data.first()
        val savedIdentifier = prefs[KEY_OFFLINE_IDENTIFIER] ?: return false
        val salt = prefs[KEY_OFFLINE_SALT] ?: return false
        val hash = prefs[KEY_OFFLINE_HASH] ?: return false
        if (savedIdentifier != identifier) return false
        return OfflineAuth.hash(password, salt) == hash
    }

    companion object {
        private val KEY_ID = stringPreferencesKey("id")
        private val KEY_ORG_ID = stringPreferencesKey("org_id")
        private val KEY_MEMBER_ID = stringPreferencesKey("member_id")
        private val KEY_FIRST_NAME = stringPreferencesKey("first_name")
        private val KEY_LAST_NAME = stringPreferencesKey("last_name")
        private val KEY_PROFILE = stringPreferencesKey("profile")
        private val KEY_IS_SUPER_ADMIN = booleanPreferencesKey("is_super_admin")
        private val KEY_OFFLINE_IDENTIFIER = stringPreferencesKey("offline_identifier")
        private val KEY_OFFLINE_SALT = stringPreferencesKey("offline_salt")
        private val KEY_OFFLINE_HASH = stringPreferencesKey("offline_hash")
    }
}
