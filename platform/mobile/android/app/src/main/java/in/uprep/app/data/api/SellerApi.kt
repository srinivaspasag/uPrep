package `in`.uprep.app.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// Mirrors platform/web's app/api/seller/verify/route.ts exactly — the one
// online call in the whole SD-card flow. Deliberately outside session auth
// (that route lives outside /api/cmds/**) so it works for a device that's
// never logged into anything else yet.
data class SellerVerifyRequest(
    val code: String,
    val email: String,
    val deviceId: String,
    val userId: String
)

data class SellerVerifyResponse(
    val ok: Boolean?,
    val groupName: String?,
    val contentIds: List<String>?,
    val encryptionKey: String?,
    val error: String?,
    // Epoch millis — 1 year from when this access code was generated. Stored
    // alongside the cached key so the device can keep enforcing it fully
    // offline after this one online call (see SdCardRepository.isExpired).
    val expiresAt: Long?
)

interface SellerApi {
    @POST("/api/seller/verify")
    suspend fun verify(@Body body: SellerVerifyRequest): Response<SellerVerifyResponse>
}
