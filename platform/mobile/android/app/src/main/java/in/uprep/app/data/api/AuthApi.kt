package `in`.uprep.app.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class LoginRequest(val identifier: String, val password: String)

data class LoginResult(
    val id: String,
    val orgId: String,
    val firstName: String,
    val lastName: String,
    val memberId: String?,
    val profile: String,
    val isSuperAdmin: Boolean
)

data class AuthEnvelope(val result: LoginResult?)

// Error responses use a different, flatter shape than success ones (no
// "result" wrapper) — see platform/web/app/api/auth/login/route.ts. Parsed
// separately from Response.errorBody() rather than modeled as a single DTO.
data class ApiError(val errorCode: String?, val errorMessage: String?)

interface AuthApi {
    @POST("/api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthEnvelope>

    @GET("/api/auth/me")
    suspend fun me(): Response<AuthEnvelope>
}
